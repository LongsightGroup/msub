-- NOTE: It is possible that your xml column in assignment_submission may be a long type. You need to convert it to a clob if this is the case.
--       The following SQL will fail to run if the column is a long.

--       This seems to be either clob or long (It needs to be a clob)

alter table assignment_submission modify xml clob;

-- SAK-16668
-- If you upgraded to 2.6.0 PRIOR TO September 1st 2009, you will need to run the conversions in the comments. It converts some additional assignment columns to clob.

-- asn_note_item_t note column needs to be clob but is probably varchar
-- asn_ma_item_t text column needs to be clob but is probably varchar


alter table asn_note_item_t add note_clob clob;
update asn_note_item_t set note_clob = note;
alter table asn_note_item_t drop column note;
alter table asn_note_item_t rename column note_clob to note;


alter table asn_ma_item_t add text_clob clob;
update asn_ma_item_t set text_clob = text;
alter table asn_ma_item_t drop column text;
alter table asn_ma_item_t rename column text_clob to text;

-- Note after performing a conversion to clob your indexes may be in an invalid/unusable state.
-- You will need to run ths following statement, and manually execute the generated 'alter indexes' and re-gather statistics on this table.
-- There are randomly named indexes so it can not be automated.

-- select 'alter index '||index_name||' rebuild online;' from user_indexes where status = 'INVALID' or status = 'UNUSABLE';

-- After the field(s) are clobs continue with the updates

--SAK-16548 - Incorrect internationalization showing the grade NO GRADE
-- Values pulled from gen.nograd in ./assignment-bundles/assignment_*.properties
--assignment_zh_CN.properties
update assignment_submission set xml = replace(xml,unistr('"\65E0\8BC4\5206"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_ar.properties
update assignment_submission set xml = replace(xml,unistr('"\0644\0627 \062A\0648\062C\062F \0623\064A \062F\0631\062C\0629."'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_pt_BR.properties
update assignment_submission set xml = replace(xml,unistr('"Nenhuma Avalia\00e7\00e3o"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_es.properties
update assignment_submission set xml = replace(xml,unistr('"No hay calificaci\00F3n"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_ko.properties
update assignment_submission set xml = replace(xml,unistr('"\d559\c810 \c5c6\c74c"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_eu.propertie
update assignment_submission set xml = replace(xml,unistr('"Kalifikatu gabe"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_nl.properties
update assignment_submission set xml = replace(xml,unistr('"Zonder beoordeling"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_fr_CA.properties
update assignment_submission set xml = replace(xml,unistr('"Aucune note"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_en_GB.properties
update assignment_submission set xml = replace(xml,unistr('"No Mark"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment.properties
update assignment_submission set xml = replace(xml,unistr('"No Grade"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_ca.properties
update assignment_submission set xml = replace(xml,unistr('"No hi ha qualificaci—"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_pt_PT.properties
update assignment_submission set xml = replace(xml,unistr('"Sem avalia\00E7\00E3o"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_ru.properties
update assignment_submission set xml = replace(xml,unistr('"\0411\0435\0437 \043e\0446\0435\043d\043a\0438"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_sv.properties
update assignment_submission set xml = replace(xml,unistr('"Betygs\00E4tts ej"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_ja.properties
update assignment_submission set xml = replace(xml,unistr('"\63a1\70b9\3057\306a\3044"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_zh_TW.properties
update assignment_submission set xml = replace(xml,unistr('"\6c92\6709\8a55\5206"'),'"gen.nograd"') where xml like '%graded="true"%';

-- SAK-16847  asn.share.drafts permission should be added into 2.6.1 conversion script
-- This might have been added with the 2.6.0 conversion but was added after release

MERGE INTO SAKAI_REALM_FUNCTION a USING (
     SELECT 'asn.share.drafts' as FUNCTION_NAME from dual) b
 ON (a.FUNCTION_NAME = b.FUNCTION_NAME)
 WHEN NOT MATCHED THEN INSERT (FUNCTION_KEY,FUNCTION_NAME) VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'asn.share.drafts');

-- Two recent Jira's (http://jira.sakaiproject.org/browse/SAK-17061) and (http://jira.sakaiproject.org/browse/PRFL-97)
-- have uncovered that when the field 'locked' was added to the SAKAI_PERSON_T a while ago
-- (before 2.5.0 was cut), there was no DB upgrade script added to upgrade existing entries.

-- Here is the Jira that added the locked field: http://jira.sakaiproject.org/browse SAK-10512

--As such, this field is null for old profiles. Its set correctly for any new profiles but all old entries need to be converted.

update SAKAI_PERSON_T set locked=0 where locked=null;

-- This is the MySQL SiteStats 1.x -> 2.0 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- Run this before you run your first app server with the updated SiteStats.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------


-- Create new reports table
create table SST_REPORTS (ID number(19,0) not null, SITE_ID varchar2(99 char), TITLE varchar2(255 char) not null, DESCRIPTION clob, HIDDEN number(1,0), REPORT_DEF clob not null, CREATED_BY varchar2(99 char) not null, CREATED_ON timestamp not null, MODIFIED_BY varchar2(99 char), MODIFIED_ON timestamp, primary key (ID));
create index SST_REPORTS_SITE_ID_IX on SST_REPORTS (SITE_ID);
create sequence SST_REPORTS_ID;

-- STAT-35: Preload with default reports
--   0) Activity total (Show activity in site, with totals per event.)
insert into SST_REPORTS (ID,SITE_ID,TITLE,DESCRIPTION,HIDDEN,REPORT_DEF,CREATED_BY,CREATED_ON,MODIFIED_BY,MODIFIED_ON) values (SST_REPORTS_ID.NEXTVAL,NULL,'${predefined_report0_title}','${predefined_report0_description}',0,'<?xml version=''1.0'' ?><ReportParams><howChartCategorySource>none</howChartCategorySource><howChartSeriesSource>total</howChartSeriesSource><howChartSource>event</howChartSource><howChartType>pie</howChartType><howLimitedMaxResults>false</howLimitedMaxResults><howMaxResults>0</howMaxResults><howPresentationMode>how-presentation-both</howPresentationMode><howSort>true</howSort><howSortAscending>true</howSortAscending><howSortBy>event</howSortBy><howTotalsBy><howTotalsBy>event</howTotalsBy></howTotalsBy><siteId/><what>what-events</what><whatEventIds/><whatEventSelType>what-events-bytool</whatEventSelType><whatLimitedAction>false</whatLimitedAction><whatLimitedResourceIds>false</whatLimitedResourceIds><whatResourceAction>new</whatResourceAction><whatResourceIds/><whatToolIds><whatToolIds>all</whatToolIds></whatToolIds><when>when-all</when><whenFrom/><whenTo/><who>who-all</who><whoGroupId/><whoRoleId>access</whoRoleId><whoUserIds/></ReportParams>','preload',(SELECT current_date FROM dual),'preload',(SELECT current_date FROM dual));
--   1) Most accessed files (Show top 10 most accessed files.)
insert into SST_REPORTS (ID,SITE_ID,TITLE,DESCRIPTION,HIDDEN,REPORT_DEF,CREATED_BY,CREATED_ON,MODIFIED_BY,MODIFIED_ON) values (SST_REPORTS_ID.NEXTVAL,NULL,'${predefined_report1_title}','${predefined_report1_description}',0,'<?xml version=''1.0'' ?><ReportParams><howChartCategorySource>none</howChartCategorySource><howChartSeriesSource>total</howChartSeriesSource><howChartSource>resource</howChartSource><howChartType>pie</howChartType><howLimitedMaxResults>true</howLimitedMaxResults><howMaxResults>10</howMaxResults><howPresentationMode>how-presentation-both</howPresentationMode><howSort>true</howSort><howSortAscending>false</howSortAscending><howSortBy>total</howSortBy><howTotalsBy><howTotalsBy>resource</howTotalsBy></howTotalsBy><siteId/><what>what-resources</what><whatEventIds/><whatEventSelType>what-events-bytool</whatEventSelType><whatLimitedAction>true</whatLimitedAction><whatLimitedResourceIds>false</whatLimitedResourceIds><whatResourceAction>read</whatResourceAction><whatResourceIds/><whatToolIds><whatToolIds>all</whatToolIds></whatToolIds><when>when-all</when><whenFrom/><whenTo/><who>who-all</who><whoGroupId/><whoRoleId>access</whoRoleId><whoUserIds/></ReportParams>','preload',(SELECT current_date FROM dual),'preload',(SELECT current_date FROM dual));
--   2) Most active users (Show top 10 users with most activity in site.)
insert into SST_REPORTS (ID,SITE_ID,TITLE,DESCRIPTION,HIDDEN,REPORT_DEF,CREATED_BY,CREATED_ON,MODIFIED_BY,MODIFIED_ON) values (SST_REPORTS_ID.NEXTVAL,NULL,'${predefined_report2_title}','${predefined_report2_description}',0,'<?xml version=''1.0'' ?><ReportParams><howChartCategorySource>none</howChartCategorySource><howChartSeriesSource>total</howChartSeriesSource><howChartSource>user</howChartSource><howChartType>pie</howChartType><howLimitedMaxResults>true</howLimitedMaxResults><howMaxResults>10</howMaxResults><howPresentationMode>how-presentation-both</howPresentationMode><howSort>true</howSort><howSortAscending>false</howSortAscending><howSortBy>total</howSortBy><howTotalsBy><howTotalsBy>user</howTotalsBy></howTotalsBy><siteId/><what>what-events</what><whatEventIds/><whatEventSelType>what-events-bytool</whatEventSelType><whatLimitedAction>false</whatLimitedAction><whatLimitedResourceIds>false</whatLimitedResourceIds><whatResourceAction>new</whatResourceAction><whatResourceIds/><whatToolIds><whatToolIds>all</whatToolIds></whatToolIds><when>when-all</when><whenFrom/><whenTo/><who>who-all</who><whoGroupId/><whoRoleId>access</whoRoleId><whoUserIds/></ReportParams>','preload',(SELECT current_date FROM dual),'preload',(SELECT current_date FROM dual));
--   3) Less active users (Show top 10 users with less activity in site.)
insert into SST_REPORTS (ID,SITE_ID,TITLE,DESCRIPTION,HIDDEN,REPORT_DEF,CREATED_BY,CREATED_ON,MODIFIED_BY,MODIFIED_ON) values (SST_REPORTS_ID.NEXTVAL,NULL,'${predefined_report3_title}','${predefined_report3_description}',0,'<?xml version=''1.0'' ?><ReportParams><howChartCategorySource>none</howChartCategorySource><howChartSeriesSource>total</howChartSeriesSource><howChartSource>user</howChartSource><howChartType>bar</howChartType><howLimitedMaxResults>false</howLimitedMaxResults><howMaxResults>0</howMaxResults><howPresentationMode>how-presentation-both</howPresentationMode><howSort>true</howSort><howSortAscending>true</howSortAscending><howSortBy>total</howSortBy><howTotalsBy><howTotalsBy>user</howTotalsBy></howTotalsBy><siteId/><what>what-events</what><whatEventIds/><whatEventSelType>what-events-bytool</whatEventSelType><whatLimitedAction>false</whatLimitedAction><whatLimitedResourceIds>false</whatLimitedResourceIds><whatResourceAction>new</whatResourceAction><whatResourceIds/><whatToolIds><whatToolIds>all</whatToolIds></whatToolIds><when>when-all</when><whenFrom/><whenTo/><who>who-all</who><whoGroupId/><whoRoleId>access</whoRoleId><whoUserIds/></ReportParams>','preload',(SELECT current_date FROM dual),'preload',(SELECT current_date FROM dual));
--   4) Users with more visits (Show top 10 users who have most visited the site.)
insert into SST_REPORTS (ID,SITE_ID,TITLE,DESCRIPTION,HIDDEN,REPORT_DEF,CREATED_BY,CREATED_ON,MODIFIED_BY,MODIFIED_ON) values (SST_REPORTS_ID.NEXTVAL,NULL,'${predefined_report4_title}','${predefined_report4_description}',0,'<?xml version=''1.0'' ?><ReportParams><howChartCategorySource>none</howChartCategorySource><howChartSeriesSource>total</howChartSeriesSource><howChartSource>user</howChartSource><howChartType>bar</howChartType><howLimitedMaxResults>false</howLimitedMaxResults><howMaxResults>0</howMaxResults><howPresentationMode>how-presentation-both</howPresentationMode><howSort>true</howSort><howSortAscending>false</howSortAscending><howSortBy>total</howSortBy><howTotalsBy><howTotalsBy>user</howTotalsBy></howTotalsBy><siteId/><what>what-visits</what><whatEventIds/><whatEventSelType>what-events-bytool</whatEventSelType><whatLimitedAction>false</whatLimitedAction><whatLimitedResourceIds>false</whatLimitedResourceIds><whatResourceAction>new</whatResourceAction><whatResourceIds/><whatToolIds><whatToolIds>all</whatToolIds></whatToolIds><when>when-all</when><whenFrom/><whenTo/><who>who-all</who><whoGroupId/><whoRoleId>access</whoRoleId><whoUserIds/></ReportParams>','preload',(SELECT current_date FROM dual),'preload',(SELECT current_date FROM dual));
--   5) Users with no visits (Show users who have never visited the site.)
insert into SST_REPORTS (ID,SITE_ID,TITLE,DESCRIPTION,HIDDEN,REPORT_DEF,CREATED_BY,CREATED_ON,MODIFIED_BY,MODIFIED_ON) values (SST_REPORTS_ID.NEXTVAL,NULL,'${predefined_report5_title}','${predefined_report5_description}',0,'<?xml version=''1.0'' ?><ReportParams><howChartCategorySource>none</howChartCategorySource><howChartSeriesSource>total</howChartSeriesSource><howChartSource>event</howChartSource><howChartType>bar</howChartType><howLimitedMaxResults>false</howLimitedMaxResults><howMaxResults>0</howMaxResults><howPresentationMode>how-presentation-table</howPresentationMode><howSort>false</howSort><howSortAscending>false</howSortAscending><howSortBy>default</howSortBy><howTotalsBy><howTotalsBy>user</howTotalsBy></howTotalsBy><siteId/><what>what-visits</what><whatEventIds/><whatEventSelType>what-events-bytool</whatEventSelType><whatLimitedAction>false</whatLimitedAction><whatLimitedResourceIds>false</whatLimitedResourceIds><whatResourceAction>new</whatResourceAction><whatResourceIds/><whatToolIds><whatToolIds>all</whatToolIds></whatToolIds><when>when-all</when><whenFrom/><whenTo/><who>who-none</who><whoGroupId/><whoRoleId>access</whoRoleId><whoUserIds/></ReportParams>','preload',(SELECT current_date FROM dual),'preload',(SELECT current_date FROM dual));
--   6) Users with no activity (Show users with no activity in site.)
insert into SST_REPORTS (ID,SITE_ID,TITLE,DESCRIPTION,HIDDEN,REPORT_DEF,CREATED_BY,CREATED_ON,MODIFIED_BY,MODIFIED_ON) values (SST_REPORTS_ID.NEXTVAL,NULL,'${predefined_report6_title}','${predefined_report6_description}',0,'<?xml version=''1.0'' ?><ReportParams><howChartCategorySource>none</howChartCategorySource><howChartSeriesPeriod>byday</howChartSeriesPeriod><howChartSeriesSource>total</howChartSeriesSource><howChartSource>event</howChartSource><howChartType>bar</howChartType><howLimitedMaxResults>false</howLimitedMaxResults><howMaxResults>0</howMaxResults><howPresentationMode>how-presentation-table</howPresentationMode><howSort>false</howSort><howSortAscending>true</howSortAscending><howSortBy>default</howSortBy><howTotalsBy><howTotalsBy>user</howTotalsBy></howTotalsBy><siteId/><what>what-events</what><whatEventIds/><whatEventSelType>what-events-bytool</whatEventSelType><whatLimitedAction>false</whatLimitedAction><whatLimitedResourceIds>false</whatLimitedResourceIds><whatResourceAction>new</whatResourceAction><whatResourceIds/><whatToolIds><whatToolIds>all</whatToolIds></whatToolIds><when>when-all</when><whenFrom/><whenTo/><who>who-none</who><whoGroupId/><whoRoleId>access</whoRoleId><whoUserIds/></ReportParams>','preload',(SELECT current_date FROM dual),'preload',(SELECT current_date FROM dual));

-- STAT-151: Improve getTotalSiteUniqueVisits() execution time
create index SST_EVENTS_SITEEVENTUSER_ID_IX on SST_EVENTS (SITE_ID,EVENT_ID,USER_ID);

----------------------------------------------------------------------------------
-- This is the Oracle JForum 2.5 to JForum 2.6.4 conversion script
-----------------------------------------------------------------------------------------------------
--add privmsgs_flag_to_follow to jforum_privmsgs
ALTER TABLE jforum_privmsgs ADD privmsgs_flag_to_follow NUMBER(10) DEFAULT 0 NOT NULL;

--add privmsgs_replied to jforum_privmsgs
ALTER TABLE jforum_privmsgs ADD privmsgs_replied NUMBER(10) DEFAULT 0 NOT NULL;