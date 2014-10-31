package com.rsmart.sakai.webservices;

import java.io.*;
import javax.servlet.*;


/**
 * StringOutputStream is a stub for ServletOutputStream which
 * buffers up the output in a string buffer instead of sending it
 * straight to the client.
 */
public class StringOutputStream
        extends ServletOutputStream {
    private StringWriter stringWriter;

    public StringOutputStream(StringWriter stringWriter) {
        this.stringWriter = stringWriter;
    }

    public void write(int c) {
        stringWriter.write(c);
    }
}