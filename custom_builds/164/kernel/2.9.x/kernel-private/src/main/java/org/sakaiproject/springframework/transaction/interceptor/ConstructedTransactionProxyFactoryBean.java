/*
 * *********************************************************************************
 *  $URL: https://svn.rsmart.com/svn/vendor/branches/sakai-kernel/rsmart-cle/kernel-private/src/main/java/org/sakaiproject/springframework/transaction/interceptor/ConstructedTransactionProxyFactoryBean.java $
 *  $Id: ConstructedTransactionProxyFactoryBean.java 26198 2011-04-08 19:28:14Z jcrodriguez $
 * **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * *********************************************************************************
 *
 */

package org.sakaiproject.springframework.transaction.interceptor;

import org.springframework.beans.BeanUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: John Ellis
 * Date: May 23, 2006
 * Time: 11:35:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConstructedTransactionProxyFactoryBean extends TransactionProxyFactoryBean {

   private DynamicTargetSource dynamicTargetSource;
   private Object target;

   public ConstructedTransactionProxyFactoryBean(
      PlatformTransactionManager transactionManager, Object targetPrototype,
      Properties transactionAttributes) {
      setTransactionManager(transactionManager);
      try {
         dynamicTargetSource =
            new DynamicTargetSource(loadTarget(targetPrototype.getClass()));
         super.setTarget(dynamicTargetSource);
      } catch (ClassNotFoundException e) {
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }
      setTransactionAttributes(transactionAttributes);
      init();
   }

   protected Object loadTarget(Class targetClass) throws ClassNotFoundException {
      return BeanUtils.instantiateClass(targetClass);
   }

   public void init() {
      super.afterPropertiesSet();
   }

   public void setTarget(Object target) {
      this.target = target;
   }

   public Object getTarget() {
      return target;
   }

   public void afterPropertiesSet() {
      dynamicTargetSource.setTarget(getTarget());
   }

}
