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

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jul 31, 2006
 * Time: 10:09:14 AM
 * To change this template use File | Settings | File Templates.
 */
public interface BrandService {
   /**
    *  if this key is in the request the brand will be set to its value
    */
   public static final String BRAND_KEY = "sakai.brand";
   public static final String CURRENT_BRAND = "current.brand";
   public static final String DEFAULT_BRAND = "default";
   public String getCurrentBrandName();
   public String getDefaultBrandName();
   public Map getBrand(String brandName);
   public Map getCurrentBrand();
       
   //public void setCurrentBrand(HttpServletRequest httpServletRequest, Session session);

    public boolean isBrandIsSkin();
}
