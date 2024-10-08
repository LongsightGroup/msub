/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/integration/context/IntegrationContextFactory.java $
 * $Id: IntegrationContextFactory.java 9347 2006-05-13 04:13:19Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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
 **********************************************************************************/

package org.sakaiproject.tool.assessment.integration.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.integration.context.spring.FactoryUtil;
import org.sakaiproject.tool.assessment.integration.helper.ifc.AgentHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.PublishingTargetHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.SectionAwareServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.ServerConfigurationServiceHelper;

/**
 * This is an abstract class.  It defines the public methods available for
 * the properties that it furnishes.
 *
 * @author Ed Smiley
 */
public abstract class IntegrationContextFactory
{
  private static Log log = LogFactory.getLog(IntegrationContextFactory.class);
  private static IntegrationContextFactory instance;

  /**
   * Static method returning an implementation instance of this factory.
   * @return the factory singleton
   */
  public static IntegrationContextFactory getInstance()
  {
    log.debug("IntegrationContextFactory.getInstance()");
    if (instance==null)
    {
      try
      {
        FactoryUtil.setUseLocator(true);
        instance = FactoryUtil.lookup();
      }
      catch (Exception ex)
      {
        log.error("Unable to read integration context: " + ex);
      }
    }
    log.debug("instance="+instance);
    return instance;
  }

  // the factory api
  public abstract boolean isIntegrated();
  public abstract AgentHelper getAgentHelper();
  public abstract GradebookHelper getGradebookHelper();
  public abstract GradebookServiceHelper getGradebookServiceHelper();
  public abstract PublishingTargetHelper getPublishingTargetHelper();
  public abstract SectionAwareServiceHelper getSectionAwareServiceHelper();
  public abstract ServerConfigurationServiceHelper getServerConfigurationServiceHelper();
}
