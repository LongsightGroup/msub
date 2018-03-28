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

package com.rsmart.customer.integration.util;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 28, 2008
 * Time: 11:45:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class SiteHelper {
    private static Log logger = LogFactory.getLog(SiteHelper.class);

    private SiteService siteService;
    /**
     * caches externalSiteId/siteId map.  Doesn't cache the site, just the lookup of its id.
     * This way we default to the normal sakai cache for site caching. The cache supports nulls
     * which would indicate there is no site matches the given key.
     */
    private Cache externalSiteIdCache;
    private SqlService sqlService;

    public static final String EXTERNAL_SITE_ID = "externalSiteId";
    public static final String LTI_CONTEXT_ID = "lti_context_id";

    public void init(){
        externalSiteIdCache.setStatisticsEnabled(true);
    }

    /**
	 * Find Site - Returns NULL if not found
	 *
	 * @param externalOaeId
	 * @return Site
	 */
	public Site findSiteByLTIContextId(String externalOaeId) throws Exception {
        Map propertyCriteria = new HashMap();

		// Replace search property
		propertyCriteria.put(LTI_CONTEXT_ID, externalOaeId);

		List list = siteService.getSites(SiteService.SelectionType.ANY, null, null,
				propertyCriteria, SiteService.SortType.NONE, null);

		if (list != null && list.size() > 0) {
            for (Iterator i=list.iterator(); i.hasNext();) {
                Site site = (Site) i.next();
                if (site.getProperties() != null) {
                    String loadedExternalSiteId = (String) site.getProperties().get(LTI_CONTEXT_ID);
                    if (loadedExternalSiteId != null && loadedExternalSiteId.equals(externalOaeId)) {
                        // deeply load site, otherwise groups won't be loaded
                        return siteService.getSite(site.getId());
                    }
                }
            }
        }

		return null;
	}

    public void invalidateCache(String externalSiteId) {
        Element element;
        if ((element = externalSiteIdCache.get(externalSiteId)) != null ){
            externalSiteIdCache.removeElement(element);
        }
    }

    /**
	 * Find Site - Returns NULL if not found
	 *
	 * @param externalSiteId
	 * @return Site
	 */
	public Site findSite(String externalSiteId) throws Exception {
        Element element;
        if ((element = externalSiteIdCache.get(externalSiteId)) != null ){

            // we could cache unfound ids, be sure to handle that
            if (element.getValue() == null) {
                logger.debug("no site found by externalSiteId:" + externalSiteId + " in cache");
                debugCache();
                return null;
            }

            String siteId = (String) element.getValue();
            logger.debug("found site [" + siteId + "] by externalSiteId:" + externalSiteId + " in cache.");
            debugCache();
            return siteService.getSite(siteId);
        } else {
            logger.debug("site with externalSiteId:" + externalSiteId + " not found in cache");
        }

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sql = "SELECT sakai_site.site_id FROM sakai_site,sakai_site_property WHERE "
                        + "sakai_site.site_id            = sakai_site_property.site_id "
                        + "and sakai_site_property.name  = 'externalSiteId' ";
        if (sqlService.getVendor().equals("oracle")){
            sql = sql + "and to_char(sakai_site_property.value) = ? ";
        }else{
            sql = sql + "and sakai_site_property.value = ? ";
        }

        try {
            con = sqlService.borrowConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, externalSiteId);
            rs = ps.executeQuery();
            if (rs.next()) {
                String siteId = rs.getString(1);
                logger.debug("found site with id:" + siteId + " by externalSiteId:" + externalSiteId+ " in database");
                Site loadedSite =  siteService.getSite(siteId);
                storeInCache(externalSiteId, siteId);
                return loadedSite;
            }

            // put unfound entries in cache too
            storeInCache(externalSiteId, null);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                }
            }

        }

		return null;
	}

    protected void debugCache() {
        if (logger.isDebugEnabled()) {
            final StringBuilder buf = new StringBuilder();
            final long hits = externalSiteIdCache.getStatistics().getCacheHits();
            final long misses = externalSiteIdCache.getStatistics().getCacheMisses();
            final long total = hits + misses;
            final long hitRatio = ((total > 0) ? ((100l * hits) / total) : 0);
            buf.append(externalSiteIdCache.getName() + ": " +
                    " count:" + externalSiteIdCache.getStatistics().getObjectCount() +
                    " hits:" + hits +
                    " misses:" + misses +
                    " hit%:" + hitRatio);

            logger.debug(buf.toString());
        }
    }

    protected void storeInCache(String externalSiteId, String siteId) {
        externalSiteIdCache.put(new Element(externalSiteId, siteId));
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setExternalSiteIdCache(Cache externalSiteIdCache) {
        this.externalSiteIdCache = externalSiteIdCache;
    }

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }
}
