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

package com.rsmart.sakaiproject.integration.coursemanagement.impl;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.impl.CourseManagementServiceSampleChainImpl;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: May 21, 2007
 * Time: 10:56:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class CourseManagementServiceTermProperties extends CourseManagementServiceSampleChainImpl {

   protected final transient Log logger = LogFactory.getLog(getClass());

   private SimpleDateFormat dateFormat;

   private List academicSessions;
   private Map<String, String> sectionCategories = new Hashtable<String, String>();
   private List<String> sectionCategoryCodes = new ArrayList<String>();
   private String dateFormatStr = "yyyyMMddHHmmssSSS";
    private CourseManagementService cmService;
    private CourseManagementAdministration cmAdmin;
    private SecurityService securityService;
    private SessionManager sessionManager;

   public void init() {
      int terms = ServerConfigurationService.getInt("termlistabbr.count", 0);
      int categories = ServerConfigurationService.getInt("section.category.count", 0);

      academicSessions = new ArrayList();

      dateFormat = new SimpleDateFormat(dateFormatStr);

      try {
    	  if( terms > 0 && !cmService.isAcademicSessionDefined(createSession(1).getEid()))
    	  {
    		  Session sakaiSession = sessionManager.getCurrentSession();
    		  try {
    			  sakaiSession.setUserEid("admin");
    			  sakaiSession.setUserId("admin");
    			  securityService.pushAdvisor(new SecurityAdvisor(){

    				  @Override
    				  public SecurityAdvice isAllowed(String userId,
    						  String function, String reference) {
    					  // TODO Auto-generated method stub
    					  return SecurityAdvice.ALLOWED;
    				  }
    			  });
    			  for (int i=1;i<=terms;i++) {
    				  AcademicSession session = createSession(i);
    				  session = cmAdmin.createAcademicSession(session.getEid(), session.getTitle(), session.getDescription(), session.getStartDate(), session.getEndDate());
    				  setCurrentStatus(session);
    			  }
    		  }
    		  finally
    		  {
    			  sakaiSession.setUserEid("");
    			  sakaiSession.setUserId("");
    			  securityService.popAdvisor();
    		  }
    	  }
      } catch (ParseException e) {
    	  logger.error("error initializing terms from properties", e);
      }

      for (int i=1;i<=categories;i++) {
         String code = ServerConfigurationService.getString("section.category.code." + i);
         sectionCategories.put(code,
            ServerConfigurationService.getString("section.category.desc." + i));
         sectionCategoryCodes.add(code);
      }
   }
   
   private void setCurrentStatus(AcademicSession session) {
       List<AcademicSession> currentSessions = cmService.getCurrentAcademicSessions();
       List<String> currentTerms = new ArrayList<String>();

      // initialize the array with the current sessions
      for (AcademicSession s: currentSessions) {
               currentTerms.add(s.getEid());
       }

          // add this session if its end date is after today
       if (session.getEndDate().after(new Date())) {
              if (!currentTerms.contains(session.getEid().toString())) {
                      currentTerms.add(session.getEid());
              }
       // otherwise remove this session
       } else {
           if (currentTerms.contains(session.getEid().toString())) {
                   currentTerms.remove(session.getEid().toString());
           }
       }
       cmAdmin.setCurrentAcademicSessions(currentTerms);
   }

   	public boolean isAcademicSessionDefined(String eid) {
         return cmService.isAcademicSessionDefined(eid);
	}

   protected AcademicSession createSession(int index) throws ParseException {
      String eid = ServerConfigurationService.getString("termlistabbr." + index);
      String termterm = ServerConfigurationService.getString("termterm." + index);
      String termyear = ServerConfigurationService.getString("termyear." + index);
      String title = termterm;
        if (termyear != null && termyear.length() > 0){
            title = title + " " + termyear;
        }
      String description = title;
      Date startDate = dateFormat.parse(ServerConfigurationService.getString("termstarttime." + index));
      Date endDate = dateFormat.parse(ServerConfigurationService.getString("termendtime." + index));
      boolean currentTerm = ServerConfigurationService.getBoolean("termiscurrent." + index, false);
      
      return new AcademicSessionPropertiesImpl(eid, title, description, startDate, endDate, currentTerm);
   }

   public AcademicSession getAcademicSession(String academicSessionEid) throws IdNotFoundException {
        if (cmService.isAcademicSessionDefined(academicSessionEid)){
            return cmService.getAcademicSession(academicSessionEid);
        }
      return null;
   }

   public List getAcademicSessions() {
        //if (storeTermsInDatabase) {
            return cmService.getAcademicSessions();
        //}
      //return academicSessions;
   }

   public List getCurrentAcademicSessions() {
            return cmService.getCurrentAcademicSessions();
   }

    public void setDateFormatStr(String dateFormatStr) {
        this.dateFormatStr = dateFormatStr;
    }

    public void setAcademicSessions(List academicSessions) {
      this.academicSessions = academicSessions;
   }

   public List<String> getSectionCategories() {
      return sectionCategoryCodes;
   }

   public String getSectionCategoryDescription(String categoryCode) {
      return sectionCategories.get(categoryCode);
   }

    public void setCmService(CourseManagementService cmService) {
        this.cmService = cmService;
    }
    
    public void setCmAdmin(CourseManagementAdministration cmAdmin) {
    	this.cmAdmin = cmAdmin;
    }

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
}
