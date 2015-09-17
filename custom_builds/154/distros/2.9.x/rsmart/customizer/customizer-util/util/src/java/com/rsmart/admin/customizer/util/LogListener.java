/*
 * Copyright 2008 The rSmart Group
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
 * Contributor(s): jbush
 */

package com.rsmart.admin.customizer.util;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Apr 3, 2007
 * Time: 8:29:55 AM
 * To change this template use File | Settings | File Templates.
 */
public interface LogListener {
   
   void warn(java.lang.Object object);

   void warn(java.lang.Object object, java.lang.Throwable throwable);

   void error(java.lang.Object object);

   void error(java.lang.Object object, java.lang.Throwable throwable);

   void fatal(java.lang.Object object);

   void fatal(java.lang.Object object, java.lang.Throwable throwable);
   
}
