package com.rsmart.admin.customizer.tool;

import org.sakaiproject.messagebundle.api.MessageBundleService;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;
import com.rsmart.sakai.common.web.springframework.AbstractCancelableController;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Mar 19, 2010
 * Time: 12:43:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class UploadMessageBundleFileController extends AbstractCancelableController {
    MessageBundleService messageBundleService;

    protected Map referenceData(HttpServletRequest request) throws Exception {
        Map map = new HashMap();
        map.put("locales",messageBundleService.getLocales());
        return map;
    }

    protected void doSubmitAction(Object command) throws Exception {
        FileUploadBean fileUploadBean = (FileUploadBean) command;
        List properties = new ArrayList();
        InputStream myxls = new ByteArrayInputStream(fileUploadBean.getFile());
        HSSFWorkbook wb     = new HSSFWorkbook(myxls);
        HSSFSheet sheet = wb.getSheetAt(0);       // first sheet
        int rows = 0;
        boolean foundHeaderRow = false;
        for (Iterator i =sheet.rowIterator();i.hasNext();)
        {
            HSSFRow row = (HSSFRow) i.next();
            if ("module".equals(row.getCell((short)0).getStringCellValue())) {
                foundHeaderRow = true;
                continue;
            }
            if (foundHeaderRow) {
                MessageBundleProperty mbp = new MessageBundleProperty();
                mbp.setModuleName(row.getCell((short)0).getStringCellValue());
                mbp.setBaseName(row.getCell((short)1).getStringCellValue());
                mbp.setPropertyName(row.getCell((short)2).getStringCellValue());
                mbp.setValue(row.getCell((short)3).getStringCellValue());
                mbp.setLocale(fileUploadBean.getLocale());
                //TODO allow an optional 5 column for new translations
                properties.add(mbp);
                
                rows++;
            }
        }
        if (foundHeaderRow == false) {
            throw new Exception("Invalid excel file, can't find header row");
        }
        fileUploadBean.setRows(messageBundleService.importProperties(properties));
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        // when done, redirect to the main page and specify the "command" http parameter
        ModelAndView modelAndView = null;
        FileUploadBean fileUploadBean = (FileUploadBean) command;
        if (isCancel(request)) {
            modelAndView = new ModelAndView(getSuccessView(), "command", "list");
            modelAndView.addObject("msg", "Upload Cancelled");
        } else {
            try {
                doSubmitAction(command);
                modelAndView = new ModelAndView(getSuccessView(), "command", "list");
                modelAndView.addObject("msg", "Upload " + fileUploadBean.getRows() + " properties for locale " + fileUploadBean.getLocale());
            } catch (Exception ex) {
                modelAndView = new ModelAndView(getSuccessView(), "command", "list");
                Map model = modelAndView.getModel();
                model.put("error", ex.getMessage());
                logger.warn("",ex);
            }
        }
        return modelAndView;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
        throws ServletException {
        // to actually be able to convert Multipart instance to byte[]
        // we have to register a custom editor
        binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
        // now Spring knows how to handle multipart object and convert them
    }


    public void setMessageBundleService(MessageBundleService messageBundleService) {
        this.messageBundleService = messageBundleService;
    }
}
