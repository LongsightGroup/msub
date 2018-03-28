/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/
package org.theospi.portfolio.migration;

import java.beans.XMLDecoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.exception.*;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.content.api.*;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.metaobj.shared.mgt.MetaobjEntityManager;
import org.sakaiproject.metaobj.registry.FormResourceType;
import org.theospi.portfolio.security.impl.AllowAllSecurityAdvisor;





/**
 *
 * This imports a zip file created by exporting OSP Repository files into Sakai Resources (via Sakai ContentHosting).
 *
 * Usage:
 *    Examine hard-coded constants and verify their values
 *    Install as a Sakai Quartz Scheduler Job
 *    This program respects the osp_repository_lock table, so it may be best to run it before importing that data
 *    Setup Quartz Job to run once (mostly harmless to run multiple times, but be careful about OVERWRITE_PRE_EXISTING_FILES setting)
 *
 * Known issues:
 *      All file creation dates show as the date and time that this program is run
 *      Empty folders will not be re-created
 *      Processing stops abruptly when Exceptions occur
 *      Exceptions occur when attempting to delete locked content
 *      Configuration is hard-coded
 */
public class ImportRepositoryZip implements Job {
   // logger
   private final static Log logger = LogFactory.getLog(ImportRepositoryZip.class);

   // class members
   public final static boolean OVERWRITE_PRE_EXISTING_FILES = true;
   public final static String  DEFAULT_PATH                 = "/tmp";
   public final static String  DEFAULT_FILENAME             = "ospi-export";
   public final static String  DEFAULT_SUFFIX               = ".zip";
   public final static String FILE_FOLDER                   = "files/";           // the trailing slash on the following three constants has been added as a workaround
   public final static String LOST_FOLDER                   = "lost-and-found/";  // to accommodate the unnecessary slash that was introduced by the export tool.
   public final static String TECH_FOLDER                   = "tech-metadata/";

   // data members
   private boolean overWritePreExistingFiles;               // whether to overwrite existing files with the same name in sakai content hosting (resources) when importing osp 2.0 repository content

   // sakai services (injected via the spring frameworks IoC)
   private ContentHostingService      contentHostingService;
   private SecurityService            securityService;
   private ServerConfigurationService serverConfigurationService;
   private SessionManager             sessionManager;






   /**
    * method which reads in a zip file containing repository content from an osp 2.0.x instance and imports the content into osp 2.1's resources.
    * <br/><br/>
    * @param context   quartz job execution context.
    * <br/><br/>
    * @throws JobExecutionException   if the job fails to run.
    */
   public void execute(JobExecutionContext context) throws JobExecutionException {
      logger.info("osp 2.0 repository migration quartz job started");

      Date start = new Date();

      // get the path name of the zip file containing the content from the osp 2.0 repository
      String repositoryZipFile = serverConfigurationService.getString("osp_repository_zip_file");
      if (repositoryZipFile == null || repositoryZipFile.trim().length() == 0)
         throw new JobExecutionException("The location of the osp 2.0 repository zip file was not specified by the osp_repository_zip_file property in sakai.properties.");

      // get whether existing files in sakai's content hosting (resources) should be overwritten when importing the osp 2.0 repository content if a name conflict occurs
      overWritePreExistingFiles = serverConfigurationService.getBoolean("overwrite_pre-existing_files", false);

      // convert the content from the osp 2.0 repository to content in the resources in osp 2.1
      try {
         importFromZip(repositoryZipFile);
         Date end = new Date();
         long seconds = end.getTime() - start.getTime() / 1000;
         SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
         logger.info("osp 2.0 repository migration quartz job finished in " + seconds + " seconds: " + dateFormat.format(new Date(end.getTime() - start.getTime())) + ".");
      } catch (Exception ex) {
         logger.info("osp 2.0 repository migration quartz job aborted due to an error.");
         ex.printStackTrace();
         throw new JobExecutionException(ex);
      }
   }

   /**
    * migrate the repository content found in the zip file to content in the osp 2.1 resources.
    * <br/><br/>
    * @param filename   zip file containing the content from the osp 2.0 repository.
    * <br/><br/>
    * @throws Exception   if the osp 2.0 repository content from the zip file can not be converted to content in the osp 2.1 resources.
    */
   public void importFromZip(String filename) throws Exception {
      logger.info("reading osp 2.0 repository zip file " + filename);

      // read in the info.xml file from the root directory of the exported osp 2.0 repository zip file.
      // the info.xml file contains two lists:
      // list                                                                                                     begins in xml file at node:
      // ------------------------------------------------------------------------------------------------------   ---------------------------
      // 1. list of users                    (user id)                                                            <string>agents</string>
      // 2. hashmap of repository file info  (key = repository content id, value = repository content metadata)   <string>directory</string>
      ZipFile               zipFile   = new ZipFile(new File(filename));
      XMLDecoder            xdec      = new XMLDecoder(retrieveFileFromZip(zipFile, "INFO.XML"));
      Map<Object, Object>   info      = (Map<Object, Object>)xdec.readObject();
      HashSet<String>       agents    = (HashSet<String>      )info.get("agents");
      Map<String, Object[]> directory = (Map<String, Object[]>)info.get("directory");

      // become the 'admin user'
      securityService.pushAdvisor(new AllowAllSecurityAdvisor());
      Session sakaiSession = sessionManager.getCurrentSession();
      String  userId       = sakaiSession.getUserId();
      String  userEid      = sakaiSession.getUserEid();
      sakaiSession.setUserId ("admin");
      sakaiSession.setUserEid("admin");

      // import the osp 2.0 repository zip file into the resources
      importFiles(directory, zipFile);

      // revert back to the original logged in user
      securityService.popAdvisor();
      sakaiSession.setUserEid(userEid);
      sakaiSession.setUserId (userId );
   }

   /**
    *
    * <br/><br/>
    * @param directory   hash map of osp 2.0 repository content in the zip file and their osp 2.0 id's.
    * @param zipFile     zip file containing the osp 2.0 repository content,
    * <br/><br/>
    * @throws Exception  if the repository content can not be imported and stored in sakai's content hosting (resources tool).
    */
   private void importFiles(Map<String, Object[]> directory, ZipFile zipFile) throws Exception {

      int numFiles            = 0;
      int numFilesImported    = 0;
      int numFilesBadMetaData = 0;
      int numFilesFailed      = 0;

      // iterate over each of the osp 2.0 repository entries in the zip file
      for(String repositoryId : directory.keySet()) {
         RepositoryEntry entry = new RepositoryEntry(directory.get(repositoryId));

         logger.debug("repository content - id: " + repositoryId + ", " + (entry.isFile() ? "file..: " : "folder: ") + entry.getName());

         if (entry.isFile()) {
            logger.debug("repository file: " + numFiles + ", " + entry.getPath() + "/" + entry.getName() + " (owner: " + entry.getOwnerId() + ")");

            String             fileSrc  = makeAbsoluteExportPath(FILE_FOLDER, entry.getPath(), entry.getName());
            String             techSrc  = makeAbsoluteExportPath(TECH_FOLDER, entry.getPath(), entry.getName() + ".xml");
            RepositoryMetadata metadata = null;

            numFiles++;
            try {
               XMLDecoder xdec = new XMLDecoder(retrieveFileFromZip(zipFile, techSrc));
               metadata = new RepositoryMetadata((Object[])xdec.readObject());
            } catch (Exception ex) {
//             ex.printStackTrace();
               numFilesBadMetaData++;
               logger.error("unable to retrieve technical metadata for " + techSrc + "./n" + ex.getMessage());                                     // mark file as being 10k
               metadata = new RepositoryMetadata(new Date(), new Date(), repositoryId, entry.getMimetype(), entry.getName(), entry.getOwnerId(), new Long(10 * 1024), entry.getType(), entry.getWorksiteId());
            }
            entry.setOwnerId(metadata.getOwnerId());
            File newFile = entry.getDestinationFile();
            ContentResource resource = null;
            try {
               resource = createFile(retrieveFileFromZip(zipFile, fileSrc), newFile, entry.getMimetype(), metadata);
               logger.info("migrated osp 2.0 repository content " + entry.getName() + " to sakai resource file " + newFile.getPath());
               numFilesImported++;

               // save old osp 2.0 repository id and new sakai resource id to a table
               setUuid(resource.getId(), repositoryId);
            } catch (Exception ex) {
               logger.error("unable to retrieve the repository file " +  fileSrc + "from the zip file.");
               numFilesFailed++;
            }
         }
      }
      logger.info("num files processed....: " + numFiles);
      logger.info("num files migrated.....: " + numFilesImported);
      logger.info("num files bad meta data: " + numFilesBadMetaData);
      logger.info("num files failed.......: " + numFilesFailed);
   }

   /**
    * @return an input stream for reading the file extracted from the zip file.
    * <br/><br/>
    * @param zipFile   the zip file containing the osp 2.0 repository files.
    * @param filename  the name of the file that is to be extracted from the zip file.
    * <br/><br/>
    * @throws IOException   if the specified filename can not be extracted from the zip file.
    */
   private InputStream retrieveFileFromZip(ZipFile zipFile, String filename) throws IOException {
      ZipEntry zipEntry = zipFile.getEntry(filename);
      return zipFile.getInputStream(zipEntry);
   }

   /**
    * @return the path in the osp 2.1 content hosting resource tree where the osp 2.0 repository item will be migrated to.
    * <br/><br/>
    * @param prefixFolder    all osp 2.0 repository items migrated to osp 2.1 resources are placed in sub-folders of the specified prefix folder.
    * @param rootDirectory   folder part of the osp 2.0 repository item's pathname.
    * @param nodeName        name of the osp 2.0 repository item.
    */
   private String makeAbsoluteExportPath(String prefixFolder, String rootDirectory, String nodeName) {
       StringBuffer dest = new StringBuffer();

       dest.append(prefixFolder);
       dest.append(rootDirectory);
       dest.append("/");
       dest.append(nodeName);

       return dest.toString();
    }

   public String combine(String str1, String str2) {
       if (str2.startsWith("/")) str2 = str2.substring(1);
       if (str1.endsWith("/")) return str1 + str2;
       return str1 + "/" + str2;
    }

   // handy stuff derived from org.theospi.portfolio.admin.ImportResourcesTask
   protected ContentCollection createOrGetCollection(String filename) throws InconsistentException, PermissionException, IdUsedException, IdInvalidException, TypeException {
      File dir = new File(filename);
      return createOrGetCollection(dir);
   }

   protected ContentCollection createOrGetCollection(File dir) throws InconsistentException, PermissionException, IdUsedException, IdInvalidException, TypeException {
      logger.info("createOrGetCollection("+dir.getPath());
      try {
         return contentHostingService.getCollection(getUnixDirPath(dir));
      } catch (IdUnusedException e) {
         // wasn't found, so we need to create it
         return createCollection(getUnixDirPath(dir), dir.getName());
      }
   }

   /**
    * change the id of the newly imported resource file to the old osp 2.0 repository id.
    * <br/>br/>
    * @param repositoryId    the old osp 2.0 repository id.
    * @param resourceId      the new osp 2.1 resource id.
    * <br/>br/>
    * @throws SQLException   if the new 2.1 resource id can not be changed to the original osp 2.0 repository id.
    */
   protected void setUuid(String resourceId, String repositoryId) throws SQLException {
      logger.info("restoring the repository id (" + repositoryId + ") in the converted resource (" + resourceId + ")");

      Connection connection = SqlService.borrowConnection();
      boolean    wasCommit  = connection.getAutoCommit();
      connection.setAutoCommit(false);

      String sql = "update CONTENT_RESOURCE set RESOURCE_UUID = ? where RESOURCE_ID = ?";
      Object[] fields = new Object[2];
      fields[0] = repositoryId;
      fields[1] = resourceId;
      SqlService.dbWrite(connection, sql, fields);

      connection.commit();
      connection.setAutoCommit(wasCommit);
      SqlService.returnConnection(connection);
   }

   /**
    * @return   the osp 2.1 resource collection that is created from appending the filename on to the given path.
    * <br/><br/>
    * @param path       parent directory.
    * @param filename   name of folder to create.
    * <br/><br/>
    * @throws PermissionException    if the user does not have sufficient rights to create the resource collection (folder).
    * @throws IdUsedException        if an error occurs.
    * @throws IdInvalidException     if an error occurs.
    * @throws InconsistentException  if an error occurs.
    */
   protected ContentCollection createCollection(String path, String filename) throws PermissionException, IdUsedException, IdInvalidException, InconsistentException {

      ResourcePropertiesEdit resourceProperties = contentHostingService.newResourceProperties();

      resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, filename);
      resourceProperties.addProperty (ResourceProperties.PROP_DESCRIPTION , filename);

      return contentHostingService.addCollection(path, resourceProperties);
   }

   /**
    * @return the specified directory using forward slashes as the file separator character as appends a trailing forward slash as well.
    * <br/><br/>
    * @param dir   directory to convert to unix style.
    */
   protected String getUnixDirPath(File dir) {
      return getUnixFilePath(dir) + '/';
   }

   /**
    * @return the specified directory using forward slashes, /, as the file separator character.
    * <br/><br/>
    * @param dir   directory to convert to unix style.
    */
   protected String getUnixFilePath(File dir) {
      String path = dir.getPath();
      if (!File.separator.equals("/")) {
         path = path.replace(File.separatorChar, '/');
      }
      return path;
   }

   /**
    * creates a resource from the osp 2.0 repository item.
    * <br/><br/>
    * @return             the newly converted osp 2.1 resource item containing the content of the osp 2.0 repository item.
    * <br/><br/>
    * @param fromStream   input stream to osp 2.0 repository item within the zip file.
    * @param toFile       new osp 2.1 resource item.
    * @param fileType     osp 2.0 repository item's mime type.
    * @param metadata     some information about the osp 2.0 repository item.
    * <br/><br/>
    * @throws IOException              if an error occurs.
    * @throws IdInvalidException       if an error occurs.
    * @throws IdUnusedException        if an error occurs.
    * @throws IdUsedException          if an error occurs.
    * @throws InUseException           if an error occurs.
    * @throws InconsistentException    if an error occurs.
    * @throws OverQuotaException       if an error occurs.
    * @throws PermissionException      if an error occurs.
    * @throws ServerOverloadException  if an error occurs.
    * @throws TypeException            if an error occurs.
    */
   protected ContentResource createFile(InputStream fromStream, File toFile, String fileType, RepositoryMetadata metadata) throws IOException, InconsistentException, PermissionException, IdUsedException, IdInvalidException, TypeException, IdUnusedException, InUseException, OverQuotaException, ServerOverloadException {
      // read in the contents of the osp 2.0 repository item from the zip file
      ByteArrayOutputStream bos       = new ByteArrayOutputStream();
      int                   c         = fromStream.read();

      while (c != -1) {
         bos.write(c);
         c = fromStream.read();
      }
      byte[] fileBytes = bos.toByteArray();


      // set some properties needed to create a resource item
      ResourcePropertiesEdit resourceProperties = contentHostingService.newResourceProperties ();
      resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME    , toFile.getName());
      resourceProperties.addProperty (ResourceProperties.PROP_DESCRIPTION     , toFile.getName());
      resourceProperties.addProperty (ResourceProperties.PROP_CONTENT_ENCODING, "UTF-8");
      // todo: these are not working...
      resourceProperties.addProperty (ResourceProperties.PROP_CREATOR         , metadata.getOwnerId());
      resourceProperties.addProperty (ResourceProperties.PROP_CREATION_DATE   , "" + metadata.getCreated());
      resourceProperties.addProperty (ResourceProperties.PROP_MODIFIED_BY     , metadata.getOwnerId());
      resourceProperties.addProperty (ResourceProperties.PROP_MODIFIED_DATE   , "" + metadata.getLastModified());


      if (metadata.isStructuredArtifact()) {
//         resource.setResourceType(FormResourceType.FORM_TYPE_ID);
         resourceProperties.addProperty(ResourceProperties.PROP_STRUCTOBJ_TYPE, metadata.getType());
         resourceProperties.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, MetaobjEntityManager.METAOBJ_ENTITY_PREFIX);
         fileType = "application/x-osp";
      }


      // make sure there isn't already a resource item with the same name
      boolean exists;
      try {
         contentHostingService.getResource(getUnixFilePath(toFile));
         exists = true;
      } catch (IdUnusedException e) {
         exists = false;
      }
      if (exists && !overWritePreExistingFiles)
         return null;
      else if (exists)
         contentHostingService.removeResource(getUnixFilePath(toFile));

      // store the osp 2.0 repository item's content in a resource
      ContentResource resource =  contentHostingService.addResource(getUnixFilePath(toFile), fileType, fileBytes, resourceProperties, NotificationService.NOTI_NONE);

      if (metadata.isStructuredArtifact()) {
         ContentResourceEdit edit = contentHostingService.editResource(resource.getId());
         edit.setResourceType(ResourceType.TYPE_METAOBJ);
         contentHostingService.commitResource(edit);
      }
      return resource;
    }

   /**
    * called by the spring framework to initialize the contentHostingService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param contentHostingService   the implementation of the ContentHostingService interface provided by the spring framework.
    */
   public void setContentHostingService(ContentHostingService contentHostingService) {
      this.contentHostingService = contentHostingService;
   }

   /**
    * called by the spring framework to initialize the securityService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param securityService   the implementation of the SecurityService interface provided by the spring framework.
    */
   public void setSecurityService(SecurityService securityService) {
      this.securityService = securityService;
   }

   /**
    * called by the spring framework to initialize the serverConfigurationService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param serverConfigurationService   the implementation of the ServerConfigurationService interface provided by the spring framework.
    */
   public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
      this.serverConfigurationService = serverConfigurationService;
   }

   /**
    * called by the spring framework to initialize the sessionManager data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param sessionManager   the implementation of the SessionManager interface provided by the spring framework.
    */
   public void setSessionManager(SessionManager sessionManager) {
      this.sessionManager = sessionManager;
   }
}
