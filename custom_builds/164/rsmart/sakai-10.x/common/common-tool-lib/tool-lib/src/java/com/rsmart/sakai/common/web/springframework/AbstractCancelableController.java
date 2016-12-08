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
package com.rsmart.sakai.common.web.springframework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractWizardFormController;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.beans.propertyeditors.CustomDateEditor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.text.SimpleDateFormat;



/**
 *
 */
abstract public class AbstractCancelableController extends SimpleFormController {
   protected final Log logger = LogFactory.getLog(getClass());

   protected boolean suppressValidation(HttpServletRequest request) {
      if (!isCancel(request)) {
         return super.suppressValidation(request);
      }
      return true;
   }

   protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
      binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy/MM/dd"), true));
   }

   protected ModelAndView onSubmit(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Object command,
                                   BindException errors)
                            throws Exception {
      if (!isCancel(request)) {
         doSubmitAction(command);
      }
      return new ModelAndView(getSuccessView());
   }

   protected boolean isCancel(HttpServletRequest request) {
      return (request.getParameter(AbstractWizardFormController.PARAM_CANCEL) != null);
   }
}
