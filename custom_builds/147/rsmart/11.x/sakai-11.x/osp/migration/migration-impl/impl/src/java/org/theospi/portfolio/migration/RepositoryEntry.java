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

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;




/**
 * This class represents an entry in the info.xml file, which is stored in the root directory of the osp 2.0 exported repository zip file.
 * The info.xml file contains the list of an osp 2.0 repository content's id followed by an entry describing the content.
 */
public class RepositoryEntry {
   // logger
   private final static Log logger = LogFactory.getLog(RepositoryEntry.class);

   // class members
   public final static String PORTFOLIO_LOST_FILES = "PORTFOLIO-Lost-And-Found";
   public final static String DESTINATION_PREFIX   = "portfolio";                  // try to avoid filename collisions when importing by putting osp 2.0 repository content in this sub-folder of sakai's resources

   // data members
   private String path;         // path within the exported zip file where the repository content is located
   private String name;         // name of the repository content
   private String ownerId;      // osp 2.0 user id
   private String type;         // folder, fileArtifact, or form id
   private String mimetype;     // mime type of the repository content

   // regular expression patterns
   private Pattern cleanupPath    = Pattern.compile("/*(.*?)");             // removes all leading slashes
   private Pattern lookupElement2 = Pattern.compile("/.*?/(.*?)/.*");       // just grabs 2 out of /1/2/3/4/5...
   private Pattern lookupElement3 = Pattern.compile("/.*?/.*?/(.*?)/.*");   // just grabs 3 out of /1/2/3/4/5...
   private Pattern ignore3        = Pattern.compile("/.*?/.*?/.*?/(.*)");   // grabs 4/5... out of /1/2/3/4/5...




   /**
    * constructor.
    * the metadata is the value of an entry in the info.xml file from the "directory" hashmap which is stored in the root of the osp 2.0 repository zip file.
    * <br/><br/>
    * @param metadata   fields containing information about a repository file.
    */
   public RepositoryEntry(Object[] metadata) {
      type     = (String)metadata[0];
      path     = cleanupPath((String)metadata[1]);
      name     = (String)metadata[2];
      ownerId  = (String)metadata[3];
      mimetype = (String)metadata[4];
   }

   /**
    * @return the path to the repository file with a single leading forward slash.
    * <br/><br/>
    * @param path   path to the repository file.
    */
   public String cleanupPath(String path) {
      return "/" + processRegexp(cleanupPath, path);
   }

   /**
    * @return   the new location within osp 2.1's content hosting's resource tree where the converted osp 2.0 repository item will be stored.
    */
   public File getDestinationFile() {
      String userDefinedFolder = processRegexp(ignore3, path); // cut out the system stuff, including userid, etc.
             userDefinedFolder = (userDefinedFolder == null ? "/" : "/" + userDefinedFolder + "/");

      //
      String prefix;
      if (isWorksite()) {
          String worksiteId = getWorksiteId();
          if (worksiteId == null && !path.endsWith("/")) {
             path += "/";
             worksiteId = getWorksiteId();
          }
          if (worksiteId == null)
             worksiteId = PORTFOLIO_LOST_FILES + "/unknown";

          prefix = "/group/" + worksiteId + "/";
      } else {
          prefix = "/user/" + ownerId + "/";
      }
      String filename = prefix + DESTINATION_PREFIX + userDefinedFolder + name;

      // convert characters in the osp 2.0 repository item's name that are illegal in osp 2.1's content hosting tool to underscores.
      // Since org.sakaiproject.util.Validator.INVALID_CHARS_IN_RESOURCE_ID is declared protected instead of public,
      // I had to copy and paste the list of illegal characters here instead of reusing them.
      String       INVALID_CHARS_IN_RESOURCE_ID = "^{}[]()%*?#&=\n\r\t\b\f";
      StringBuffer buffer                       = new StringBuffer();
      char         c;

      for (int i=0; i<filename.length(); i++)
      {
         c = filename.charAt(i);
         buffer.append(INVALID_CHARS_IN_RESOURCE_ID.indexOf(c) == -1 ? c : '_');
      }
      filename = buffer.toString();
 //   logger.debug("new file name will be: " + filename);

      return new File(filename);
   }

   /**
    * @return the mime type of the osp 2.0 repository entry.
    */
   public String getMimetype() {
      return mimetype;
   }

   /**
    * @return the name of the osp 2.0 repository entry.
    */
   public String getName() {
      return name;
   }

   /**
    * @return the osp 2.0 id of the owner of the osp 2.0 repository entry.
    */
   public String getOwnerId() {
      return ownerId;
   }

   /**
    * set the id of the repository entry's owner.
    * <br/><br/>
    * @param ownderId   id of the repository entry's owner.
    */
   public void setOwnerId(String ownderId) {
      this.ownerId = ownderId;
   }

   /**
    * @return  the parent folder of this repository item, but does not include the "/files" part of the path.
    */
   public String getParentFolder() {
      return path.substring("/files".length());
   }

   /**
    * @return the path to the osp 2.0 repository entry.
    */
   public String getPath() {
      return path;
   }

   /**
    * @return the type of the osp 2.0 repository entry.
    */
   public String getType() {
      return type;
   }

   /*
    * @return the worksite id from the 3rd component of the repository entry's path name.
    * ex: /files/worksites/<worksite id>/...
    */
   public String getWorksiteId() {
      return processRegexp(lookupElement3, path);
   }

   /**
    * @return whether the osp 2.0 repository entry is a file.
    */
   boolean isFile() {
      return type!=null && !"folder".equals(type);
   }

   /**
    * @return whether the osp 2.0 repository entry is a folder.
    */
   boolean isFolder() {
      return type==null || "folder".equals(type);
   }

   /**
    * @return whether the osp 2.0 repository entry is a worksite.
    * repository entries that belong to a worksite have a path in the following format: /files/worksites/<worksite id>/...
    */
   public boolean isWorksite() {
      return "worksites".equals(processRegexp(lookupElement2, path));
   }

   /**
    * @return the portion of the target string matched by the regular expression pattern, or null if there is no match.
    * <br/><br/>
    * @param pattern  a pre-compiled regular expression.
    * @param target   target string to which the regular expression will be applied.
    */
   public String processRegexp(Pattern pattern, String target) {
      String  result  = null;
      Matcher matcher = pattern.matcher(target);

      if (matcher.matches())
         result = matcher.group(1);

 //   logger.debug("regexp: " + pattern.pattern() + " against " + target + " --> " + result);
      return result;
   }
}
