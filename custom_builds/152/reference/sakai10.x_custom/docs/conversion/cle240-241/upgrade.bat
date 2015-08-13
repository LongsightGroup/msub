@echo off

if not exist %1\nul (
   echo directory '%1' does not exist
   echo Usage: upgrade.bat c:\path\to\tomcat
   goto :EOF
)

set tomcat=%~1
echo Upgrading %tomcat%...
pause

del %tomcat%\common\lib\sakai*.jar
rmdir /q /s %tomcat%\components
del /q %tomcat%\shared\lib\*
del %tomcat%\server\lib\sakai*.jar

xcopy /C /E /Y /H tomcat\common %tomcat%\common
xcopy /C /E /Y /H /I tomcat\components %tomcat%\components
xcopy /C /E /Y /H tomcat\shared\* %tomcat%\shared
xcopy /C /E /Y /H tomcat\server\* %tomcat%\server
xcopy /C /E /Y /H tomcat\webapps\* %tomcat%\webapps

rmdir /q /s %tomcat%\webapps\access
rmdir /q /s %tomcat%\webapps\authn
rmdir /q /s %tomcat%\webapps\courier
rmdir /q /s %tomcat%\webapps\dav
rmdir /q /s %tomcat%\webapps\library
rmdir /q /s %tomcat%\webapps\meleteDocs
rmdir /q /s %tomcat%\webapps\mercury
rmdir /q /s %tomcat%\webapps\osp-common-tool
rmdir /q /s %tomcat%\webapps\osp-glossary-tool
rmdir /q /s %tomcat%\webapps\osp-jsf-example
rmdir /q /s %tomcat%\webapps\osp-jsf-resource
rmdir /q /s %tomcat%\webapps\osp-matrix-tool
rmdir /q /s %tomcat%\webapps\osp-portal-tool
rmdir /q /s %tomcat%\webapps\osp-portal
rmdir /q /s %tomcat%\webapps\osp-presentation-tool
rmdir /q /s %tomcat%\webapps\osp-reports-tool
rmdir /q /s %tomcat%\webapps\osp-wizard-tool
rmdir /q /s %tomcat%\webapps\podcasts
rmdir /q /s %tomcat%\webapps\portal-render
rmdir /q /s %tomcat%\webapps\portal
rmdir /q /s %tomcat%\webapps\rsmart-customizer-tool
rmdir /q /s %tomcat%\webapps\rsmart-help-content
rmdir /q /s %tomcat%\webapps\sakai-alias-tool
rmdir /q /s %tomcat%\webapps\sakai-announcement-tool
rmdir /q /s %tomcat%\webapps\sakai-archive-tool
rmdir /q /s %tomcat%\webapps\sakai-assignment-tool
rmdir /q /s %tomcat%\webapps\sakai-authz-tool
rmdir /q /s %tomcat%\webapps\sakai-axis
rmdir /q /s %tomcat%\webapps\sakai-blogger-tool
rmdir /q /s %tomcat%\webapps\sakai-calendar-summary-tool
rmdir /q /s %tomcat%\webapps\sakai-calendar-tool
rmdir /q /s %tomcat%\webapps\sakai-chat-tool
rmdir /q /s %tomcat%\webapps\sakai-content-tool
rmdir /q /s %tomcat%\webapps\sakai-discussion-tool
rmdir /q /s %tomcat%\webapps\sakai-dp-tool
rmdir /q /s %tomcat%\webapps\sakai-fck-connector
rmdir /q /s %tomcat%\webapps\sakai-gmt-tool
rmdir /q /s %tomcat%\webapps\sakai-gradebook-testservice
rmdir /q /s %tomcat%\webapps\sakai-gradebook-tool
rmdir /q /s %tomcat%\webapps\sakai-help-tool
rmdir /q /s %tomcat%\webapps\sakai-jforum-tool
rmdir /q /s %tomcat%\webapps\sakai-jsf-example
rmdir /q /s %tomcat%\webapps\sakai-jsf-resource
rmdir /q /s %tomcat%\webapps\sakai-login-tool
rmdir /q /s %tomcat%\webapps\sakai-mailarchive-james
rmdir /q /s %tomcat%\webapps\sakai-mailarchive-tool
rmdir /q /s %tomcat%\webapps\sakai-melete-tool
rmdir /q /s %tomcat%\webapps\sakai-memory-tool
rmdir /q /s %tomcat%\webapps\sakai-messageforums-tool
rmdir /q /s %tomcat%\webapps\sakai-message-tool
rmdir /q /s %tomcat%\webapps\sakai-metaobj-tool
rmdir /q /s %tomcat%\webapps\sakai-news-tool
rmdir /q /s %tomcat%\webapps\sakai-osid-unit-test
rmdir /q /s %tomcat%\webapps\sakai-podcasts
rmdir /q /s %tomcat%\webapps\sakai-postem-tool
rmdir /q /s %tomcat%\webapps\sakai-presence-tool
rmdir /q /s %tomcat%\webapps\sakai-presentation-tool
rmdir /q /s %tomcat%\webapps\sakai-profile-tool
rmdir /q /s %tomcat%\webapps\sakai-reset-pass
rmdir /q /s %tomcat%\webapps\sakai-rights-tool
rmdir /q /s %tomcat%\webapps\sakai-roster-tool
rmdir /q /s %tomcat%\webapps\sakai-rutgers-linktool
rmdir /q /s %tomcat%\webapps\sakai-rwiki-tool
rmdir /q /s %tomcat%\webapps\sakai-sample-tool-jsf
rmdir /q /s %tomcat%\webapps\sakai-sample-tool-servlet
rmdir /q /s %tomcat%\webapps\sakai-scheduler-tool
rmdir /q /s %tomcat%\webapps\sakai-search-tool
rmdir /q /s %tomcat%\webapps\sakai-sections-tool
rmdir /q /s %tomcat%\webapps\sakai-site-manage-tool
rmdir /q /s %tomcat%\webapps\sakai-site-pageorder-helper
rmdir /q /s %tomcat%\webapps\sakai-sitestats-tool
rmdir /q /s %tomcat%\webapps\sakai-site-tool
rmdir /q /s %tomcat%\webapps\sakai-syllabus-tool
rmdir /q /s %tomcat%\webapps\sakai-tool-tool-su
rmdir /q /s %tomcat%\webapps\sakai-usermembership-tool
rmdir /q /s %tomcat%\webapps\sakai-user-tool-admin-prefs
rmdir /q /s %tomcat%\webapps\sakai-user-tool-prefs
rmdir /q /s %tomcat%\webapps\sakai-user-tool
rmdir /q /s %tomcat%\webapps\sakai-web-tool
rmdir /q /s %tomcat%\webapps\samigo
rmdir /q /s %tomcat%\webapps\tool
rmdir /q /s %tomcat%\webapps\web
