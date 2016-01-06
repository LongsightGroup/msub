/*
 * Copyright 2008 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): jbush
 */

package com.rsmart.customer.integration.processor.cle.cm;

import com.rsmart.customer.integration.processor.ProcessorState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;


public class SectionCategoryProcessor extends AbstractCMProcessor {
   private static final Log log = LogFactory.getLog(SectionCategoryProcessor.class);

   public void processRow(String[] data, ProcessorState state) throws Exception {
      String sectionCategory = data[0];
      if (log.isDebugEnabled()) log.debug("Reconciling section cateogory " + sectionCategory);

      List sectionCategories = cmService.getSectionCategories();

      if (!sectionCategories.contains(sectionCategory)) {
         cmAdmin.addSectionCategory(sectionCategory, data[1]);
      }

   }

   public String getProcessorTitle() {
      return "Section Category Processor";
   }


}
