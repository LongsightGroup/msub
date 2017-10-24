package com.rsmart.sakai.emailtemplate;/*
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
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.model.EmailTemplate;
import org.sakaiproject.tool.cover.SessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class EmailTemplateLoader {
   private EmailTemplateService emailTemplateService;
   private List emailTemplates = new ArrayList();

   private static Log logger = LogFactory.getLog(EmailTemplateLoader.class);

   /**
    * loads templates into the database at startup time
    */
   public void init() {
         for (Iterator i= getEmailTemplates().iterator();i.hasNext();) {
            EmailTemplate template = (EmailTemplate) i.next();
            try {
               if (getEmailTemplateService().getEmailTemplate(template.getKey(), new Locale(template.getLocale())) != null){
                  continue;
               }
               if (template.getOwner() == null) {
                  template.setOwner("admin");
               }
               getEmailTemplateService().saveTemplate(template);
            } catch (Throwable t) {
               logger.error("error load email template into EmailTemplateService: key=" + template.getKey(), t);
            }

         }
   }

   public EmailTemplateService getEmailTemplateService() {
      return emailTemplateService;
   }

   public void setEmailTemplateService(EmailTemplateService emailTemplateService) {
      this.emailTemplateService = emailTemplateService;
   }

   public List getEmailTemplates() {
      return emailTemplates;
   }

   public void setEmailTemplates(List emailTemplates) {
      this.emailTemplates = emailTemplates;
   }
}
