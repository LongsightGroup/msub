/**
 * $Id: EvalImportJob.java 46025 2008-02-27 13:01:48Z aaronz@vt.edu $
 * $URL: https://source.sakaiproject.org/contrib/evaluation/branches/1.3.x/api/src/java/org/sakaiproject/evaluation/logic/imports/EvalImportJob.java $
 * EvalImportJob.java - evaluation - May 28, 2007 12:07:31 AM - rwellis
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.imports;

import org.quartz.Job;

/**
 * A Quartz job to process an XML ContentResource and persist the contained evaluation data
 * 
 * @author rwellis
 */
public interface EvalImportJob extends Job {

}
