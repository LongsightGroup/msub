package com.rsmart.admin.customizer.tool;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Feb 2, 2009
 * Time: 3:17:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class FuncRequireForm {
   
   private String toolId;
   private List<String> required;
   private List<String> functions;
   private Map<String, String> requiredMap;

   public String getToolId() {
      return toolId;
   }

   public void setToolId(String toolId) {
      this.toolId = toolId;
   }

   public List<String> getFunctions() {
      return functions;
   }

   public void setFunctions(List<String> functions) {
      this.functions = functions;
   }

   public List<String> getRequired() {
      return required;
   }

   public void setRequired(List<String> required) {
      setRequiredMap(new Hashtable());
      for (String func : required) {
         getRequiredMap().put(func, "true");
      }
      
      this.required = required;
   }

   public Map<String, String> getRequiredMap() {
      return requiredMap;
   }

   public void setRequiredMap(Map<String, String> requiredMap) {
      this.requiredMap = requiredMap;
   }

   public String getRequiredAsString() {
      StringBuffer sb = new StringBuffer();
      
      boolean first = true;
      for (String func : getRequired()) {
         if (!first) {
            sb.append("|");
         }
         sb.append(func);
         first = false;
      }
      
      return sb.toString();
   }
   
   public void setRequired(String requiredString) {
      List<String> requiredList = new ArrayList();
      
      if (requiredString != null) {
         String[] funcs = requiredString.split("\\|");
         for (int i=0;i<funcs.length;i++) {
            requiredList.add(funcs[i]);
         }
      }
      
      setRequired(requiredList);
   }
}
