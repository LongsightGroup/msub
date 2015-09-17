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

package com.rsmart.admin.customizer.tool;

import com.rsmart.admin.customizer.api.CustomizerService;
import com.rsmart.admin.customizer.api.CustomizerProcessingException;
import com.rsmart.admin.customizer.api.CustomizerRun;
import com.rsmart.admin.customizer.util.*;
import org.springframework.web.servlet.mvc.AbstractWizardFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.exception.ServerOverloadException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerFactory;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.Hashtable;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Apr 3, 2007
 * Time: 9:07:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class CustomizerController extends AbstractWizardFormController {

   private CustomizerService customizerService;
   private int pageBeforeHelper = 0;
   private ResourceLoader rl = new ResourceLoader("messages");

   final public static int START = 0;
   final public static int SELECT_SPREADSHEET = 1;
   final public static int REVIEW_WARNINGS = 2;
   final public static int FINISHED = 3;

   /**
    * Template method for processing the final action of this wizard.
    * <p>Call <code>errors.getModel()</code> to populate the ModelAndView model
    * with the command and the Errors instance, under the specified command name,
    * as expected by the "spring:bind" tag.
    * <p>You can call the <code>showPage</code> method to return back to the wizard,
    * in case of last-minute validation errors having been found that you would
    * like to present to the user within the original wizard form.
    *
    * @param request  current HTTP request
    * @param response current HTTP response
    * @param command  form object with the current wizard state
    * @param errors   validation errors holder
    * @return the finish view
    * @throws Exception in case of invalid state or arguments
    * @see org.springframework.validation.Errors
    * @see org.springframework.validation.BindException#getModel
    * @see #showPage(javax.servlet.http.HttpServletRequest, org.springframework.validation.BindException, int)
    */
   protected ModelAndView processFinish(HttpServletRequest request, HttpServletResponse response,
                                        Object command, BindException errors) throws Exception {
      return null;
   }

   /**
    * Post-process the given page after binding and validation, potentially
    * updating its command object. The passed-in request might contain special
    * parameters sent by the page.
    * <p>Only invoked when displaying another page or the same page again,
    * not when finishing or cancelling.
    *
    * @param request current HTTP request
    * @param command form object with request parameters bound onto it
    * @param errors  validation errors holder
    * @param page    number of page to post-process
    * @throws Exception in case of invalid state or arguments
    */
   protected void postProcessPage(HttpServletRequest request, Object command, Errors errors, int page) throws Exception {
      CustomizerForm form = (CustomizerForm) command;

      if (page == SELECT_SPREADSHEET) {
         List refs = (List) request.getSession().getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
         if (refs.size() == 0) {
            errors.reject("file_required", "You must choose a spreadsheet file");
         }
         else {
            Reference ref = (Reference) refs.get(0);

            try {
               form.setFileId(ref.getId());
               preProcessSpreadsheetInternal(ref.getId(), getCustomizerDir(), false);
               form.setException(new CustomizerProcessingException());
            }
            catch(CustomizerProcessingException exp) {
               form.setException(exp);
            }
            form.setLastModifiedDir(getCustomizerDir().lastModified());
            getCustomizerService().processXml(getCustomizerDir(), false, form.getException());
         }
      }
      else if (page == REVIEW_WARNINGS) {
         getCustomizerService().processXml(getCustomizerDir(), true, form.getException(), form.getFileId());
      }

      super.postProcessPage(request, command, errors, page);
   }

   protected File getCustomizerDir() {
      File dir = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");
      return new File(dir, "customizer");
   }
   
   /**
    * Return the initial page of the wizard, i.e. the page shown at wizard startup.
    * Default implementation delegates to <code>getInitialPage(HttpServletRequest)</code>.
    *
    * @param request current HTTP request
    * @param command the command object as returned by formBackingObject
    * @return the initial page number
    * @see #getInitialPage(javax.servlet.http.HttpServletRequest)
    * @see #formBackingObject
    */
   protected int getInitialPage(HttpServletRequest request, Object command) {
      return super.getInitialPage(request, command);
   }

   /**
    * Create a reference data map for the given request, consisting of
    * bean name/bean instance pairs as expected by ModelAndView.
    * <p>Default implementation delegates to referenceData(HttpServletRequest, int).
    * Subclasses can override this to set reference data used in the view.
    *
    * @param request current HTTP request
    * @param command form object with request parameters bound onto it
    * @param errors  validation errors holder
    * @param page    current wizard page
    * @return a Map with reference data entries, or <code>null</code> if none
    * @throws Exception in case of invalid state or arguments
    * @see #referenceData(javax.servlet.http.HttpServletRequest, int)
    * @see org.springframework.web.servlet.ModelAndView
    */
   protected Map referenceData(HttpServletRequest request, Object command, Errors errors, int page) throws Exception {
      if (page == SELECT_SPREADSHEET) {
         request.getSession().setAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS,
            FilePickerHelper.CARDINALITY_SINGLE);
         request.getSession().setAttribute("sakai.filepicker" + Tool.HELPER_DONE_URL,
            "customizer.osp?_target2=blah&helper=done");
         request.getSession().setAttribute(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT,
            rl.getString("FILE_PICKER_INSTRUCTION_TEXT"));
         request.getSession().setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT,
            rl.getString("FILE_PICKER_TITLE_TEXT"));
         request.getSession().setAttribute(FilePickerHelper.FILE_PICKER_SUBTITLE_TEXT,
            rl.getString("FILE_PICKER_SUBTITLE_TEXT"));
      }
      else if (page == START) {
         List<CustomizerRun> runs = getCustomizerService().listEvents();
         logger.info(runs.size() + "");
         Map model = super.referenceData(request, command, errors, page);
         if (model == null) {
            model = new Hashtable();
         }
         model.put("runHistory", runs);
         return model;
      }

      return super.referenceData(request, command, errors, page);
   }

   /**
    * Return the target page specified in the request.
    * <p>Default implementation delegates to
    * <code>getTargetPage(HttpServletRequest, int)</code>.
    * Subclasses can override this for customized target page determination.
    *
    * @param request     current HTTP request
    * @param command     form object with request parameters bound onto it
    * @param errors      validation errors holder
    * @param currentPage the current page, to be returned as fallback
    *                    if no target page specified
    * @return the page specified in the request, or current page if not found
    * @see #getTargetPage(javax.servlet.http.HttpServletRequest, int)
    */
   protected int getTargetPage(HttpServletRequest request, Object command, Errors errors, int currentPage) {
      if (request.getSession().getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) != null) {
         request.getSession().removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
         return pageBeforeHelper;
      }

      pageBeforeHelper = currentPage;
      return super.getTargetPage(request, command, errors, currentPage);
   }

   /**
    * Consider an explicit finish or cancel request as a form submission too.
    *
    * @see #isFinishRequest(javax.servlet.http.HttpServletRequest)
    * @see #isCancelRequest(javax.servlet.http.HttpServletRequest)
    */
   protected boolean isFormSubmission(HttpServletRequest request) {
      if (request.getParameter("helper") != null) {
         return true;
      }
      return super.isFormSubmission(request);    //To change body of overridden methods use File | Settings | File Templates.
   }

   public CustomizerService getCustomizerService() {
      return customizerService;
   }

   public void setCustomizerService(CustomizerService customizerService) {
      this.customizerService = customizerService;
   }

   protected void preProcessSpreadsheetInternal(String spreadsheetResourceId, 
                                                File customizerDir, boolean ignoreWarnings) {

      LogListenerBase listener = new LogListenerBase();

      if (customizerDir.exists()) {
         delDir(customizerDir);
      }
      customizerDir.mkdirs();

      File outputDir = new File(customizerDir, "output");
      File sourceDir = new File(customizerDir, "source");
      outputDir.mkdirs();
      sourceDir.mkdirs();

      ContentResource cr = null;
      try {
         cr = ContentHostingService.getResource(spreadsheetResourceId);
         ExcelPreProcessor preProcessor = new ExcelPreProcessor(true);
         preProcessor.setLogListener(listener);
         preProcessor.processDocument(cr.streamContent(),
            sourceDir.getAbsolutePath(), outputDir.getAbsolutePath());

         XsltPreProcessor processor = new XsltPreProcessor(true);
         processor.setLogListener(listener);
         processor.process(sourceDir.getAbsolutePath(), outputDir.getAbsolutePath());

         ToolRegProcessor toolRegProcessor = new ToolRegProcessor(true);
         toolRegProcessor.setLogListener(listener);
         toolRegProcessor.process(sourceDir.getAbsolutePath(), outputDir.getAbsolutePath());

         SakaiRealmProcessor sakaiRealmProcessor = new SakaiRealmProcessor(true);
         sakaiRealmProcessor.setLogListener(listener);
         sakaiRealmProcessor.process(sourceDir.getAbsolutePath(), outputDir.getAbsolutePath());

         ToolCategoryProcessor toolCategoryProcessor = new ToolCategoryProcessor(true);
         toolCategoryProcessor.setLogListener(listener);
         toolCategoryProcessor.process(sourceDir.getAbsolutePath(), outputDir.getAbsolutePath());

         WorkspaceProcessor workspaceProcessor = new WorkspaceProcessor(true);
         workspaceProcessor.setLogListener(listener);
         workspaceProcessor.process(sourceDir.getAbsolutePath(), outputDir.getAbsolutePath());

         checkListener(listener, ignoreWarnings);
      } catch (PermissionException e) {
         throw new CustomizerProcessingException(e);
      } catch (IdUnusedException e) {
         throw new CustomizerProcessingException(e);
      } catch (TypeException e) {
         throw new CustomizerProcessingException(e);
      } catch (ServerOverloadException e) {
         throw new CustomizerProcessingException(e);
      }

   }

   protected void checkListener(LogListenerBase listener, boolean ignoreWarnings)
      throws CustomizerProcessingException {

      CustomizerProcessingException exp = new CustomizerProcessingException();

      if (!ignoreWarnings) {
         for (Iterator<String> i=listener.getWarns().iterator();i.hasNext();) {
            exp.addWarning(i.next());
         }
      }

      for (Iterator<String> i=listener.getErrors().iterator();i.hasNext();) {
         exp.addError(i.next());
      }

      for (Iterator<String> i=listener.getFatals().iterator();i.hasNext();) {
         exp.addError(i.next());
      }

      if (exp.hasWarningsOrErrors()) {
         throw exp;
      }
   }

   protected void delDir(File directory) {
      File[] children = directory.listFiles();

      for (int i=0;i<children.length;i++) {
         if (children[i].isDirectory()) {
            delDir(children[i]);
         }
         else {
            children[i].delete();
         }
      }
      directory.delete();
   }

}
