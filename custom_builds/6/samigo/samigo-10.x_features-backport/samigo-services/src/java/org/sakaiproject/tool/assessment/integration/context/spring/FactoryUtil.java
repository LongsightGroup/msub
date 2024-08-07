/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.integration.context.spring;

import java.io.File;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.spring.SpringBeanLocator;

/**
 * Encapsulates the Spring lookup for this factory.
 * @author Ed smiley
 */
public class FactoryUtil
{
  private static boolean useLocator = false;

  private static final String FS = File.separator;
  private static final String CONFIGURATION =
    "org" + FS + "sakaiproject" + FS + "spring" + FS + "integrationContext.xml";

  public static IntegrationContextFactory lookup() throws Exception
  {
    // the instance is provided by Spring-injection
    if (useLocator)
    {
	    SpringBeanLocator locator = SpringBeanLocator.getInstance();
	    if (locator != null) {
	      return (IntegrationContextFactory) locator.getBean("integrationContextFactory");
	    }
    }

    // unit testing
    Resource res = new ClassPathResource(CONFIGURATION);
    BeanFactory factory = new XmlBeanFactory(res);
    return (IntegrationContextFactory) factory.getBean("integrationContextFactory");
  }
  
  public static boolean getUseLocator()
  {
    return useLocator;
  }

  public static void setUseLocator(boolean use)
  {
    useLocator = use;
  }

}
