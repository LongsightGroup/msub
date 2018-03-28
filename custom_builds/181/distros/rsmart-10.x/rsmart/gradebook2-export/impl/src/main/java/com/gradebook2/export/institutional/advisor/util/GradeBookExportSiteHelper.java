package com.gradebook2.export.institutional.advisor.util;


import org.sakaiproject.gradebook.gwt.sakai.InstitutionalAdvisor;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import java.util.*;

/**
 * This class is a helper class to process gradebookexport for the InstitutionalAdvisor-SGUInstitutionAdvisor
 *
 *
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Feb 1, 2011
 * Time: 10:48:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class GradeBookExportSiteHelper {



    private SiteService siteService;
    public static final String EXTERNAL_SITE_ID = "externalSiteId";

    /**
	 * Find Site - Returns NULL if not found
	 *
	 * @param externalSiteId
	 * @return Site
	 */
	public  Site findSite(String externalSiteId) throws Exception {
        Map propertyCriteria = new HashMap();
    	// Replace search property
		propertyCriteria.put(EXTERNAL_SITE_ID, externalSiteId);

		List list = siteService.getSites(SiteService.SelectionType.ANY, null, null,
				propertyCriteria, SiteService.SortType.NONE, null);

		if (list != null && list.size() > 0) {
            for (Iterator i=list.iterator(); i.hasNext();) {
                Site site = (Site) i.next();
                if (site.getProperties() != null) {
                    String loadedExternalSiteId = (String) site.getProperties().get(EXTERNAL_SITE_ID);
                    if (loadedExternalSiteId != null && loadedExternalSiteId.equals(externalSiteId)) {
                        // deeply load site, otherwise groups won't be loaded
                        return siteService.getSite(site.getId());
                    }
                }
            }
        }

		return null;
	}

    /**
     * This method gives you the users only for a specific group/section section to be
     * processed and imported into a .csv file.
     *
     *
     * @param totalStudents
     * @param groupStudents
     * @return
     */
    public List getUserGroupData(List<Map<InstitutionalAdvisor.Column,String>> totalStudents, Set groupStudents){
		Set users = new HashSet<String>();
        List groupStudentList = new ArrayList();
		for (Map<InstitutionalAdvisor.Column, String> studentData : totalStudents) {
           Iterator<String>itrGroupStudents = groupStudents.iterator();
           while ( itrGroupStudents.hasNext()){
           if (studentData.containsValue(itrGroupStudents.next()) ){
        	   groupStudentList.add(studentData);
        	   break;
           	}
           }
		}
		return groupStudentList;
	}



    //Spring injectors
    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }



}
