/*
 * Copyright 2011 The rSmart Group
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
 * Contributor(s): duffy
 */

package com.rsmart.content.google.helper;

import com.rsmart.content.google.api.GoogleDocsServiceNotConfiguredException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Jan 6, 2011
 * Time: 10:46:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleDocumentHelperErrorController
    extends SimpleMappingExceptionResolver
{
    public static final String
        EXCEPTION = "exception",
        ERROR_VIEW = "error/notConfigured";

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception ex)
    {
        if (!GoogleDocsServiceNotConfiguredException.class.isAssignableFrom(ex.getClass()))
        {
            return super.resolveException(request, response, handler, ex);
        }

        final GoogleDocsServiceNotConfiguredException
            gdsnce = (GoogleDocsServiceNotConfiguredException)ex;

        final HashMap
            model = new HashMap();

        model.put(EXCEPTION, gdsnce);
        
        return new ModelAndView(ERROR_VIEW, model);
    }
}
