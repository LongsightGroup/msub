package com.rsmart.admin.customizer.tool;

import org.sakaiproject.messagebundle.api.MessageBundleService;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;
import org.apache.poi.hssf.usermodel.*;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.Validator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Mar 19, 2010
 * Time: 8:46:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class MessageBundleController extends MultiActionController {
    MessageBundleService messageBundleService;


    public ModelAndView showAdvancedSearchForm(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("advancedSearch");
        modelAndView.addObject("locales",messageBundleService.getLocales());

        return modelAndView;
    }
    public ModelAndView showDownloadForm(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("downloadMessageBundle");
        modelAndView.addObject("locales",messageBundleService.getLocales());
        modelAndView.addObject("modules",messageBundleService.getAllModuleNames());

        return modelAndView;
    }

    public ModelAndView revertAll(HttpServletRequest request, HttpServletResponse response){
        String locale = request.getParameter("selectedLocale");
        int rows = messageBundleService.revertAll(locale);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:messageBundleHome");
        modelAndView.addObject("msg","Reverted " + rows + " properties for locale " + locale);
        return modelAndView;
    }


    public ModelAndView download(HttpServletRequest request, HttpServletResponse response){
        String locale = request.getParameter("selectedLocale");
        String module = request.getParameter("module");
        List<MessageBundleProperty> properties = messageBundleService.getAllProperties(locale, module);

        HSSFWorkbook wb = new HSSFWorkbook();
        short rowNum = 0;
        // a tab title in a workbook have a maximum length of 31 chars.
        // otherwise an Exception will been thrown "Sheet name cannot be blank, greater than 31 chars, or contain any of /\*?[]"
        // we truncate it if it's too long
        String sheetTitle = "Sakai " + ServerConfigurationService.getString("version.service") + " bundle data (" + locale +")";
        int siteTitleLength = sheetTitle.length();
        if (siteTitleLength > 31) {
            logger.info(this + " Site title is too long (" + siteTitleLength + " chars) truncating it down to 31 chars!");
            sheetTitle = sheetTitle.substring(0, 31);
        }
        HSSFSheet sheet = wb.createSheet(Validator.escapeZipEntry(sheetTitle));

        // Create a row and put some cells in it. Rows are 0 based.
        HSSFRow row = sheet.createRow(rowNum++);

        row.createCell((short) 0).setCellValue(sheetTitle);

        // empty line
        row = sheet.createRow(rowNum++);
        row.createCell((short) 0).setCellValue("");

        // download time
        row = sheet.createRow(rowNum++);
        row.createCell((short) 0).setCellValue("Download Date: " + TimeService.newTime().toStringLocalFull());

        // empty line
        row = sheet.createRow(rowNum++);
        row.createCell((short) 0).setCellValue("");

        HSSFCellStyle style = wb.createCellStyle();
        
        // set up the header cells
        row = sheet.createRow(rowNum++);
        short cellNum = 0;

        HSSFCell cell = row.createCell(cellNum++);
        cell.setCellStyle(style);
        cell.setCellValue("module");

        cell = row.createCell(cellNum++);
        cell.setCellStyle(style);
        cell.setCellValue("baseName");

        cell = row.createCell(cellNum++);
        cell.setCellStyle(style);
        cell.setCellValue("key");

        cell = row.createCell(cellNum++);
        cell.setCellStyle(style);
        cell.setCellValue(locale + " default value");

        for (MessageBundleProperty property: properties) {
            row = sheet.createRow(rowNum);
            rowNum++;
            cellNum = 0;
            row.createCell(cellNum++).setCellValue(property.getModuleName());
            row.createCell(cellNum++).setCellValue(property.getBaseName());
            row.createCell(cellNum++).setCellValue(property.getPropertyName());
            row.createCell(cellNum++).setCellValue(property.getDefaultValue());
        }

        // output
        try
        {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename = message_bundle_download.xls");
            wb.write(response.getOutputStream());
        }
        catch (IOException e)
        {
            logger.error("",e);
        }

        return null;
    }

    public ModelAndView showUploadForm(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("uploadMessageBundle");
        modelAndView.addObject("locales",messageBundleService.getLocales());

        return modelAndView;
    }
    public ModelAndView showRevertAllForm(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("revertAll");
        modelAndView.addObject("locales",messageBundleService.getLocales());
        return modelAndView;
    }

    public void setMessageBundleService(MessageBundleService messageBundleService) {
        this.messageBundleService = messageBundleService;
    }
}
