package org.sakaiproject.sitemanage.api;

import org.sakaiproject.site.api.Site;

import java.util.List;
/**
 * SiteAction will optionally (if an implementation is present)
 * use this during Site creation to set the title
 * and description based on selected section data.
 *
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Oct 1, 2009
 * Time: 2:05:48 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SiteInfoComposer {
    /**
     * return back the intented site title, its the callers responsibility to save the site
     * if that is intended.
     * @param site
     * @return
     */
    public String composeTitle(Site site);

    public String composeTitle(String academicSessionEid, List<SectionField> sectionFields);


    /**
     * update the site title
     * @param site
     */
    public void updateSiteTitle(Site site);

    /**
     *
     * @param sections - list of selected section eids
     * @return
     */
    public String composeTitle(List sections);

    /**
     *
     * @param sections - list of selected section eids
     * @return
     */
    public String composeDescription(List sections);
}
