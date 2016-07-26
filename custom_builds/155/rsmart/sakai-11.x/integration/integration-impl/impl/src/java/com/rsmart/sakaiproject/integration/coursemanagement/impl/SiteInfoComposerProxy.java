package com.rsmart.sakaiproject.integration.coursemanagement.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitemanage.api.SiteInfoComposer;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 5/19/11
 * Time: 10:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class SiteInfoComposerProxy implements SiteInfoComposer {
    private static Log log = LogFactory.getLog(SiteInfoComposerProxy.class);
    private SiteInfoComposer siteInfoComposer;
    private String siteInfoComposerBeanName;

    public void init() {
        if (siteInfoComposerBeanName == null) {
            siteInfoComposerBeanName = ServerConfigurationService.getString("siteInfoComposerBeanName@com.rsmart.sakaiproject.integration.coursemanagement.impl.SiteInfoComposerProxy");
        }

        // if we have a siteInfoComposerBeanName use it, else will fall back on Dummy that is wired up by default
        if (siteInfoComposerBeanName != null && siteInfoComposerBeanName.length() > 0) {
            try {
                siteInfoComposer = (SiteInfoComposer) ComponentManager.get(siteInfoComposerBeanName);
            } catch (Exception e) {
                log.error("problem finding SiteInfoComposer from :" + siteInfoComposerBeanName, e);
            }
        }
    }

    public String composeTitle(Site site) {
        return siteInfoComposer.composeTitle(site);
    }

    public String composeTitle(String academicSessionEid, List sectionFields) {
        return siteInfoComposer.composeTitle(academicSessionEid, sectionFields);
    }

    public void updateSiteTitle(Site site) {
        siteInfoComposer.updateSiteTitle(site);
    }

    public String composeTitle(List sections) {
        return siteInfoComposer.composeTitle(sections);
    }

    public String composeDescription(List sections) {
        return siteInfoComposer.composeDescription(sections);
    }

    public void setSiteInfoComposer(SiteInfoComposer siteInfoComposer) {
        this.siteInfoComposer = siteInfoComposer;
    }



    public void setSiteInfoComposerBeanName(String siteInfoComposerBeanName) {
        this.siteInfoComposerBeanName = siteInfoComposerBeanName;
    }
}
