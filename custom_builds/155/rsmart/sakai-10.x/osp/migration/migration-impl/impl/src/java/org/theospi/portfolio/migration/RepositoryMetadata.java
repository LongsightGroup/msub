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

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;




/**
 * This class represents an entry in the info.xml file, which is stored in the root directory of the osp 2.0 exported repository zip file.
 * The info.xml file contains the list of an osp 2.0 repository content's id followed by an entry describing the content.
 */
public class RepositoryMetadata {
   // logger
   private final static Log logger = LogFactory.getLog(RepositoryMetadata.class);

   // data members
   private Date   created;       // date the repository content was originally created.
   private Date   lastModified;  // date the repository content was last modified.
   private String id;            // osp 2.0 id of the repository content.
   private String mimeType;      // mime type of the repository content
   private String name;          // name of the osp 2.0 repository content.
   private String ownerId;       // osp 2.0 user id
   private long   size;          // size of the repository content in bytes.
   private String type;          // folder, fileArtifact, or form id
   private String worksiteId;    // id of the worksite where the repository content was stored.





   /**
    * constructor.
    * the metadata is the value of an entry in the info.xml file from the "directory" hashmap which is stored in the root of the osp 2.0 repository zip file.
    * <br/><br/>
    * @param created        date the repository content was originally created.
    * @param lastModified   date the repository content was last modified.
    * @param id             osp 2.0 id of the repository content.
    * @param mimeType       mime type of the repository content
    * @param name           name of the osp 2.0 repository content.
    * @param ownerId        osp 2.0 user id
    * @param size           size of the repository content in bytes.
    * @param type           folder, fileArtifact, or form id
    * @param worksiteId     id of the worksite where the repository content was stored.
    */
   public RepositoryMetadata(Object[] metadata) {
      created      = (Date  )metadata[0];
      lastModified = (Date  )metadata[1];
      id           = (String)metadata[2];
      mimeType     = (String)metadata[3];
      name         = (String)metadata[4];
      ownerId      = (String)metadata[5];
      size         = (Long  )metadata[6];
      type         = (String)metadata[7];
      worksiteId   = (String)metadata[8];
   }

   /**
    * constructor.
    * the metadata is the value of an entry in the info.xml file from the "directory" hashmap which is stored in the root of the osp 2.0 repository zip file.
    * <br/><br/>
    * @param metadata   fields containing information about a repository file.
    */
   public RepositoryMetadata(Date created, Date lastModified, String id, String mimeType, String name, String ownerId, long   size, String type, String worksiteId) {
      this.created      = created;
      this.lastModified = lastModified;
      this.id           = id;
      this.mimeType     = mimeType;
      this.name         = name;
      this.ownerId      = ownerId;
      this.size         = size;
      this.type         = type;
      this.worksiteId   = worksiteId;
   }

   /**
    * @return the date the osp 2.0 repository content was created.
    */
   public Date getCreated() {
      return created;
   }

   /**
    * @return the date the osp 2.0 repository content was last modified;
    */
   public Date getLastModified() {
      return lastModified;
   }

   /**
    * @return the osp 2.0 id of the osp 2.0 repository content.
    */
   public String getId() {
      return id;
   }

   /**
    * @return the mime type of the osp 2.0 repository content.
    */
   public String getMimetype() {
      return mimeType;
   }

   /**
    * @return the name of the osp 2.0 repository content.
    */
   public String getName() {
      return name;
   }

   /**
    * @return the osp 2.0 id of the owner of the osp 2.0 repository content.
    */
   public String getOwnerId() {
      return ownerId;
   }

   /**
    * @return the size of the osp 2.0 repository content in bytes.
    */
   public long getSize() {
      return size;
   }

   /**
    * @return the type of the osp 2.0 repository content.
    */
   public String getType() {
      return type;
   }

   /*
    * @return the worksite id where the osp 2.0 repository content was stored.
    */
   public String getWorksiteId() {
      return worksiteId;
   }

   boolean isStructuredArtifact(){
      return type!=null && !"folder".equals(type) && !"fileArtifact".equals(type);
   }

   /**
    * @return whether the osp 2.0 repository content is a file.
    */
   boolean isFile() {
      return type!=null && !"folder".equals(type);
   }

   /**
    * @return whether the osp 2.0 repository content is a folder.
    */
   boolean isFolder() {
      return type==null || "folder".equals(type);
   }

   /**
    * @return a string representation of a repository metadata entry from the info.xml file.
    */
   public String toString() {
      StringBuffer buffer = new StringBuffer();

      buffer.append("repository metadata\n");
      buffer.append("   id...........: " + id           + "\n");
      buffer.append("   name.........: " + name         + "\n");
      buffer.append("   type.........: " + type         + "\n");
      buffer.append("   mime type....: " + mimeType     + "\n");
      buffer.append("   size (bytes).: " + size         + "\n");
      buffer.append("   owner id.....: " + ownerId      + "\n");
      buffer.append("   worksite id..: " + worksiteId   + "\n");
      buffer.append("   created......: " + created      + "\n");
      buffer.append("   last modified: " + lastModified       );

      return buffer.toString();
   }
}
