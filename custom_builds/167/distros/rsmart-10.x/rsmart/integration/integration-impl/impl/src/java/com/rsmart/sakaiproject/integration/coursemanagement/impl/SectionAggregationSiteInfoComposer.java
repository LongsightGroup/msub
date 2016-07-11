package com.rsmart.sakaiproject.integration.coursemanagement.impl;



import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.sitemanage.api.SiteInfoComposer;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.sitemanage.api.SectionField;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Sep 14, 2009
 * Time: 10:34:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class SectionAggregationSiteInfoComposer implements SiteInfoComposer {
    protected final transient Log logger = LogFactory.getLog(getClass());
    public org.sakaiproject.site.api.SiteService siteService = null;
    private boolean includeTerm = true;
    private boolean includeCourseName = true;
    private String delim = " / ";
	protected CourseManagementService courseManagementService;
    private AuthzGroupService authzGroupService;
    private GroupProvider groupProvider;

    public String composeTitle(Site site) {
        if (site.getType().equals("course")) {
            List sectionList = getSectionList(site);
            if (sectionList.size() > 0) {
                return composeTitleWithSections(sectionList);
            }
        }
        return site.getTitle();
    }

    public String composeTitle(String academicSessionEid, List<SectionField> sectionFieldsList) {
        return composeTitleWithSections( getSectionsFromSectionFields(sectionFieldsList),
                courseManagementService.getAcademicSession(academicSessionEid));
    }

    public void updateSiteTitle(Site site) {
        if (site != null) {
            site.loadAll();
            if (site.getType().equals("course")) {
                List sectionList = getSectionList(site);
                if (sectionList.size() > 0) {
                    try {
                        // truncate length otherwise can end up with titles that exceed database limits
                        site.setTitle(truncate(composeTitleWithSections(sectionList), 99));
                        siteService.save(site);
                    } catch (Exception e) {
                        logger.error("error updating title  for site object, site refid=" + site.getId(), e);
                        throw new RuntimeException(e);
                    }

                }
            }
        }
    }

    public String truncate(String value, int length) {
        if (value != null && value.length() > length)
            value = value.substring(0, length);
        return value;
    }

    public String composeDescription(List sectionList) {
        if (sectionList.size() > 0 && sectionList.get(0) instanceof String) {
            return composeDescriptionWithSections(getSectionsFromIds(sectionList));
        }
        return composeDescriptionWithSections(sectionList);
    }

    private List getSectionsFromSectionFields(List<SectionField> sectionList) {

        List sections = new ArrayList();
        for (Iterator i = sectionList.iterator(); i.hasNext(); ) {
            List sectionFields = (List) i.next();
            Section section = new SectionCmImpl();

            StringBuffer title = new StringBuffer();

            title.append(((SectionField) sectionFields.get(0)).getValue() + "-");
            title.append(((SectionField) sectionFields.get(1)).getValue() + "-");
            title.append(((SectionField) sectionFields.get(2)).getValue());

            section.setTitle(title.toString());
            sections.add(section);
        }

        return sections;
    }



    private List getSectionsFromIds(List<String> sectionList) {
        List sections = new ArrayList();
        for (String sectionId : sectionList){
             try {
                Section section = courseManagementService.getSection(sectionId);
                sections.add(section);
            } catch (IdNotFoundException ide) {
                logger.error("can't find section with eid:" + sectionId);
            }
        }
        return sections;
    }

    public String composeTitle(List sectionList) {
        if (sectionList.size() > 0 && sectionList.get(0) instanceof String) {
            return composeTitleWithSections(getSectionsFromIds(sectionList));
        }
        return composeTitleWithSections(sectionList);
    }

    protected void updateTitle(Site site, List sectionList) {
        site.setTitle(composeTitle(sectionList));

    }

    protected void updateDescription(Site site) {

    }

    protected List getSectionList(Site site) {
        List sections = new ArrayList();
        String realm = siteService.siteReference(site.getId());
        try {
            AuthzGroup realmEdit = authzGroupService.getAuthzGroup(realm);
            String[] providerIds = groupProvider.unpackId(realmEdit.getProviderGroupId());
            for (int i=0; i < providerIds.length; i++ ) {
                String providerId = providerIds[i];

                Section officialSection = null;
                try {
                    officialSection = courseManagementService.getSection(providerId);
                    sections.add(officialSection);
                } catch (IdNotFoundException ide) {
                    logger.error("Site " + site.getId() + " has a provider id, " + providerId + ", that has no matching section in CM.");
                }

                addSections(sections, site, "site.cm.requested");
                addSections(sections, site, "site-request-course-sections");
            }

        } catch (GroupNotDefinedException e) {
            logger.error("problem finding realm with id:" + realm,e);
        }

        return sections;
    }

    protected void addSections(List sections, Site site, String propName) {
	    String courseListString = StringUtil.trimToNull(site.getProperties().getProperty(propName));
		if (courseListString != null) {
			List courseList = new Vector();
			if (courseListString.indexOf("+") != -1) {
				courseList = new ArrayList(Arrays.asList(courseListString.split("\\+")));
			} else {
				courseList.add(courseListString);
			}

            // need to construct the list of SectionObjects
            for (int i=0; i<courseList.size();i++)
            {
                String courseEid = (String) courseList.get(i);

                try
                {
                Section s = courseManagementService.getSection(courseEid);
                if (s!=null)
                    sections.add(s);
                }
                catch (Exception e)
                {
                    logger.warn(this + ".courseListFromStringIntoContext: cannot find section " + courseEid, e);
                }
            }
		}
    }

	// UCD : SAK-1084
	private String buildTitleStub(String deptCode, String courseCode, String sectionCode, String sessionDescription) {
		StringBuilder builder = new StringBuilder();

		if (deptCode != null && courseCode != null && sectionCode != null)
			builder.append(deptCode).append(" ").append(courseCode).append(" ").append(sectionCode);

		if (sessionDescription != null)
			builder.append(" ").append(sessionDescription);

		return builder.toString();
	}

	/*
	 * 	UCD: SAK-1084
	 *
	 *  The section eid should be in the following format:
	 *	http://ucdavis.edu/course/section/ENL/195H/030/200801
	 */
	private String[] disassembleSectionEid(String sectionEid) {
		String[] fields = sectionEid.split("/");
		String[] items = new String[4];

		// Let's just assume that the last four fields are the ones we want, and that the eid is at least 4 fields long
		if (fields != null && fields.length >= 4) {
			int length = fields.length;

			items[3] = fields[length - 1];
			items[2] = fields[length - 2];
			items[1] = fields[length - 3];
			items[0] = fields[length - 4];
		}

		return items;
	}

    public String composeTitleWithSections(List fullSectionList, AcademicSession academicSession) {
		String academicSessionShortDescription = null;
        if (academicSession != null) {
			academicSessionShortDescription = academicSession.getDescription();
        } else if (fullSectionList.size() > 0) {
            Section section = (Section)fullSectionList.get(0);
            if (section.getCourseOfferingEid() != null) {
                CourseOffering co = courseManagementService.getCourseOffering(section.getCourseOfferingEid());
                if (co != null && co.getAcademicSession() != null) {
                    academicSessionShortDescription = co.getAcademicSession().getDescription();
                }
            }
        }
		StringBuilder ucdCourseTitle = new StringBuilder();
        String courseName =  null;
		boolean isSingleSectionCourse = fullSectionList.size() == 1;

		Map<String, List<String>> multiSectionCourseSections = new HashMap<String, List<String>>();
		Map<String, String> crossListedCourseTitles = new HashMap<String, String>();

        int count = 0;

        Collections.sort(fullSectionList, new Comparator(){
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Section) (o1)).getTitle())
                        .compareTo(((Section) (o2)).getTitle());
            }
        });

		for (Iterator i= fullSectionList.iterator(); i.hasNext(); ) {
            Section so = (Section) i.next();

            if (count++ == 0) {
                courseName = so.getDescription();
            }
            String[] fields = so.getTitle().split("-");
            if (fields.length == 3 )  {

                String deptCode = fields[0];
                String courseCode = fields[1];
                String sectionCode = fields[2];



                String courseTitleStub = buildTitleStub(deptCode, courseCode, sectionCode, null);

                if (isSingleSectionCourse) {
                    // We don't need to do anything more in this case
                    ucdCourseTitle.append(courseTitleStub);
                    break;
                }

                String deptCourseCode = new StringBuilder().append(deptCode).append(" ").append(courseCode).toString();
                List<String> sectionCodes = multiSectionCourseSections.get(deptCourseCode);
                if (null == sectionCodes) {
                    sectionCodes = new LinkedList<String>();
                    multiSectionCourseSections.put(deptCourseCode, sectionCodes);
                    crossListedCourseTitles.put(deptCourseCode, courseTitleStub);
                }

                sectionCodes.add(sectionCode);
            }
		}


        if (multiSectionCourseSections.size() > 0) {
			// There are three different cases (1) single section, (2) multiple sections of the same course, (3) cross-listed course
			for (Iterator<String> it = multiSectionCourseSections.keySet().iterator();it.hasNext();) {
				String deptCourseCode = it.next();
				List<String> sectionCodes = multiSectionCourseSections.get(deptCourseCode);
				if (sectionCodes.size() > 1) {
					ucdCourseTitle.append(deptCourseCode).append(" ").append(constructSectionStub(sectionCodes));
				} else {
					String courseTitleStub = crossListedCourseTitles.get(deptCourseCode);
					ucdCourseTitle.append(courseTitleStub);
				}

				if (it.hasNext())
					ucdCourseTitle.append(delim);
			}
		}

        if (includeCourseName && courseName != null) {
            ucdCourseTitle.append(delim).append(courseName);
        }

		// All course titles need an academic session description at the end
		if (includeTerm && academicSessionShortDescription != null && ucdCourseTitle.length() > 0)
			ucdCourseTitle.append(delim).append(academicSessionShortDescription);

		return ucdCourseTitle.toString();
    }

	// UCD : SAK-1084
	public String composeTitleWithSections(List fullSectionList) {
        return composeTitleWithSections(fullSectionList, null);
	}

    public String composeDescriptionWithSections(List sectionObjectList) {
		String rv = "";

		if(sectionObjectList.size()<1) {
			logger.warn("No Section information available to compose site description");
			return rv;
		}
		// this should be more complicated since there can be sections accross courseofferings but until then
		// the descrption will be the description of the courseoffering of the first section

		CourseOffering co = null;
		Section sec = null;
		Iterator<Section> i = sectionObjectList.iterator();
		while(null==co && i.hasNext()){
			try {
				sec = courseManagementService.getSection(i.next().getEid());
			} catch (IdNotFoundException e){
				logger.error("Cannot get section from courseManagementService: " + ((Section)sectionObjectList.get(0)).getEid() +
					"\n ... this could be a 'fully manually' created site");
			}
			if(sec!=null){
				co = courseManagementService.getCourseOffering(sec.getCourseOfferingEid());
			}
		}
		if(null==co){
			logger.error("Unable to get CourseOffering objects for any sections choosen... using first section's description for site");
			try {
				sec = courseManagementService.getSection(((Section)sectionObjectList.get(0)).getEid());
			} catch (IdNotFoundException e){
				logger.error("Cannot get even the *first* section from courseManagementService: " + ((Section)sectionObjectList.get(0)).getEid() +
						"\n ... this could be a 'fully manually' created site");
			}
			if(sec!=null){
				rv = sec.getDescription();
			}
		} else {
			rv = co.getDescription();
		}

		if(logger.isDebugEnabled()){
			logger.debug("New site description:\n-----\n'" +
					rv + "'\n-----\n");
		}
		return rv;
	}

	private Pattern ordinalPattern = Pattern.compile("/d+");

	private int translateSectionCode(String sectionCode) {
		int ordinal = -1;

		if (sectionCode != null) {
			Matcher m = ordinalPattern.matcher(sectionCode);

			try {
				if (m.matches()) {
					ordinal = Integer.parseInt(sectionCode);
				} else {
					String workingCode = new String(sectionCode);

					workingCode = workingCode.replace('A', '1');
					workingCode = workingCode.replace('B', '2');
					workingCode = workingCode.replace('C', '3');
					workingCode = workingCode.replace('D', '4');
					workingCode = workingCode.replace('E', '5');

					ordinal = Integer.parseInt(workingCode);
				}
			} catch (NumberFormatException nfe) {
				logger.warn("Unable to format " + sectionCode + " as an integer ", nfe);
			}
		}
		return ordinal;
	}

	// UCD: SAK-1084
	private String constructSectionStub(List<String> sectionCodes) {
		StringBuilder stub = new StringBuilder();

		Collections.sort(sectionCodes, new SectionCodeComparator());

		int beginOrdinal = -1;
		int endOrdinal = -1;
		String beginCode = null;
		String endCode = null;
		boolean isContiguous;
		for (Iterator<String> it = sectionCodes.iterator();it.hasNext();) {
			String sectionCode = it.next();

			int sectionCodeOrdinal = translateSectionCode(sectionCode);

			if (sectionCodeOrdinal == -1) {
				// Fall thru. We can't figure out the sequence of this one
				stub.append(sectionCode);
				if (it.hasNext())
					stub.append(",");
			} else {
				if (beginOrdinal == -1) {
					// We're at the beginning
					beginOrdinal = sectionCodeOrdinal;
					endOrdinal = beginOrdinal;
					beginCode = new String(sectionCode);
					endCode = new String(beginCode);
				} else {
					if (sectionCodeOrdinal == endOrdinal + 1) {
						// Keep looking for the end
						endOrdinal = sectionCodeOrdinal;
						endCode = sectionCode;
					} else {
						// We're at the end of the previous list
						if (beginOrdinal + 1 == endOrdinal) {
							// We can use a comma in this case
							stub.append(beginCode).append(",").append(endCode);
						} else if (beginOrdinal == endOrdinal) {
							// This means the previous one is not contiguous with this one, and it had no list
							stub.append(beginCode);
						} else {
							// This means we have to append the previous list
							stub.append(beginCode).append("-").append(endCode);
						}

						stub.append(",");

						beginOrdinal = sectionCodeOrdinal;
						endOrdinal = beginOrdinal;
						beginCode = new String(sectionCode);
						endCode = new String(beginCode);
					}
				}
			}

		}

		if (beginOrdinal == endOrdinal)
			stub.append(beginCode);
		else
			stub.append(beginCode).append("-").append(endCode);



		return stub.toString();
	}

    // UCD : SAK-1084
	public class SectionCodeComparator implements Comparator {

		public int compare(Object o1, Object o2) {

			String s1 = (String)o1;
			String s2 = (String)o2;

			int n1 = translateSectionCode(s1);
			int n2 = translateSectionCode(s2);

			return n1 - n2;
		}

	}


    public void setSiteService(org.sakaiproject.site.api.SiteService siteService) {
        this.siteService = siteService;
    }

    public void setCourseManagementService(CourseManagementService courseManagementService) {
        this.courseManagementService = courseManagementService;
    }

    public boolean isIncludeTerm() {
        return includeTerm;
    }

    public void setIncludeTerm(boolean includeTerm) {
        this.includeTerm = includeTerm;
    }

    public boolean isIncludeCourseName() {
        return includeCourseName;
    }

    public void setIncludeCourseName(boolean includeCourseName) {
        this.includeCourseName = includeCourseName;
    }

    public String getDelim() {
        return delim;
    }

    public void setDelim(String delim) {
        this.delim = delim;
    }

    public AuthzGroupService getAuthzGroupService() {
        return authzGroupService;
    }

    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    public void setGroupProvider(GroupProvider groupProvider) {
        this.groupProvider = groupProvider;
    }
}