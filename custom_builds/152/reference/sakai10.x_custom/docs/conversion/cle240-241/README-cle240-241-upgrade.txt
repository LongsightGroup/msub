Upgrading CLE 2.4.0 to CLE 2.4.1
================================

As of the 2.4.1 release, a binary install option is available.  Upgrading from 2.4.0 is also available in binary form.  Source code is also available for situations in which custom code is used and must be compiled in with the CLE code.  This document will explain the upgrade with the binary option.  If you are using source, the only difference will be you will build & deploy the new 2.4.1 code in step 5 rather than execute the upgrade script.

Overview
========
The general process overview is that the 2.4.0 database is upgraded via a script, then the 2.4.1 code deployed to the 2.4.0 tomcat is configured to run against the converted database.  You will have to start up once with a special setting to do some conversion, then can remove the setting.

Detailed Instructions
=====================

1) download the upgrade file
- obtain the binary upgrade file from RSN.  

2) Backup current data
- back up your current production database & uploaded files.  Uploaded files are placed on the files system by several tools.  You can view the locations in the local.prooperties file by reviewing these properties:

  - location@org.sakaiproject.search.api.SearchService.SegmentStore
  - sharedSegments@org.sakaiproject.search.api.SearchService.SegmentStore
  - bodyPath@org.sakaiproject.content.api.ContentHostingService
  - storagePath@org.sakaiproject.archive.api.ArchiveService
  - melete.meleteDocsDir
  - samigo.answerUploadRepositoryPath

 - Also, you may have modified SystemGlobals.properties in the jforum tool to point to a file storage area. 

3) Upgrade the database.  The sql script files cle_2.4.0-2.4.1_mysql.sql and cle_2.4.0-2.4.1_oracle.sql are located in the conversion folder.

4) Add/update the local.properties file:

add the following 7 lines between the dashes:
-------------------------
# This setting removes the unjoin link from specified site types within the membership tool
# wsetup.disable.unjoin.count=1
# wsetup.disable.unjoin.1=course

# online help location
#help.location=http://localhost:8080/rsmart-help-content
melete.migrate=true
-------------------------
change localhost in the URL to your external server URL.

If you have lines like the following, the references to the Sferyx editor should be removed as the CLE does not ship with this proprietary editor.

change:
melete.wysiwyg.editor=FCK Editor 
melete.wysiwyg.editor.count=2
melete.wysiwyg.editor1=Sferyx Editor
melete.wysiwyg.editor2=FCK Editor

to:
melete.wysiwyg.editor=FCK Editor 
melete.wysiwyg.editor.count=1
melete.wysiwyg.editor1=FCK Editor


5) run upgrade.sh or upgrade.bat.  This will overwrite all files in the tomcat area.  You will need to provide the full path to the tomcat home directory.
If you have a customized skin, you will need to save those files to another location, then copy them back after the script finishes.  These files are located in $tomcat/webapps/library/skin.

6) copy the file moduleSeq.dtd from the conversion folder to the folder identifed in local.properties as the melete.packagefiles directory.

7) Start tomcat.  Once started, you will need to log in and navigate to a course site with content in the Lessons tool.  Click on that tool to start the Melete migration process.  The Melete tool was updated to v2.4 in this release, and files stored in the meleteDocs folder will be transferred to the native Content hosting service provided by Sakai.  Once this process is done, you can remove the meleteDocs folder.  However, please review the tomcat logs carefully before doing so to make sure no errors occurred.

Depending on how much content is in the Lessons tool, this process may take quite some time to complete.  Progress messages are written to the tomcat logs.

If any errors do occur, our recommendation is to restore the database & reapply the upgrade sql script, and retry things after addressing the error.  See also the file melete_troubleshooting.txt for the instructions that were provided by the Melete team.

8) Once Melete/Lessons is upgraded successfully, stop tomcat & remove or comment out the melete.migrate setting.

The upgrade is complete.
