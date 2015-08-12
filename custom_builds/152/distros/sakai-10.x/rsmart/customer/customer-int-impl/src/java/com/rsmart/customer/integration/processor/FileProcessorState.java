package com.rsmart.customer.integration.processor;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Sep 28, 2010
 * Time: 2:00:43 PM
 * To change this template use File | Settings | File Templates.
 */
public interface FileProcessorState extends ProcessorState
{
    public void setFilename(String filename);

    public String getFilename();

    public void setHeaderRowPresent (boolean p);

    public boolean isHeaderRowPresent();

    public void setColumns (int c);

    public int getColumns ();
}
