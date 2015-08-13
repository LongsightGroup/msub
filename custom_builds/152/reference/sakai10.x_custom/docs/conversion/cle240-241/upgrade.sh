#!/bin/sh

if [ $# != 1 ]; then
  echo Usage: ./upgrade.sh /path/to/tomcat
  exit 1
fi

tomcat=$1

rm $tomcat/common/lib/sakai*.jar
rm -rf $tomcat/components/*
rm -rf $tomcat/shared/lib/*
rm $tomcat/server/lib/sakai*.jar

cp -R tomcat/common $tomcat
cp -R tomcat/components $tomcat
cp -R tomcat/shared/* $tomcat/shared
cp -R tomcat/server/* $tomcat/server
cp -R tomcat/webapps/* $tomcat/webapps

rm -rf $tomcat/webapps/access
rm -rf $tomcat/webapps/authn
rm -rf $tomcat/webapps/courier
rm -rf $tomcat/webapps/dav
rm -rf $tomcat/webapps/library
rm -rf $tomcat/webapps/meleteDocs
rm -rf $tomcat/webapps/mercury
rm -rf $tomcat/webapps/osp-common-tool
rm -rf $tomcat/webapps/osp-glossary-tool
rm -rf $tomcat/webapps/osp-jsf-example
rm -rf $tomcat/webapps/osp-jsf-resource
rm -rf $tomcat/webapps/osp-matrix-tool
rm -rf $tomcat/webapps/osp-portal-tool
rm -rf $tomcat/webapps/osp-portal
rm -rf $tomcat/webapps/osp-presentation-tool
rm -rf $tomcat/webapps/osp-reports-tool
rm -rf $tomcat/webapps/osp-wizard-tool
rm -rf $tomcat/webapps/podcasts
rm -rf $tomcat/webapps/portal-render
rm -rf $tomcat/webapps/portal
rm -rf $tomcat/webapps/rsmart-customizer-tool
rm -rf $tomcat/webapps/rsmart-help-content
rm -rf $tomcat/webapps/sakai-alias-tool
rm -rf $tomcat/webapps/sakai-announcement-tool
rm -rf $tomcat/webapps/sakai-archive-tool
rm -rf $tomcat/webapps/sakai-assignment-tool
rm -rf $tomcat/webapps/sakai-authz-tool
rm -rf $tomcat/webapps/sakai-axis
rm -rf $tomcat/webapps/sakai-blogger-tool
rm -rf $tomcat/webapps/sakai-calendar-summary-tool
rm -rf $tomcat/webapps/sakai-calendar-tool
rm -rf $tomcat/webapps/sakai-chat-tool
rm -rf $tomcat/webapps/sakai-content-tool
rm -rf $tomcat/webapps/sakai-discussion-tool
rm -rf $tomcat/webapps/sakai-dp-tool
rm -rf $tomcat/webapps/sakai-fck-connector
rm -rf $tomcat/webapps/sakai-gmt-tool
rm -rf $tomcat/webapps/sakai-gradebook-testservice
rm -rf $tomcat/webapps/sakai-gradebook-tool
rm -rf $tomcat/webapps/sakai-help-tool
rm -rf $tomcat/webapps/sakai-jforum-tool
rm -rf $tomcat/webapps/sakai-jsf-example
rm -rf $tomcat/webapps/sakai-jsf-resource
rm -rf $tomcat/webapps/sakai-login-tool
rm -rf $tomcat/webapps/sakai-mailarchive-james
rm -rf $tomcat/webapps/sakai-mailarchive-tool
rm -rf $tomcat/webapps/sakai-melete-tool
rm -rf $tomcat/webapps/sakai-memory-tool
rm -rf $tomcat/webapps/sakai-messageforums-tool
rm -rf $tomcat/webapps/sakai-message-tool
rm -rf $tomcat/webapps/sakai-metaobj-tool
rm -rf $tomcat/webapps/sakai-news-tool
rm -rf $tomcat/webapps/sakai-osid-unit-test
rm -rf $tomcat/webapps/sakai-podcasts
rm -rf $tomcat/webapps/sakai-postem-tool
rm -rf $tomcat/webapps/sakai-presence-tool
rm -rf $tomcat/webapps/sakai-presentation-tool
rm -rf $tomcat/webapps/sakai-profile-tool
rm -rf $tomcat/webapps/sakai-reset-pass
rm -rf $tomcat/webapps/sakai-rights-tool
rm -rf $tomcat/webapps/sakai-roster-tool
rm -rf $tomcat/webapps/sakai-rutgers-linktool
rm -rf $tomcat/webapps/sakai-rwiki-tool
rm -rf $tomcat/webapps/sakai-sample-tool-jsf
rm -rf $tomcat/webapps/sakai-sample-tool-servlet
rm -rf $tomcat/webapps/sakai-scheduler-tool
rm -rf $tomcat/webapps/sakai-search-tool
rm -rf $tomcat/webapps/sakai-sections-tool
rm -rf $tomcat/webapps/sakai-site-manage-tool
rm -rf $tomcat/webapps/sakai-site-pageorder-helper
rm -rf $tomcat/webapps/sakai-sitestats-tool
rm -rf $tomcat/webapps/sakai-site-tool
rm -rf $tomcat/webapps/sakai-syllabus-tool
rm -rf $tomcat/webapps/sakai-tool-tool-su
rm -rf $tomcat/webapps/sakai-usermembership-tool
rm -rf $tomcat/webapps/sakai-user-tool-admin-prefs
rm -rf $tomcat/webapps/sakai-user-tool-prefs
rm -rf $tomcat/webapps/sakai-user-tool
rm -rf $tomcat/webapps/sakai-web-tool
rm -rf $tomcat/webapps/samigo
rm -rf $tomcat/webapps/tool
rm -rf $tomcat/webapps/web

