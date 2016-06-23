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

package com.rsmart.sakai.brand;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.springframework.web.servlet.ThemeResolver;
import org.sakaiproject.component.cover.ServerConfigurationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jul 31, 2006
 * Time: 10:07:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class BrandServiceImpl implements BrandService {
    private SessionManager sessionManager;
    private String brandHome;
    private static Log logger = LogFactory.getLog(BrandServiceImpl.class);
    private Map brands = new HashMap();
    private boolean disabled = false;
    private String defaultBrand = BrandService.DEFAULT_BRAND;
    private boolean brandIsSkin = false;

    public String getCurrentBrandName() {
       String currentBrand = (String) ThreadLocalManager.get(BrandService.BRAND_KEY);
       Session session = sessionManager.getCurrentSession();
  
       // look in request first
       if (currentBrand != null && currentBrand.length() > 0 ){
          session.setAttribute(BrandService.CURRENT_BRAND, currentBrand);
          logger.info("setting brand to: " + currentBrand);
          return currentBrand;
       }
       // look in session
        if (session.getAttribute(BrandService.CURRENT_BRAND) != null &&
                ((String) session.getAttribute(BrandService.CURRENT_BRAND)).trim().length() > 0) {
            return (String) session.getAttribute(BrandService.CURRENT_BRAND);
        }
        return getDefaultBrandName();
    }

    public String getDefaultBrandName() {
    	String skin = ServerConfigurationService.getString("skin.default");
        String templates = ServerConfigurationService.getString("portal.templates", "neoskin");
        String prefix = ServerConfigurationService.getString("portal.neoprefix", "neo-");
        if ("neoskin".equals(templates)) skin = prefix + skin;
        
        if (isBrandIsSkin()) return skin;
        return defaultBrand;
    }

    public void setDefaultBrand(String defaultBrand) {
        this.defaultBrand = defaultBrand;
    }

    public Map getBrand(String brandName) {
        if (disabled) return new HashMap();

        if (brands.get(brandName) != null) {
            return (Map) brands.get(brandName);
        }
        if (!isBrandIsSkin()) {
         return loadBrand(brandName);
        }
        return new HashMap();
    }

    protected Map loadBrand(String brandName) {
        FileInputStream fis = null;
        String brandFile = brandHome + brandName + ".properties";

        try {
            fis = new FileInputStream(brandFile);
            Properties props = new Properties();
            props.load(fis);
            brands.put(brandName, props);
           return props;
        } catch (FileNotFoundException e) {
            logger.error("can't find brand file: " + brandFile);
        } catch (IOException e) {
            logger.error("can't open brand file: " + brandFile);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
        return new HashMap();
    }

    public Map getCurrentBrand() {
        return getBrand(getCurrentBrandName());
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public String resolveThemeName(HttpServletRequest httpServletRequest) {
        return getCurrentBrandName();
    }

    public void setThemeName(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String string) {
        throw new UnsupportedOperationException();
    }

    public String getBrandHome() {
        return brandHome;
    }

    public void setBrandHome(String brandHome) {
        if (!brandHome.endsWith(File.separator)) {
            brandHome = brandHome + File.separator;
        }
        this.brandHome = brandHome;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isBrandIsSkin() {
        return brandIsSkin;
    }

    public void setBrandIsSkin(boolean brandIsSkin) {
        this.brandIsSkin = brandIsSkin;
    }
}
