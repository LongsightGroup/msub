package com.rsmart.customer.integration.processor;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Sep 28, 2010
 * Time: 2:05:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class BaseFileProcessorState
    extends BaseProcessorState
    implements FileProcessorState
{

    private String
        filename = null;
    private boolean
        headerRowPresent = false;
    private int
        columns = 0;

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setHeaderRowPresent(boolean p)
    {
        headerRowPresent = p;
    }

    public boolean isHeaderRowPresent()
    {
        return headerRowPresent;
    }

    public void setColumns(int c)
    {
        columns = c;
    }

    public int getColumns()
    {
        return columns;
    }
}
