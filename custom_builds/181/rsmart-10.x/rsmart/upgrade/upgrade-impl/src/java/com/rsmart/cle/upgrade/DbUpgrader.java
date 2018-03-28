package com.rsmart.cle.upgrade;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.api.MigrationInfo;

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

public class DbUpgrader {
    private SqlService sqlService;
    private static Log LOG = LogFactory.getLog(DbUpgrader.class);

    private DataSource dataSource;

    protected void upgrade() {
        // Create the Flyway instance
        Flyway flyway = new Flyway();

        // Point it to the database
        flyway.setDataSource(dataSource);

        String vendor = sqlService.getVendor();
//        if ("oracle".equalsIgnoreCase(vendor)) {
//            flyway.setLocations("db/migration/oracle");
//        }
        if ("mysql".equalsIgnoreCase(vendor)) {
            flyway.setLocations("db/migration/mysql");
        } else {
            LOG.error("Vendor: " + vendor + " not supported for auto db migration");
            return;
        }

        flyway.setInitVersion("2.9.1.0");
        flyway.setInitDescription("initial version");
        flyway.setInitOnMigrate(true);

        // Start the migration
        flyway.migrate();

        if (LOG.isInfoEnabled()) {
            MigrationInfo migrationInfo = flyway.info().current();
            if (migrationInfo != null) {
                LOG.info("Applied migration version: '" + migrationInfo.getVersion() + "' description:'" + migrationInfo.getDescription() + "' in " +
                        migrationInfo.getExecutionTime() + " millis.  State: " + migrationInfo.getState().getDisplayName());
            }
        }
    }

    protected void adjustLogLevel(String packageName, String level) {
    	if (packageName == null || "".equals(packageName)) return;
    	
    	Logger logger = Logger.getLogger(packageName);
		if (logger != null)
		{
			if (level == null || "".equals(level)) level = "INFO";
			
			if ("DEBUG".equals(level))
				logger.setLevel(org.apache.log4j.Level.DEBUG);
			else if ("WARN".equals(level))
				logger.setLevel(org.apache.log4j.Level.WARN);
			else if ("ERROR".equals(level))
				logger.setLevel(org.apache.log4j.Level.ERROR);
			else
				logger.setLevel(org.apache.log4j.Level.INFO);
		}
    }

    public void init() {
        ClassLoader current = Thread.currentThread().getContextClassLoader();

        try {

            if (!ServerConfigurationService.getBoolean("flyway.enabled", true) ||
                    ServerConfigurationService.getBoolean("auto.ddl", false)) {
                LOG.info("Database migration (flyway) is disabled, on this startup");
                return;
            }
            adjustLogLevel("com.googlecode.flyway", "DEBUG");


            // can't find the sql resources without this.  It ends up looking
            // in the /access classloader for resources, oh the joys the classloader
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            upgrade();
        } catch (Throwable t)  {
            LOG.error("flyway initialization failed: " + t.getMessage(), t);
            throw new RuntimeException(t);
        } finally {
            Thread.currentThread().setContextClassLoader(current);
            adjustLogLevel("com.googlecode.flyway", "INFO");
        }
    }

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
