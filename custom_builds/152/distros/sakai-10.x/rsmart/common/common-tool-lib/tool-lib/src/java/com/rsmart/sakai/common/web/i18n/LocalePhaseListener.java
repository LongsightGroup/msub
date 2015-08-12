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
package com.rsmart.sakai.common.web.i18n;

import org.sakaiproject.util.ResourceLoader;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import java.util.Locale;


/**
 * Add the following to your faces-application.xml
 * <lifecycle><phase-listener>org.sakaiproject.tool.gradebook.jsf.LocalePhaseListener</phase-listener></lifecycle>
 *
 * You also need these guys added to the project.xml
 *    <dependency>
 *     <groupId>mesa</groupId>
 *     <artifactId>mesa-common</artifactId>
 *     <version>0.0.1</version>
 *     <properties>
 *       <war.bundle>true</war.bundle>
 *    </properties>
 *  </dependency>
 *  <dependency>
 *     <groupId>sakaiproject</groupId>
 *     <artifactId>sakai-util-i18n</artifactId>
 *     <version>${sakai.version}</version>
 *     <properties>
 *       <war.bundle>true</war.bundle>
 *    </properties>
 *  </dependency>
 */
public class LocalePhaseListener implements PhaseListener {

   public void afterPhase(PhaseEvent phaseEvent) {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public void beforePhase(PhaseEvent phaseEvent) {
      FacesContext context = FacesContext.getCurrentInstance();

      if ( context.getViewRoot() != null) {
         ResourceLoader rb = new ResourceLoader();
         Locale locale = rb.getLocale();
         context.getViewRoot().setLocale(locale);
      }
   }

  protected Locale getLocaleFromString( String localeString )
  {
     String[] locValues = localeString.trim().split("_");
     if (locValues.length > 1)
        return new Locale( locValues[0], locValues[1] ); // language, country
     else if (locValues.length == 1 )
        return new Locale( locValues[0] ); // just language
     else
        return Locale.getDefault();
  }

   public PhaseId getPhaseId() {
     return PhaseId.ANY_PHASE;
   }
}
