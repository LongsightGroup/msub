package com.rsmart.sakaiproject.integration.coursemanagement.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitemanage.api.SectionField;
import org.sakaiproject.sitemanage.api.SiteInfoComposer;
import org.sakaiproject.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * The Default SiteInfoComposer for when system is using course mgmt.
 * <p/>
 * <p/>
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Sep 14, 2009
 * Time: 10:34:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultSiteInfoComposer implements SiteInfoComposer {
    protected final transient Log logger = LogFactory.getLog(getClass());
    public org.sakaiproject.site.api.SiteService siteService = null;
    protected CourseManagementService courseManagementService;
    private boolean includeAcademicSessionInEid;
    private boolean updateSiteTitle = false;

    public void init() {
        includeAcademicSessionInEid = ServerConfigurationService.getBoolean("includeAcademicSessionInEid", true);
    }

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
        List sections = new ArrayList();
        for (Iterator i = sectionFieldsList.iterator(); i.hasNext(); ) {
            List sectionFields = (List) i.next();
            Section section = new SectionCmImpl();

            StringBuffer eid = new StringBuffer();
            eid.append(((SectionField) sectionFields.get(0)).getValue() + "/");
            eid.append(((SectionField) sectionFields.get(1)).getValue() + "/");
            eid.append(((SectionField) sectionFields.get(2)).getValue() + "/");
            if(includeAcademicSessionInEid){
            String[] split = academicSessionEid.split("/");
            eid.append(split[split.length - 1]);
            }
            section.setEid(eid.toString());
            sections.add(section);
        }

        return composeTitleWithSections(sections,
                courseManagementService.getAcademicSession(academicSessionEid));
    }

    public void updateSiteTitle(Site site) {
        if (site != null && updateSiteTitle) {
            site.loadAll();
            if (site.getType().equals("course")) {
                List sectionList = getSectionList(site);
                if (sectionList.size() > 0) {
                    try {
                        site.setTitle(composeTitleWithSections(sectionList));
                        siteService.save(site);
                    } catch (Exception e) {
                        logger.error("error updating title  for site object, site refid=" + site.getId(), e);
                        throw new RuntimeException(e);
                    }

                }
            }
        }
    }

    public String composeDescription(List sectionList) {
        if (sectionList.size() > 0 && sectionList.get(0) instanceof String) {
            return composeDescriptionWithSections(getSectionsFromIds(sectionList));
        }
        return composeDescriptionWithSections(sectionList);
    }

    private List getSectionsFromIds(List<String> sectionList) {
        List sections = new ArrayList();
        for (String sectionId : sectionList) {
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

    protected List getSectionList(Site site) {
        List sections = new ArrayList();
        for (Iterator<Group> iter = site.getGroups().iterator(); iter.hasNext(); ) {
            Group group = iter.next();
            String providerId = group.getProviderGroupId();

            Section officialSection = null;
            try {
                officialSection = courseManagementService.getSection(providerId);
                sections.add(officialSection);
            } catch (IdNotFoundException ide) {
                logger.error("Site " + site.getId() + " has a provider id, " + providerId + ", that has no matching section in CM.");
            }
        }

        addSections(sections, site, "site.cm.requested");
        addSections(sections, site, "site-request-course-sections");
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
            for (int i = 0; i < courseList.size(); i++) {
                String courseEid = (String) courseList.get(i);

                try {
                    Section s = courseManagementService.getSection(courseEid);
                    if (s != null)
                        sections.add(s);
                } catch (Exception e) {
                    logger.warn(this + ".courseListFromStringIntoContext: cannot find section " + courseEid, e);
                }
            }
        }
    }


    public String composeTitleWithSections(List fullSectionList, AcademicSession academicSession) {
        String academicSessionShortDescription = null;
        Section section = null;
        if (fullSectionList != null && fullSectionList.size() > 0) {
            section = (Section) fullSectionList.get(0);
        }

        if (academicSession != null) {
            academicSessionShortDescription = academicSession.getTitle();
        } else if (section != null) {
            if (section.getCourseOfferingEid() != null) {
                CourseOffering co = courseManagementService.getCourseOffering(section.getCourseOfferingEid());
                if (co != null && co.getAcademicSession() != null) {
                    academicSessionShortDescription = co.getAcademicSession().getTitle();
                }
            }
        }

        StringBuilder siteTitle = new StringBuilder();

        if (section != null) {

            if (section.getTitle() == null) {
                siteTitle.append(section.getEid().replaceAll("/", " "));
            } else {
                siteTitle.append(section.getTitle() + " ");

                // All course titles need an academic session description at the end
                if ((academicSessionShortDescription != null && siteTitle.length() > 0) && includeAcademicSessionInEid) {
                    siteTitle.append(" ").append(academicSessionShortDescription);
                }
            }
        }

        return siteTitle.toString();
    }

    public String composeTitleWithSections(List fullSectionList) {
        return composeTitleWithSections(fullSectionList, null);
    }

    public String composeDescriptionWithSections(List sectionObjectList) {
        String rv = "";

        if (sectionObjectList.size() < 1) {
            logger.warn("No Section information available to compose site description");
            return rv;
        }
        // this should be more complicated since there can be sections accross courseofferings but until then
        // the descrption will be the description of the courseoffering of the first section

        CourseOffering co = null;
        Section sec = null;
        Iterator<Section> i = sectionObjectList.iterator();
        while (null == co && i.hasNext()) {
            try {
                sec = courseManagementService.getSection(((Section) i.next()).getEid());
            } catch (IdNotFoundException e) {
                logger.error("Cannot get section from courseManagementService: " + ((Section) sectionObjectList.get(0)).getEid() +
                        "\n ... this could be a 'fully manually' created site");
            }
            if (sec != null) {
                co = courseManagementService.getCourseOffering(sec.getCourseOfferingEid());
            }
        }
        if (null == co) {
            logger.error("Unable to get CourseOffering objects for any sections choosen... using first section's description for site");
            try {
                sec = courseManagementService.getSection(((Section) sectionObjectList.get(0)).getEid());
            } catch (IdNotFoundException e) {
                logger.error("Cannot get even the *first* section from courseManagementService: " + ((Section) sectionObjectList.get(0)).getEid() +
                        "\n ... this could be a 'fully manually' created site");
            }
            if (sec != null) {
                rv = sec.getDescription();
            }
        } else {
            rv = co.getDescription();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("New site description:\n-----\n'" +
                    rv + "'\n-----\n");
        }
        return rv;
    }


    public void setCourseManagementService(CourseManagementService courseManagementService) {
        this.courseManagementService = courseManagementService;
    }

    public void setIncludeAcademicSessionInEid(boolean includeAcademicSessionInEid) {
        this.includeAcademicSessionInEid = includeAcademicSessionInEid;
    }

    public void setUpdateSiteTitle(boolean updateSiteTitle) {
        this.updateSiteTitle = updateSiteTitle;
    }
}
