package com.rsmart.admin.customizer.tool;

import com.rsmart.admin.customizer.api.CustomizerService;
import com.rsmart.sakai.common.web.springframework.AbstractCancelableController;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 7/6/11
 * Time: 4:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReloadToolsController extends AbstractCancelableController {
    private CustomizerService customizerService;
    protected void doSubmitAction(Object command) throws Exception {
        customizerService.reloadTools();
    }

    public void setCustomizerService(CustomizerService customizerService) {
        this.customizerService = customizerService;
    }

    protected ModelAndView onSubmit(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Object command,
                                   BindException errors)
                            throws Exception {
      if (!isCancel(request)) {
         doSubmitAction(command);
      } else {
          return new ModelAndView("redirect:customizer.osp");
      }
      ModelAndView modelAndView = new ModelAndView(getSuccessView());
      modelAndView.addObject("msg", "Reload Successful");
      return modelAndView;
   }

}
