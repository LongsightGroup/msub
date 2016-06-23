/*
 * Copyright 2011 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): duffy
 */

package com.rsmart.content.google.api;

/**
 * This exception is thrown when a document cannot be found for the supplied Google Document ID.
 * 
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Mar 30, 2010
 * Time: 12:46:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleDocumentMissingException
    extends GoogleDocsException
{
    public GoogleDocumentMissingException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public GoogleDocumentMissingException(String msg) {
        super(msg);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public GoogleDocumentMissingException(Throwable t) {
        super(t);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public GoogleDocumentMissingException(String msg, Throwable t) {
        super(msg, t);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
