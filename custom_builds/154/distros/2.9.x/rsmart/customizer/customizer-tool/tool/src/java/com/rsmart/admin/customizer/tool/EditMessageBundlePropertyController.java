package com.rsmart.admin.customizer.tool;

import org.sakaiproject.messagebundle.api.MessageBundleService;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;
import com.rsmart.sakai.common.web.springframework.AbstractCancelableController;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Mar 18, 2010
 * Time: 9:57:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class EditMessageBundlePropertyController extends AbstractCancelableController {
    private MessageBundleService messageBundleService;

   protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {

       MessageBundleProperty mbp = messageBundleService.getMessageBundleProperty(Long.valueOf(httpServletRequest.getParameter("id")));
        if (mbp.getValue() == null) mbp.setValue(mbp.getDefaultValue());
        return mbp;
   }
    protected void doSubmitAction(Object command) throws Exception {
        MessageBundleProperty mbp = (MessageBundleProperty) command;
        messageBundleService.updateMessageBundleProperty(mbp);
    }

   protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
      // when done, redirect to the main page and specify the "command" http parameter
      ModelAndView modelAndView = null;

      if (isCancel(request)) {
         modelAndView = new ModelAndView(getSuccessView(), "command", "list");
          modelAndView.addObject("msg", "Edit Cancelled");
      } else {
         try {
            doSubmitAction(command);
            modelAndView = new ModelAndView(getSuccessView(), "command", "list");
            modelAndView.addObject("msg", "Edit Successful");
         } catch (Exception ex) {
            modelAndView = new ModelAndView(getSuccessView(), "command", "list");
            Map model = modelAndView.getModel();
            model.put("error", ex.getMessage());
            logger.warn(ex);
         }
      }
      return modelAndView;
   }

    public void setMessageBundleService(MessageBundleService messageBundleService) {
        this.messageBundleService = messageBundleService;
    }
}
