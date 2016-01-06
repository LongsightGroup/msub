package com.rsmart.admin.customizer.tool;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Mar 19, 2010
 * Time: 12:53:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileUploadBean {
    private byte[] file;
    private String locale;
    private int rows;

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public byte[] getFile() {
        return file;
    }

}
