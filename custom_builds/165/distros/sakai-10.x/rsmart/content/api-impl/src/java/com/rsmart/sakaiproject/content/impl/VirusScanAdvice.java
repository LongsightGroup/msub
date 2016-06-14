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

package com.rsmart.sakaiproject.content.impl;

import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResourceEdit;

import com.rsmart.antivirus.VirusScanner;

/**
 * Scans any added resource for viruses by intercepts calls commitResource in the ContentHostingService
 * 
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Aug 14, 2007
 * Time: 1:50:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class VirusScanAdvice implements MethodBeforeAdvice {
    private static final Log logger = LogFactory.getLog(VirusScanAdvice.class);

    private VirusScanner virusScanner;
    public void before(Method method, Object[] args, Object target) throws Throwable {
        ContentResourceEdit edit = (ContentResourceEdit) args[0];
        getVirusScanner().scan(edit.getContent());
    }


    public VirusScanner getVirusScanner() {
        return virusScanner;
    }

    public void setVirusScanner(VirusScanner virusScanner) {
        this.virusScanner = virusScanner;
    }
}
