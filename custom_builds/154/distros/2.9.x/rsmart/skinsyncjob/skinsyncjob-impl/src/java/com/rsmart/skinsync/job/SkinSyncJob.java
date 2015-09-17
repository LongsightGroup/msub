package com.rsmart.skinsync.job;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: 7/14/11
 * Time: 9:27 AM
 * To change this template use File | Settings | File Templates.
 */

import com.rsmart.sakai.common.job.AbstractAdminJob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import nl.edia.sakai.tool.skinmanager.SkinArchiveService;
import nl.edia.sakai.tool.skinmanager.SkinException;
import nl.edia.sakai.tool.skinmanager.SkinFileSystemService;
import nl.edia.sakai.tool.skinmanager.SkinService;
import nl.edia.sakai.tool.skinmanager.model.SkinArchive;
import nl.edia.sakai.tool.skinmanager.model.SkinDirectory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.component.cover.ServerConfigurationService;

public class SkinSyncJob extends AbstractAdminJob {

    protected final Log logger = LogFactory.getLog(getClass());
    private long fileSystemDate, dbDate;
    private SkinFileSystemService skinFileSystemService;
    private SkinArchiveService skinArchiveService;
    private SkinService skinService;

    public void init() {
        //do nothing
    }

    public void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        logger.info("Starting Skin Synchronization job");

        try {

            List<SkinDirectory> skins = skinFileSystemService.fetchInstalledSkins();

            if (!skins.isEmpty()) {
                for (SkinDirectory skinDir : skins) {
                    SkinDirectory fileSystemSkin = skinFileSystemService.findSkin(skinDir.getName());
                    SkinArchive dbSkin = skinArchiveService.findSkinArchive(skinDir.getName());

                    //Get last modified times for skins in DB and file system.
                    if (fileSystemSkin != null) {
                        fileSystemDate = fileSystemSkin.getLastModified().getTime();
                    }
                    if (dbSkin != null) {
                        dbDate = dbSkin.getLastModified().getTime();
                    }

                    /*
                    *   Compare the last modified dates of the file system and database skins.  If they are the same do nothing.
                    *   Otherwise copy the most recently modified skin to either the file system or database.
                    */
                    if (fileSystemDate == dbDate) {
                        logger.info("File system and database archive skins are the same version, doing nothing.");
                    } else {
                        if (fileSystemDate > dbDate) {
                            logger.info("Synchronizing file system skin.");
                            copyToDB(fileSystemSkin.getName());
                        } else {
                            if (dbDate > fileSystemDate) {
                                logger.info("Synchronizing database skin.");
                                copyFromDB(dbSkin.getName());
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Caught Exception " + e, e);
        } catch (SkinException e) {
            logger.error("Caught Exception " + e, e);
        }
    }

    private void copyFromDB(String skinName) {
        try {
            //Create skin file system directory from current version in database.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            skinArchiveService.fetchSkinArchiveData(skinName, outputStream);
            skinFileSystemService.updateSkin(skinName, new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (SkinException e) {
            logger.error("Caught Exception: " + e, e);
        } catch (IOException e) {
            logger.error("Caught Exception: " + e, e);
        }
    }

    private void copyToDB(String skinName) {
        try {
            //Use skin in file system to update skin saved in database.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            skinFileSystemService.writeSkinData(skinName, outputStream);
            skinService.updateSkin(skinName, new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (SkinException e) {
            logger.error("Caught Exception: " + e, e);
        } catch (IOException e) {
            logger.error("Caught Exception: " + e, e);
        }
    }

    public SkinFileSystemService getSkinFileSystemService() {
        return skinFileSystemService;
    }

    public void setSkinFileSystemService(SkinFileSystemService skinFileSystemService) {
        this.skinFileSystemService = skinFileSystemService;
    }

    public SkinArchiveService getSkinArchiveService() {
        return skinArchiveService;
    }

    public void setSkinArchiveService(SkinArchiveService skinArchiveService) {
        this.skinArchiveService = skinArchiveService;
    }

    public SkinService getSkinService() {
        return skinService;
    }

    public void setSkinService(SkinService skinService) {
        this.skinService = skinService;
    }

    public long getDbDate() {
        return dbDate;
    }

    public void setDbDate(long dbDate) {
        this.dbDate = dbDate;
    }

    public long getFileSystemDate() {
        return fileSystemDate;
    }

    public void setFileSystemDate(long fileSystemDate) {
        this.fileSystemDate = fileSystemDate;
    }
}