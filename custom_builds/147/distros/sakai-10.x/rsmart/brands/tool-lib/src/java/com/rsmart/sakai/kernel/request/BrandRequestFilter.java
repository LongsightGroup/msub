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

package com.rsmart.sakai.kernel.request;

import com.rsmart.sakai.brand.BrandService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import java.io.IOException;

/**
 * sets the brand by first looking in the request for a sakai.brand parameter.  If that fails is looks in the session,
 * otherwise the default brand is set.
 *
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jul 31, 2006
 * Time: 9:35:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class BrandRequestFilter extends RequestFilter {
   private static Log logger = LogFactory.getLog(BrandRequestFilter.class);
   private BrandService brandService;

   public void doFilter(ServletRequest requestObj, ServletResponse responseObj, FilterChain chain) throws IOException, ServletException {
      HttpServletRequest req = (HttpServletRequest) requestObj;
		HttpServletResponse resp = (HttpServletResponse) responseObj;

      // check on file uploads and character encoding BEFORE checking if
      // this request has already been filtered, as the character encoding
      // and file upload handling are configurable at the tool level.
      // so the 2nd invokation of the RequestFilter (at the tool level)
      // may actually cause character encoding and file upload parsing
      // to happen.

      // handle character encoding
      handleCharacterEncoding(req, resp);
      if (requestObj.getParameter(BrandService.BRAND_KEY) != null) {
         ThreadLocalManager.set(BrandService.BRAND_KEY, requestObj.getParameter(BrandService.BRAND_KEY));
         Cookie brandCookie = new Cookie(BrandService.BRAND_KEY, requestObj.getParameter(BrandService.BRAND_KEY));
         resp.addCookie(brandCookie);
      } else {
         Cookie[] cookies = req.getCookies();
         if (cookies != null) {
            for (int i=0;i<cookies.length; i++){
               Cookie cookie = cookies[i];
               if (cookie.getName().equals(BrandService.BRAND_KEY)){
                  ThreadLocalManager.set(BrandService.BRAND_KEY, cookie.getValue());
               }
            }
         }
      }

      super.doFilter(requestObj, responseObj, chain);
   }

   public void init(FilterConfig config) throws ServletException {
      super.init(config);
      setBrandService((BrandService) ComponentManager.get(BrandService.class));
   }

   public BrandService getBrandService() {
      return brandService;
   }

   public void setBrandService(BrandService brandService) {
      this.brandService = brandService;
   }
}
