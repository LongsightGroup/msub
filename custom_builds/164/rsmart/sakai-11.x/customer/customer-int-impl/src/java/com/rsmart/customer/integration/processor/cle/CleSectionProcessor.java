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

package com.rsmart.customer.integration.processor.cle;

import com.rsmart.customer.integration.model.CleSection;
import com.rsmart.customer.integration.processor.BaseCsvFileProcessor;
import com.rsmart.customer.integration.processor.ProcessorState;
import com.rsmart.customer.integration.util.SiteHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.exception.IdUnusedException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 28, 2008
 * Time: 9:21:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class CleSectionProcessor extends BaseCsvFileProcessor {
    private static final Log logger = LogFactory.getLog(CleSectionProcessor.class);
    private SectionManager sectionManager;
    private SiteHelper siteHelper;
    private SiteService siteService;
    private boolean deleteSections;

    public SectionManager getSectionManager() {
        return sectionManager;
    }

    public void setSectionManager(SectionManager sectionManager) {
        this.sectionManager = sectionManager;
    }

    public String getProcessorTitle() {
        return "SIS Section Processor";
    }

    public ProcessorState init(Map config)
    {
        ProcessorState
            state = super.init(config);

        state.getConfiguration().put("sections", new HashMap());

        return state;
    }

    public void preProcess(ProcessorState state) throws Exception
    {
        Map
            sections = (Map)state.getConfiguration().get("sections");

        sections.clear();
    }

    public void postProcess(ProcessorState state) throws Exception
    {
        logger.debug("postProcess()");

        Map
            sections = (Map)state.getConfiguration().get("sections");

        for (Iterator<String> i= sections.keySet().iterator(); i.hasNext();) {
            String courseEid = i.next();
            List currentSections = (List) sections.get(courseEid);
            Site site = siteHelper.findSite(courseEid);
            List addedSections = new ArrayList();

            for (Iterator<CleSection> j= currentSections.iterator(); j.hasNext();) {
                CleSection section = j.next();

                if (site == null) {
                    logger.error("can't find a site with an " + SiteHelper.EXTERNAL_SITE_ID +  " of [" + section.getCourseEid() +
                            "], skipping section record with sectionEid = [" + section.getSectionEid() + "]");
                    continue;
                }

                if (!sectionExists(site, addedSections, section)){

                    CourseSection newSection = sectionManager.addSection(site.getReference(), section.getName(), section.getCategory(), section.getMaxEnrollments(), "", null, null, false, false, false, false, false, false,false);
                    logger.debug("created section: " + newSection.getUuid() + " from " + section.toString());
                    addedSections.add(newSection.getUuid());

                    try {
                        setExternalSectionId(section, site, newSection);
                    } catch(Exception e) {
                        logger.error("error updating the externalSectionId for section:" + section.toString(), e);
                    }
                }

                updateProperties(site, section);
            }
            //remove any sections for this site, not specified in the csv file
            deleteSections(site, addedSections);
        }
        sections.clear();
    }

    protected void updateProperties(Site site, CleSection section) {
        String[] propertyNames = org.sakaiproject.component.cover.ServerConfigurationService.getStrings("section.sis.property");
        if (propertyNames != null && propertyNames.length > 0) {
            //reload site, this is Sakai dude, its always recommended
            try {
                site = siteService.getSite(site.getId());
            } catch (IdUnusedException e) {
                logger.error("can't find site with id:" + site.getId(), e);
            }

            for (Iterator<Group> k=site.getGroups().iterator(); k.hasNext(); ) {
                Group group = k.next();
                if (group.getProperties().getProperty("externalSectionId") != null &&
                        group.getProperties().getProperty("externalSectionId").equals(section.getSectionEid())){

                    if (section.getProperty1() != null) {
                        group.getProperties().addProperty(propertyNames[0], section.getProperty1());
                    }
                    if (section.getProperty2() != null) {
                        group.getProperties().addProperty(propertyNames[1], section.getProperty2());
                    }
                    if (section.getProperty3() != null) {
                        group.getProperties().addProperty(propertyNames[2], section.getProperty3());
                    }
                    if (section.getProperty4() != null) {
                        group.getProperties().addProperty(propertyNames[3], section.getProperty4());
                    }
                    if (section.getProperty5() != null) {
                        group.getProperties().addProperty(propertyNames[4], section.getProperty5());
                    }

                    try {
                        siteService.save(site);
                    } catch (Exception e) {
                        logger.error("problem updating group properties: " + e.getMessage(), e);
                    }

                    logger.debug("updated additional properties for section: " + section.toString());
                    return;
                }
            }
        } else if (StringUtils.isNotBlank(section.getProperty1()) || StringUtils.isNotBlank(section.getProperty2()) ||
                StringUtils.isNotBlank(section.getProperty3()) || StringUtils.isNotBlank(section.getProperty4()) ||
                StringUtils.isNotBlank(section.getProperty5())){
            logger.warn("not updating section properties and section property data detected, did you forget to set 'section.sis.property' correctly?");
        }

    }

    protected Group getSectionGroup(Site site, CleSection section) {
        for (Iterator<Group> k=site.getGroups().iterator(); k.hasNext(); ) {
            Group group = k.next();
            // section exists already, skip
            if (group.getProperties().getProperty("externalSectionId") != null &&
                    group.getProperties().getProperty("externalSectionId").equals(section.getSectionEid())) {
                return group;
            }
        }
        return null;
    }

    protected boolean sectionExists(Site site, List addedSections, CleSection section) {
        for (Iterator<Group> k=site.getGroups().iterator(); k.hasNext(); ) {
            Group group = k.next();
            // section exists already, skip
            if (group.getProperties().getProperty("externalSectionId") != null &&
                    group.getProperties().getProperty("externalSectionId").equals(section.getSectionEid())) {
                addedSections.add(group.getReference());
                logger.debug("skipping existing section: " + section.toString());
                return true;
            }
        }
        return false;
    }

    private void deleteSections(Site site, List addedSections) {
        if (deleteSections) {
            List<CourseSection> existingSections = sectionManager.getSections(site.getId());
            logger.debug("found " + existingSections.size() + " existing sections in site " + site.getId());
            for (Iterator<CourseSection> j= existingSections.iterator();j.hasNext();) {
                CourseSection courseSection = j.next();
                if (!addedSections.contains(courseSection.getUuid())){
                    logger.debug("disbanding section: " + courseSection.getUuid());
                    try {
                        sectionManager.disbandSection(courseSection.getUuid());
                    } catch (Exception e) {
                        logger.error("problem disbanding section: " + courseSection.getUuid(), e);
                    }
                }
            }
        }
    }

    protected boolean sectionExists(Site site, CleSection section){

        return false;
    }

    protected void setExternalSectionId(CleSection section, Site site, CourseSection newSection) throws Exception {
        //reload site, and set externalSectionid
        site = siteService.getSite(site.getId());
        for (Iterator<Group> k=site.getGroups().iterator(); k.hasNext(); ) {
            Group group = k.next();
            if (newSection.getUuid().equals(group.getReference())){
                group.getProperties().addProperty("externalSectionId", section.getSectionEid());
                logger.debug("adding externalSectionId property to group:" + group.getId());
                siteService.save(site);
                return;
            }
        }
        logger.error("created section with id=[" + newSection.getUuid() + "] but couldn't find group with same id to set externalSectionId property");
    }

    public void processRow(String[] data, ProcessorState state)
        throws Exception
    {

        CleSection section = new CleSection();
        section.setSectionEid(data[0]);
        section.setName(data[1]);
        section.setCategory(data[2]);
        section.setCourseEid(data[3]);

        try {
            Integer.parseInt(data[4]);
            section.setMaxEnrollments(new Integer(data[4]));
        } catch (NumberFormatException e) {
            logger.error("invalid maxenrollments value of [" + data[4]+ "] for section with eid of [" + data[0] +
                    "], setting max enrollment to default value");
            return;
        }


        if (data.length > 5){
            section.setProperty1(data[5]);
        }
        if (data.length > 6){
            section.setProperty2(data[6]);
        }
        if (data.length > 7){
            section.setProperty3(data[7]);
        }
        if (data.length > 8){
            section.setProperty4(data[8]);
        }
        if (data.length > 9){
            section.setProperty5(data[9]);
        }

        Site site = siteHelper.findSite(section.getCourseEid());

        if (site == null) {
            throw new Exception("can't find a site with an " + SiteHelper.EXTERNAL_SITE_ID +  " of [" + section.getCourseEid() +
                    "], skipping section record with sectionEid = [" + section.getSectionEid() + "]");
        }

        section.setSiteReference(site.getReference());

        logger.debug("queuing section: " + section.toString());
        addSection(section.getCourseEid(), section, state);
    }

    protected void addSection(String courseEid, CleSection section, ProcessorState state) {
        Map
            sections = (Map)state.getConfiguration().get("sections");

        List sectionList = (List) sections.get(courseEid);
        if (sectionList == null) {
            sectionList = new ArrayList();
            sections.put(courseEid, sectionList);
        }
        sectionList.add(section);

    }

    public SiteHelper getSiteHelper() {
        return siteHelper;
    }

    public void setSiteHelper(SiteHelper siteHelper) {
        this.siteHelper = siteHelper;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public boolean isDeleteSections() {
        return deleteSections;
    }

    public void setDeleteSections(boolean deleteSections) {
        this.deleteSections = deleteSections;
    }
}

