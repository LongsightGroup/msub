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

package com.rsmart.antivirus;

import java.io.InputStream;

/**
 * provide virus scanning capabilities
 * <br>Creation Date: Mar 23, 2005
 *
 * @author Mike DeSimone, mike.[at].rsmart.com
 * @author John Bush
 * @version $Revision$
 */
public interface VirusScanner {

   /**
    * check whether the virus scanner is enabled
    * @return true if virus scanning can be performed
    */
   public boolean getEnabled();

   /**
    * scan byte array for a virus
    * @param bytes
    * @throws VirusFoundException if a virus was found
    * @throws VirusScanIncompleteException if virus scan was not completed due to an error
    */
   public void scan(byte[] bytes) throws VirusFoundException, VirusScanIncompleteException;

   public void scan(InputStream inputStream) throws VirusFoundException, VirusScanIncompleteException;
}
