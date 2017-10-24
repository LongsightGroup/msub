/**********************************************************************************
*
* GradeExporter.java
* by Earle Nietzel
*    John Bush
*
***********************************************************************************
*
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.coursemanagement.sample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.service.gradebook.shared.ExternalGradeProvider;
import org.sakaiproject.service.gradebook.shared.GradingPeriod;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.component.api.ServerConfigurationService;


/**
 *
 */
public class GradeExporter {
	private static final Log log = LogFactory.getLog(GradeExporter.class);
 
	private CourseManagementService courseManagementService;
	private AuthzGroupService authzGroupService;
	private SiteService siteService;
	private SessionManager sessionManager;
	private SectionManager sectionManager;
	private ExternalGradeProvider externalGradeProvider;
	private UserDirectoryService userDirectoryService;
	private ServerConfigurationService serverConfigurationService;
	
	private String externalSiteProperty;
	private String externalSectionProperty;
	private String externalUserProperty;
	private String courseSiteType;
	private String termEidPropertyName;
	private String gradePeriod;
	
	private String termEid; 
	private File file;
	
	public void execute() {
		actAsAdmin();

		file = new File(serverConfigurationService.getString("grade.export.file") + "_" + getDateTime() + ".csv");

		try {
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
		} catch (IOException e) {
			if (log.isWarnEnabled()) log.warn(e.getMessage());
		}

		
		if (termEid == null) {
			// If no term was provided in the job name try and get current term(s) from cm
			List<AcademicSession> academicSessions = courseManagementService.getCurrentAcademicSessions();
			for (AcademicSession academicSession : academicSessions) {
				exportGradesForAcademicSession(academicSession.getEid());
			}
		} else {
			// Term was provided
			exportGradesForAcademicSession(termEid);
		}
		
		file = null;
	}
	
	protected void exportGradesForAcademicSession(String academicSessionEid) {
		if (log.isInfoEnabled()) log.info("Exporting grades for courses with term=" + academicSessionEid);

		Map<String, String> propertyCriteria = new HashMap<String, String>();
		propertyCriteria.put(termEidPropertyName, academicSessionEid);

		List<Site> sites = siteService.getSites(SiteService.SelectionType.NON_USER, courseSiteType, null, propertyCriteria, SiteService.SortType.NONE, null);
		for (Site site : sites) {
			site.loadAll();
			exportGradesForCourse(site);
		}
	}
	
	private void exportGradesForCourse(Site course) {
		if (log.isDebugEnabled()) log.debug("Export grades for site: " + course.getTitle() + "|" + course.getId());
		
		List<Grade> courseGrades = new ArrayList<Grade>();
		List<GradingPeriod> periods = externalGradeProvider.getGradingPeriods(course.getId());
		
		for (GradingPeriod period : periods) {
			Map<String, String> grades = null;
			
			if (gradePeriod != null) {
				// use the supplied grading period if there is one
				if (gradePeriod.equals(period.getColumnKey())) {
					grades = externalGradeProvider.getPeriodGrades(course.getId(), gradePeriod);
				} else {
					// don't process grades from other periods 
					continue;
				}
			} else {
			// otherwise get grades for all grading periods
				grades = externalGradeProvider.getPeriodGrades(course.getId(), period.getColumnKey());
			}
			
			if(grades != null) {
				for (Map.Entry<String, String> e : grades.entrySet()) {
			    	if (log.isDebugEnabled()) log.debug("Found " + period.getColumnKey() + " Grade  [key|value]=[" + e.getKey() + "|" + e.getValue() + "]");

					List<CourseSection> sections = sectionManager.getSections(course.getId());
			    	Grade aGrade = new Grade();

			    	String userId = null;
			    	try {
			    		userId = userDirectoryService.getUser(e.getKey()).getProperties().getProperty(externalUserProperty);
					} catch (UserNotDefinedException undfe) {
						if (log.isWarnEnabled()) log.warn(undfe.getMessage());
                        continue;
					}
					
		    		if ( userId != null && userId.length() > 0 )
		    			aGrade.setStudent(userId);
		    		else
		    			aGrade.setStudent(e.getKey());

					String courseId = course.getProperties().getProperty(externalSiteProperty);
					if ( courseId != null && courseId.length() > 0)
						aGrade.setCourse(courseId);
					else
						aGrade.setCourse(course.getId());

		    		aGrade.setGrade(e.getValue());
					aGrade.setCourse(course.getProperties().getProperty(externalSiteProperty));

					// check to see if course site has sections
					if (sections.isEmpty()) {
						// if no sections then leave section blank
						aGrade.setSection("");
					} else {
						// match the section with the group so we can query for the externalSectionProperty
						for (CourseSection section : sections) {
							
							// get the group from the section uuid
							// uuid="/site/b5498a2b-4514-45b4-bc8e-8431aecefdf3/group/2af53d6f-538d-4b57-9cb8-4a33d4c69365"
							String[] splitUuId = section.getUuid().split("/group/");

							Collection<Group> groups = course.getGroupsWithMember(e.getKey());
							// should only be one group that matches a section
							for (Group group : groups) {
								if (group.getId().equals(splitUuId[1])) {
									if (group.getProperties().getProperty(externalSectionProperty) != null) {
										String[] splitSection = group.getProperties().getProperty(externalSectionProperty).split("\\*");

										if (splitSection.length == 2) {
											aGrade.setSection(splitSection[1]);
										} else {
											aGrade.setSection(group.getProperties().getProperty(externalSectionProperty));
											//aGrade.setSection("");
											//if (log.isWarnEnabled()) log.warn("Invalid data for this property=" + externalSectionProperty + ", Site|Group: " + course.getId() + "|" +group.getId());
										}
									} else {
										if (log.isWarnEnabled()) log.warn("Group: " + group.getId() + "|" + group.getTitle() + ", did not have the property=" + externalSectionProperty);
										aGrade.setSection("");
									}
								}
							}
						}
					}
					// add the grade to the list
			    	courseGrades.add(aGrade);
				}
			}
		}
		// write the list to the file
		writeGradeListToFile(courseGrades);
	}

	private void writeGradeListToFile(List<Grade> list) {
		
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(file, true));
			for (Grade g : list) {
				out.write(g.toString());
				out.newLine();
			}
		} catch (IOException e) {
			if (log.isWarnEnabled()) log.warn(e.getMessage());
		} finally {
			try {
				out.close();
			} catch (IOException e1) {
				if (log.isWarnEnabled()) log.warn(e1.getMessage());
			}
		}
	}
	
	/**
	 * Convenience routine to support the frequent testing need to switch authn/authz identities.
	 * TODO Find some central place for this frequently-needed helper logic. It can easily be made
	 * static.
	 */
	private void actAsAdmin() {
		String userId = "admin";
		Session session = sessionManager.getCurrentSession();
		session.setUserEid(userId);
		session.setUserId(userId);
		authzGroupService.refreshUser(userId);
	}

	public void setCourseManagementService(CourseManagementService courseManagementService) {
		this.courseManagementService = courseManagementService;
	}

	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}
	
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	/**
	 * @param courseSiteType
	 *            site type to search for
	 */
	public void setCourseSiteType(String courseSiteType) {
		this.courseSiteType = courseSiteType;
	}
	
	/**
	 * @param termEidPropertyName
	 *            site property to match against an academic session ID; THIS IS
	 *            NOT CURRENTLY PART OF A DOCUMENTED SERVICE API
	 */
	public void setTermEidPropertyName(String termEidPropertyName) {
		this.termEidPropertyName = termEidPropertyName;
	}

	/**
	 * @param termEid
	 *            academic session if left null, all current academic sessions are checked
	 */
	public void setTermEid(String termEid) {
		this.termEid = termEid;
	}

	public void setSectionManager(SectionManager sectionManager) {
		this.sectionManager = sectionManager;
	}

	public void setExternalGradeProvider(ExternalGradeProvider externalGradeProvider) {
		this.externalGradeProvider = externalGradeProvider;
	}

	public void setExternalSiteProperty(String externalSiteProperty) {
		this.externalSiteProperty = externalSiteProperty;
	}

	public void setExternalSectionProperty(String externalSectionProperty) {
		this.externalSectionProperty = externalSectionProperty;
	}

	public void setExternalUserProperty(String externalUserProperty) {
		this.externalUserProperty = externalUserProperty;
	}

	public void setGradePeriod(String gradePeriod) {
		this.gradePeriod = gradePeriod;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	

	private final static String getDateTime() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
		return df.format(new Date());
	}

	class Grade extends Object {
		private String student;
		private String course;
		private String section;
		private String grade;
		
		public void setStudent(String student) {
			this.student = student;
		}
		public void setCourse(String course) {
			this.course = course;
		}
		public void setSection(String section) {
			this.section = section;
		}
		public void setGrade(String grade) {
			this.grade = grade;
		}
		
		@Override
		public String toString() {
			return student + "," + section + "," + course + "," + grade;
		}
	}
}
