package com.rsmart.admin.customizer.tool;

import org.sakaiproject.tool.api.Tool;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Dec 4, 2008
 * Time: 11:17:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class ToolForm {
   
   private String editToolId;
   private String toolId;
   private String toolTitle;
   private boolean included;

   public ToolForm() {
   }

   public ToolForm(Tool tool, boolean included) {
      this.included = included;
      this.toolId = tool.getId();
      this.toolTitle = tool.getTitle();
   }
   
   public String getToolId() {
      return toolId;
   }

   public void setToolId(String toolId) {
      this.toolId = toolId;
   }

   public String getToolTitle() {
      return toolTitle;
   }

   public void setToolTitle(String toolTitle) {
      this.toolTitle = toolTitle;
   }

   public String getEditToolId() {
      return editToolId;
   }

   public void setEditToolId(String editToolId) {
      this.editToolId = editToolId;
   }

   public boolean isIncluded() {
      return included;
   }

   public void setIncluded(boolean included) {
      this.included = included;
   }
}
