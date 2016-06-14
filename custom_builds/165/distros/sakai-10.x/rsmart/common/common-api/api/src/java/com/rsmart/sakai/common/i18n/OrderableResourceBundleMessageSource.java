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
package com.rsmart.sakai.common.i18n;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.Ordered;



/**
 *
 */
public class OrderableResourceBundleMessageSource extends ResourceBundleMessageSource implements Ordered {
   private int order;
   private String basename;

   public int getOrder() {
      return order;
   }

   public void setOrder(int order) {
      this.order = order;
   }


   public String getBasename() {
      return basename;
   }

   public void setBasename(String basename) {
      this.basename = basename;
      super.setBasename(basename);
   }
}
