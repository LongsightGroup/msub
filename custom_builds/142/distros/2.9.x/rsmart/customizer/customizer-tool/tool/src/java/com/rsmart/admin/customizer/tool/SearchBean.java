package com.rsmart.admin.customizer.tool;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Mar 22, 2010
 * Time: 2:40:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchBean {
    private String module;
    private String baseName;
    private String search;
    private String locale;

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public boolean getNotEmpty(){
        return notEmpty();
    }

    public boolean notEmpty(){
        if (module != null && module.length() > 0){
            return true;
        }
        if (baseName != null && baseName.length() > 0){
            return true;
        }
        if (search != null && search.length() > 0){
            return true;
        }
        if (locale != null && locale.length() > 0){
            return true;
        }
        return false;
    }
}
