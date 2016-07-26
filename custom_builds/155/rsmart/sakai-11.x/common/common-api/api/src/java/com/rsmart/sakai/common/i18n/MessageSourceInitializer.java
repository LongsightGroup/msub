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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.OrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



/**
 * Adds all instances of OrderableResourceBundleMessageSource in the specified order to the basenames of the messageSource bean.
 */
public class MessageSourceInitializer implements ApplicationContextAware {
   public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
      ResourceBundleMessageSource messageSource = (ResourceBundleMessageSource) applicationContext.getBean("messageSource", ResourceBundleMessageSource.class);
      if (messageSource == null) {
         return;
      }
      Map beans = applicationContext.getBeansOfType(OrderableResourceBundleMessageSource.class);
      if (beans == null) {
         return;
      }

      String[] bundles = orderBundles(beans.values());
      messageSource.setBasenames(bundles);
   }
   protected String[] orderBundles(Collection bundles) {
      List orderBundles = new ArrayList(bundles);
      Collections.sort(orderBundles, new OrderComparator());
      Collections.reverse(orderBundles);
      String[] basenames = new String[orderBundles.size()];
      int count = 0;
      for (Iterator i=orderBundles.iterator(); i.hasNext() ;) {
         OrderableResourceBundleMessageSource bundle = (OrderableResourceBundleMessageSource) i.next();
         basenames[count++] = bundle.getBasename();
      }
      return basenames;
   }
}
