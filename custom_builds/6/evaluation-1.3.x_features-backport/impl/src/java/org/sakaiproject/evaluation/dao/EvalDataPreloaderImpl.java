/**
 * $Id: EvalDataPreloaderImpl.java 48891 2008-05-12 12:55:30Z aaronz@vt.edu $
 * $URL: https://source.sakaiproject.org/contrib/evaluation/branches/1.3.x/impl/src/java/org/sakaiproject/evaluation/dao/EvalDataPreloaderImpl.java $
 * EvalDataPreloaderImpl.java - evaluation - Feb 14, 2008 11:12:41 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;

/**
 * This is a simple bean which will initiate the data preloading sequence with locks
 * when it initializes if autoDDL is enabled
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalDataPreloaderImpl {

   private static Log log = LogFactory.getLog(PreloadDataImpl.class);

   public static String EVAL_PRELOAD_LOCK = "data.preload.lock";
   public static String EVAL_FIXUP_LOCK = "data.fixup.lock";

   private EvaluationDao dao;
   public void setDao(EvaluationDao evaluationDao) {
      this.dao = evaluationDao;
   }

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private PreloadDataImpl preloadData;
   public void setPreloadData(PreloadDataImpl preloadData) {
      this.preloadData = preloadData;
   }

   public void init() {
      boolean autoDDL = externalLogic.getConfigurationSetting(EvalExternalLogic.SETTING_AUTO_DDL, false);
      String serverId = externalLogic.getConfigurationSetting(EvalExternalLogic.SETTING_SERVER_ID, "UNKNOWN_SERVER_ID");
      if (autoDDL) {
         log.info("Auto DDL enabled: Checking preload data exists...");
         if (! preloadData.checkCriticalDataPreloaded() ) {
            log.info("Preload data missing, preparing to preload critical evaluation system data");
            Boolean gotLock = dao.obtainLock(EVAL_PRELOAD_LOCK, serverId, 10000);
            if (gotLock == null) {
               throw new IllegalStateException("Failure attempting to obtain lock ("+EVAL_PRELOAD_LOCK+") and preload evaluation system data, " 
                     + "see logs just before this for more details, system terminating...");               
            } else if (gotLock) {
               preloadData.preload();
               dao.releaseLock(EVAL_PRELOAD_LOCK, serverId);
            }
         }
      } else {
         log.info("Auto DDL disabled: Skipping data preloading...");
         if ( preloadData.checkCriticalDataPreloaded() ) {
            log.info("Preloaded data is present");
         } else {
            throw new IllegalStateException("Preloaded data is missing, evaluation cannot start up in this state, " +
            		"you must either enable the auto.ddl flag or preload the critical system data (config settings, email templates, scales) " +
            		"manually, evaluation system shutting down...");
         }
      }

      Boolean gotLock = dao.obtainLock(EVAL_FIXUP_LOCK, serverId, 3000);
      if (gotLock != null && gotLock) {
         dao.fixupDatabase();
         dao.releaseLock(EVAL_FIXUP_LOCK, serverId);
      }
   }

}
