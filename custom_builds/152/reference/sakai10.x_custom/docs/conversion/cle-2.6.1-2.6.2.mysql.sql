-- SAK-16668
-- Note: If you upgraded to sakai 2.6.0 PRIOR TO September 1st 2009 you'll need to run this column conversion.
-- You should probably check your columns as they should be converted from varchar to text
-- (Or just run them anyway as it is safe to rerun, which is why they are uncommented)
ALTER TABLE ASN_MA_ITEM_T CHANGE TEXT TEXT TEXT;
ALTER TABLE ASN_NOTE_ITEM_T CHANGE NOTE NOTE TEXT;

--SAK-16548 - Incorrect internationalization showing the grade NO GRADE

-- Note these are all mostly hex replacements as I could not find a better way to do it in mysql like in oracle.
--assignment_zh_CN.properties
update assignment_submission set xml = unhex(replace(hex(xml),'22E697A0E8AF84E5888622',hex('"gen.nograd"'))) where xml like '%graded="true"%';
--assignment_ar.properties
update assignment_submission set xml = unhex(replace(hex(xml),'22D984D8A720D8AAD988D8ACD8AF20D8A3D98A20D8AFD8B1D8ACD8A92E22',hex('"gen.nograd"'))) where xml like '%graded="true"%';
--assignment_pt_BR.properties
update assignment_submission set xml = unhex(replace(hex(xml),'224E656E68756D61204176616C6961C3A7C3A36F22',hex('"gen.nograd"'))) where xml like '%graded="true"%';
--assignment_es.properties
update assignment_submission set xml = replace(xml,'"No hay calificaci—n"','"gen.nograd"') where xml like '%graded="true"%';
--assignment_ko.properties
update assignment_submission set xml = unhex(replace(hex(xml),'22ED9599ECA09020EC9786EC9D8C22',hex('"gen.nograd"'))) where xml like '%graded="true"%';
--assignment_eu.propertie
update assignment_submission set xml = replace(xml,'"Kalifikatu gabe"','"gen.nograd"') where xml like '%graded="true"%';
--assignment_nl.properties
update assignment_submission set xml = replace(xml,'"Zonder beoordeling"','"gen.nograd"') where xml like '%graded="true"%';
--assignment_fr_CA.properties
update assignment_submission set xml = replace(xml,'"Aucune note"','"gen.nograd"') where xml like '%graded="true"%';
--assignment_en_GB.properties
update assignment_submission set xml = replace(xml,'"No Mark"','"gen.nograd"') where xml like '%graded="true"%';
--assignment.properties
update assignment_submission set xml = replace(xml,'"No Grade"','"gen.nograd"') where xml like '%graded="true"%';
--assignment_ca.properties
update assignment_submission set xml = unhex(replace(hex(xml),'224E6F206861792063616C69666963616369C3B36E22',hex('"gen.nograd"'))) where xml like '%graded="true"%';
--assignment_pt_PT.properties
update assignment_submission set xml = unhex(replace(hex(xml),'2253656D206176616C6961C3A7C3A36F22',hex('"gen.nograd"'))) where xml like '%graded="true"%';
--assignment_ru.properties
update assignment_submission set xml = unhex(replace(hex(xml),'22D091D0B5D0B720D0BED186D0B5D0BDD0BAD0B822',hex('"gen.nograd"'))) where xml like '%graded="true"%';
--assignment_sv.properties
update assignment_submission set xml = unhex(replace(hex(xml),'22426574796773C3A474747320656A22',hex('"gen.nograd"'))) where xml like '%graded="true"%';
--assignment_ja.properties
update assignment_submission set xml = unhex(replace(hex(xml),'22E68EA1E782B9E38197E381AAE3818422',hex('"gen.nograd"'))) where xml like '%graded="true"%';
--assignment_zh_TW.properties
update assignment_submission set xml = unhex(replace(hex(xml),'22E6B292E69C89E8A995E5888622',hex('"gen.nograd"'))) where xml like '%graded="true"%';

-- SAK-16847  asn.share.drafts permission should be added into 2.6.1 conversion script
-- This might have been added with the 2.6.0 conversion but was added after release

-- Don't do anything if the function already exists
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'asn.share.drafts') on duplicate key update function_name=function_name;

-- Two recent Jira's (http://jira.sakaiproject.org/browse/SAK-17061) and (http://jira.sakaiproject.org/browse/PRFL-97)
-- have uncovered that when the field 'locked' was added to the SAKAI_PERSON_T a while ago
-- (before 2.5.0 was cut), there was no DB upgrade script added to upgrade existing entries.

-- Here is the Jira that added the locked field: http://jira.sakaiproject.org/browse SAK-10512

--As such, this field is null for old profiles. Its set correctly for any new profiles but all old entries need to be converted.

update SAKAI_PERSON_T set locked=false where locked=null;

-- This is the MySQL SiteStats 1.x -> 2.0 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- Run this before you run your first app server with the updated SiteStats.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------


-- Create new reports table
create table SST_REPORTS (ID bigint not null auto_increment, SITE_ID varchar(99), TITLE varchar(255) not null, DESCRIPTION longtext, HIDDEN bit, REPORT_DEF text not null, CREATED_BY varchar(99) not null, CREATED_ON datetime not null, MODIFIED_BY varchar(99), MODIFIED_ON datetime, primary key (ID));
create index SST_REPORTS_SITE_ID_IX on SST_REPORTS (SITE_ID);

-- STAT-35: Preload with default reports
--   0) Activity total (Show activity in site, with totals per event.)
insert  into `SST_REPORTS`(`SITE_ID`,`TITLE`,`DESCRIPTION`,`HIDDEN`,`REPORT_DEF`,`CREATED_BY`,`CREATED_ON`,`MODIFIED_BY`,`MODIFIED_ON`) values (NULL,'${predefined_report0_title}','${predefined_report0_description}',0,'<?xml version=\'1.0\' ?><ReportParams><howChartCategorySource>none</howChartCategorySource><howChartSeriesSource>total</howChartSeriesSource><howChartSource>event</howChartSource><howChartType>pie</howChartType><howLimitedMaxResults>false</howLimitedMaxResults><howMaxResults>0</howMaxResults><howPresentationMode>how-presentation-both</howPresentationMode><howSort>true</howSort><howSortAscending>true</howSortAscending><howSortBy>event</howSortBy><howTotalsBy><howTotalsBy>event</howTotalsBy></howTotalsBy><siteId/><what>what-events</what><whatEventIds/><whatEventSelType>what-events-bytool</whatEventSelType><whatLimitedAction>false</whatLimitedAction><whatLimitedResourceIds>false</whatLimitedResourceIds><whatResourceAction>new</whatResourceAction><whatResourceIds/><whatToolIds><whatToolIds>all</whatToolIds></whatToolIds><when>when-all</when><whenFrom/><whenTo/><who>who-all</who><whoGroupId/><whoRoleId>access</whoRoleId><whoUserIds/></ReportParams>','preload',now(),'preload',now());
--   1) Most accessed files (Show top 10 most accessed files.)
insert  into `SST_REPORTS`(`SITE_ID`,`TITLE`,`DESCRIPTION`,`HIDDEN`,`REPORT_DEF`,`CREATED_BY`,`CREATED_ON`,`MODIFIED_BY`,`MODIFIED_ON`) values (NULL,'${predefined_report1_title}','${predefined_report1_description}',0,'<?xml version=\'1.0\' ?><ReportParams><howChartCategorySource>none</howChartCategorySource><howChartSeriesSource>total</howChartSeriesSource><howChartSource>resource</howChartSource><howChartType>pie</howChartType><howLimitedMaxResults>true</howLimitedMaxResults><howMaxResults>10</howMaxResults><howPresentationMode>how-presentation-both</howPresentationMode><howSort>true</howSort><howSortAscending>false</howSortAscending><howSortBy>total</howSortBy><howTotalsBy><howTotalsBy>resource</howTotalsBy></howTotalsBy><siteId/><what>what-resources</what><whatEventIds/><whatEventSelType>what-events-bytool</whatEventSelType><whatLimitedAction>true</whatLimitedAction><whatLimitedResourceIds>false</whatLimitedResourceIds><whatResourceAction>read</whatResourceAction><whatResourceIds/><whatToolIds><whatToolIds>all</whatToolIds></whatToolIds><when>when-all</when><whenFrom/><whenTo/><who>who-all</who><whoGroupId/><whoRoleId>access</whoRoleId><whoUserIds/></ReportParams>','preload',now(),'preload',now());
--   2) Most active users (Show top 10 users with most activity in site.)
insert  into `SST_REPORTS`(`SITE_ID`,`TITLE`,`DESCRIPTION`,`HIDDEN`,`REPORT_DEF`,`CREATED_BY`,`CREATED_ON`,`MODIFIED_BY`,`MODIFIED_ON`) values (NULL,'${predefined_report2_title}','${predefined_report2_description}',0,'<?xml version=\'1.0\' ?><ReportParams><howChartCategorySource>none</howChartCategorySource><howChartSeriesSource>total</howChartSeriesSource><howChartSource>user</howChartSource><howChartType>pie</howChartType><howLimitedMaxResults>true</howLimitedMaxResults><howMaxResults>10</howMaxResults><howPresentationMode>how-presentation-both</howPresentationMode><howSort>true</howSort><howSortAscending>false</howSortAscending><howSortBy>total</howSortBy><howTotalsBy><howTotalsBy>user</howTotalsBy></howTotalsBy><siteId/><what>what-events</what><whatEventIds/><whatEventSelType>what-events-bytool</whatEventSelType><whatLimitedAction>false</whatLimitedAction><whatLimitedResourceIds>false</whatLimitedResourceIds><whatResourceAction>new</whatResourceAction><whatResourceIds/><whatToolIds><whatToolIds>all</whatToolIds></whatToolIds><when>when-all</when><whenFrom/><whenTo/><who>who-all</who><whoGroupId/><whoRoleId>access</whoRoleId><whoUserIds/></ReportParams>','preload',now(),'preload',now());
--   3) Less active users (Show top 10 users with less activity in site.)
insert  into `SST_REPORTS`(`SITE_ID`,`TITLE`,`DESCRIPTION`,`HIDDEN`,`REPORT_DEF`,`CREATED_BY`,`CREATED_ON`,`MODIFIED_BY`,`MODIFIED_ON`) values (NULL,'${predefined_report3_title}','${predefined_report3_description}',0,'<?xml version=\'1.0\' ?><ReportParams><howChartCategorySource>none</howChartCategorySource><howChartSeriesSource>total</howChartSeriesSource><howChartSource>user</howChartSource><howChartType>bar</howChartType><howLimitedMaxResults>false</howLimitedMaxResults><howMaxResults>0</howMaxResults><howPresentationMode>how-presentation-both</howPresentationMode><howSort>true</howSort><howSortAscending>true</howSortAscending><howSortBy>total</howSortBy><howTotalsBy><howTotalsBy>user</howTotalsBy></howTotalsBy><siteId/><what>what-events</what><whatEventIds/><whatEventSelType>what-events-bytool</whatEventSelType><whatLimitedAction>false</whatLimitedAction><whatLimitedResourceIds>false</whatLimitedResourceIds><whatResourceAction>new</whatResourceAction><whatResourceIds/><whatToolIds><whatToolIds>all</whatToolIds></whatToolIds><when>when-all</when><whenFrom/><whenTo/><who>who-all</who><whoGroupId/><whoRoleId>access</whoRoleId><whoUserIds/></ReportParams>','preload',now(),'preload',now());
--   4) Users with more visits (Show top 10 users who have most visited the site.)
insert  into `SST_REPORTS`(`SITE_ID`,`TITLE`,`DESCRIPTION`,`HIDDEN`,`REPORT_DEF`,`CREATED_BY`,`CREATED_ON`,`MODIFIED_BY`,`MODIFIED_ON`) values (NULL,'${predefined_report4_title}','${predefined_report4_description}',0,'<?xml version=\'1.0\' ?><ReportParams><howChartCategorySource>none</howChartCategorySource><howChartSeriesSource>total</howChartSeriesSource><howChartSource>user</howChartSource><howChartType>bar</howChartType><howLimitedMaxResults>false</howLimitedMaxResults><howMaxResults>0</howMaxResults><howPresentationMode>how-presentation-both</howPresentationMode><howSort>true</howSort><howSortAscending>false</howSortAscending><howSortBy>total</howSortBy><howTotalsBy><howTotalsBy>user</howTotalsBy></howTotalsBy><siteId/><what>what-visits</what><whatEventIds/><whatEventSelType>what-events-bytool</whatEventSelType><whatLimitedAction>false</whatLimitedAction><whatLimitedResourceIds>false</whatLimitedResourceIds><whatResourceAction>new</whatResourceAction><whatResourceIds/><whatToolIds><whatToolIds>all</whatToolIds></whatToolIds><when>when-all</when><whenFrom/><whenTo/><who>who-all</who><whoGroupId/><whoRoleId>access</whoRoleId><whoUserIds/></ReportParams>','preload',now(),'preload',now());
--   5) Users with no visits (Show users who have never visited the site.)
insert  into `SST_REPORTS`(`SITE_ID`,`TITLE`,`DESCRIPTION`,`HIDDEN`,`REPORT_DEF`,`CREATED_BY`,`CREATED_ON`,`MODIFIED_BY`,`MODIFIED_ON`) values (NULL,'${predefined_report5_title}','${predefined_report5_description}',0,'<?xml version=\'1.0\' ?><ReportParams><howChartCategorySource>none</howChartCategorySource><howChartSeriesSource>total</howChartSeriesSource><howChartSource>event</howChartSource><howChartType>bar</howChartType><howLimitedMaxResults>false</howLimitedMaxResults><howMaxResults>0</howMaxResults><howPresentationMode>how-presentation-table</howPresentationMode><howSort>false</howSort><howSortAscending>false</howSortAscending><howSortBy>default</howSortBy><howTotalsBy><howTotalsBy>user</howTotalsBy></howTotalsBy><siteId/><what>what-visits</what><whatEventIds/><whatEventSelType>what-events-bytool</whatEventSelType><whatLimitedAction>false</whatLimitedAction><whatLimitedResourceIds>false</whatLimitedResourceIds><whatResourceAction>new</whatResourceAction><whatResourceIds/><whatToolIds><whatToolIds>all</whatToolIds></whatToolIds><when>when-all</when><whenFrom/><whenTo/><who>who-none</who><whoGroupId/><whoRoleId>access</whoRoleId><whoUserIds/></ReportParams>','preload',now(),'preload',now());
--   6) Users with no activity (Show users with no activity in site.)
insert  into `SST_REPORTS`(`SITE_ID`,`TITLE`,`DESCRIPTION`,`HIDDEN`,`REPORT_DEF`,`CREATED_BY`,`CREATED_ON`,`MODIFIED_BY`,`MODIFIED_ON`) values (NULL,'${predefined_report6_title}','${predefined_report6_description}',0,'<?xml version=\'1.0\' ?><ReportParams><howChartCategorySource>none</howChartCategorySource><howChartSeriesPeriod>byday</howChartSeriesPeriod><howChartSeriesSource>total</howChartSeriesSource><howChartSource>event</howChartSource><howChartType>bar</howChartType><howLimitedMaxResults>false</howLimitedMaxResults><howMaxResults>0</howMaxResults><howPresentationMode>how-presentation-table</howPresentationMode><howSort>false</howSort><howSortAscending>true</howSortAscending><howSortBy>default</howSortBy><howTotalsBy><howTotalsBy>user</howTotalsBy></howTotalsBy><siteId/><what>what-events</what><whatEventIds/><whatEventSelType>what-events-bytool</whatEventSelType><whatLimitedAction>false</whatLimitedAction><whatLimitedResourceIds>false</whatLimitedResourceIds><whatResourceAction>new</whatResourceAction><whatResourceIds/><whatToolIds><whatToolIds>all</whatToolIds></whatToolIds><when>when-all</when><whenFrom/><whenTo/><who>who-none</who><whoGroupId/><whoRoleId>access</whoRoleId><whoUserIds/></ReportParams>','preload',now(),'preload',now());

-- STAT-151: Improve getTotalSiteUniqueVisits() execution time
create index SST_EVENTS_SITEEVENTUSER_ID_IX on SST_EVENTS (SITE_ID,EVENT_ID,USER_ID);

----------------------------------------------------------------------------------
-- This is the MySQL JForum 2.5 to JForum 2.6.4 conversion script
-----------------------------------------------------------------------------------------------------
--change the column data type from TEXT to LONGTEXT to avoid Data truncation error
ALTER TABLE jforum_posts_text MODIFY COLUMN post_text LONGTEXT DEFAULT NULL;

--change the column data type from TEXT to LONGTEXT to avoid Data truncation error
ALTER TABLE jforum_privmsgs_text MODIFY COLUMN privmsgs_text LONGTEXT DEFAULT NULL;

--add privmsgs_flag_to_follow to jforum_privmsgs
ALTER TABLE jforum_privmsgs ADD COLUMN privmsgs_flag_to_follow TINYINT(1) NOT NULL DEFAULT 0 AFTER privmsgs_attachments;

--add privmsgs_replied to jforum_privmsgs
ALTER TABLE jforum_privmsgs ADD COLUMN privmsgs_replied TINYINT(1) NOT NULL DEFAULT 0 AFTER privmsgs_flag_to_follow;

