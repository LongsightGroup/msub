package com.rsmart.admin.customizer.tool;

import com.rsmart.sakaiproject.component.model.ToToolDef;
import com.rsmart.sakaiproject.component.model.ToCategory;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Dec 30, 2008
 * Time: 10:37:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class ToolCategoryForm {
   
   ToCategory category;
   List<ToToolDef> tools;
   boolean moveUp;
   boolean moveDown;
   
   boolean expanded;

   public ToolCategoryForm(ToCategory category) {
      this.category = category;
      
      tools = new ArrayList();
      
      if (category.getTools() != null) {
         
         for (ToToolDef tool : category.getTools()) {
            if (tool != null && (!(tool.getToolId().equals("sakai.iframe.site") || 
               tool.getToolId().equals("sakai.iframe.myworkspace") || 
               tool.getToolId().equals("sakai.synoptic.chat") || 
               tool.getToolId().equals("sakai.synoptic.discussion") || 
               tool.getToolId().equals("sakai.synoptic.announcement")))) {
               tools.add(tool);
            }
         }
      }
   }

   public ToCategory getCategory() {
      return category;
   }

   public void setCategory(ToCategory category) {
      this.category = category;
   }

   public List<ToToolDef> getTools() {
      return tools;
   }

   public void setTools(List<ToToolDef> tools) {
      this.tools = tools;
   }

   public boolean isExpanded() {
      return expanded;
   }

   public void setExpanded(boolean expanded) {
      this.expanded = expanded;
   }

   public boolean isMoveUp() {
      return moveUp;
   }

   public void setMoveUp(boolean moveUp) {
      this.moveUp = moveUp;
   }

   public boolean isMoveDown() {
      return moveDown;
   }

   public void setMoveDown(boolean moveDown) {
      this.moveDown = moveDown;
   }
}
