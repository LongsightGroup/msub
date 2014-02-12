/**
 * $Id: ItemEntityProviderImpl.java 46025 2008-02-27 13:01:48Z aaronz@vt.edu $
 * $URL: https://source.sakaiproject.org/contrib/evaluation/branches/1.3.x/impl/src/java/org/sakaiproject/evaluation/logic/entity/ItemEntityProviderImpl.java $
 * ItemEntityProviderImpl.java - evaluation - Jan 31, 2008 2:12:40 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.entity;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.entity.ItemEntityProvider;


/**
 * Implementation for the entity provider for evaluation items (questions)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ItemEntityProviderImpl implements ItemEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider {

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }


   public String getEntityPrefix() {
      return ENTITY_PREFIX;
   }

   public boolean entityExists(String id) {
      boolean exists = false;
      Long itemId;
      try {
         itemId = new Long(id);
         if (authoringService.getItemById(itemId) != null) {
            exists = true;
         }
      } catch (NumberFormatException e) {
         // invalid number so roll through to the false
         exists = false;
      }
      return exists;
   }

}
