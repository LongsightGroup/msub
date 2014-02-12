/**
 * $Id: TemplateItemEntityProvider.java 63843 2009-10-08 14:43:55Z lovemore.nalube@uct.ac.za $
 * $URL: https://source.sakaiproject.org/contrib/evaluation/branches/1.3.x/api/src/java/org/sakaiproject/evaluation/logic/entity/TemplateItemEntityProvider.java $
 * TemplateItemEntityProvider.java - evaluation - Jan 31, 2008 11:49:07 AM - azeckoski
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

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

/**
 * Allows external packages to find out the prefix for the eval group entity 
 * (deals with {@link EvalTemplateItem} model class)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface TemplateItemEntityProvider extends EntityProvider {
   public final static String ENTITY_PREFIX = "eval-templateitem";
   
   //Custom actions
	public final static String CUSTOM_TEMPLATE_ITEMS_REORDER = "template-items-reorder";
	public final static String CUSTOM_TEMPLATE_ITEMS_BLOCK = "modify-block-items";
	public final static String CUSTOM_TEMPLATE_ITEMS_UNBLOCK = "unblock";
}
