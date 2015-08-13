-- This is the MYSQL Sakai 2.7.0 -> 2.7.1 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.7.0 to 2.7.1.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement
-- --------------------------------------------------------------------------------------------------------------------------------------

-- 2.7.0 CORRECTIONS

-- SAK-18863 change default value (from '0' to 0)
-- Uncomment the following ALTER TABLE statement if you ran the sakai_2_7_0_mysql_conversion.sql
-- prior to 2.7.x r81038 (10 Aug 2010 17:10:04 PDT).
-- ALTER TABLE osp_presentation ADD isCollab bit DEFAULT 0;

-- SAK-18648 osp_workflow_parent table name is not recognized as upper case OSP_WORKFLOW_PARENT
-- Users have reported case-sensitivity issues; uncomment and run the following conversion script
-- if you ran the sakai_2_7_0_mysql_conversion.sql prior to 2.7.x r81041 (10 Aug 2010 18:15:57 PDT).
-- insert into osp_workflow_parent select s.id, null, null, null, null, null, null from osp_scaffolding s where s.id not in (select wp.id from osp_workflow_parent wp);

-- 2.7.1 CHANGES

-- PRFL-94 remove twitter from preferences
alter table PROFILE_PREFERENCES_T drop TWITTER_ENABLED;
alter table PROFILE_PREFERENCES_T drop TWITTER_USERNAME;
alter table PROFILE_PREFERENCES_T drop TWITTER_PASSWORD;

-- PRFL-94 add external integration table
create table if not exists PROFILE_EXTERNAL_INTEGRATION_T (
	USER_UUID varchar(99) not null,
	TWITTER_TOKEN varchar(255),
	TWITTER_SECRET varchar(255),
	primary key (USER_UUID)
    );

-- SAK-5742 create SAKAI_PERSON_T indexes
create index SAKAI_PERSON_SURNAME_I on SAKAI_PERSON_T (SURNAME);
create index SAKAI_PERSON_ferpaEnabled_I on SAKAI_PERSON_T (ferpaEnabled);
create index SAKAI_PERSON_GIVEN_NAME_I on SAKAI_PERSON_T (GIVEN_NAME);
create index SAKAI_PERSON_UID_I on SAKAI_PERSON_T (UID_C);


-- Elluminate Bridge changes, some table where not auto incrementing which was causing clustering issues
alter table ell_configuration modify ID bigint(20) NOT NULL AUTO_INCREMENT;
alter table ELL_SESSION_DATA modify ID bigint(20) NOT NULL AUTO_INCREMENT;

-- fixes slowness in message bundle queries
create index SMB_SEARCH on sakai_message_bundle (BASENAME , MODULE_NAME , LOCALE , PROP_VALUE );


-- make sure recommended indices exist for performance considerations
create index SST_EVENTS_USER_ID_IX on SST_EVENTS (USER_ID);
create index SST_EVENTS_SITE_ID_IX on SST_EVENTS (SITE_ID);
create index SST_EVENTS_SITEEVENTUSER_ID_IX on SST_EVENTS (USER_ID, SITE_ID, EVENT_ID);
create index SST_EVENTS_EVENT_ID_IX on SST_EVENTS (EVENT_ID);
create index SST_EVENTS_DATE_IX on SST_EVENTS (EVENT_DATE);
create index SST_PREFERENCES_SITE_ID_IX on SST_PREFERENCES (SITE_ID);
create index SST_PRESENCE_DATE_IX on SST_PRESENCES (DATE);
create index SST_PRESENCE_USER_ID_IX on SST_PRESENCES (USER_ID);
create index SST_PRESENCE_SITE_ID_IX on SST_PRESENCES (SITE_ID);
create index SST_PRESENCE_SITEUSERDATE_ID_IX on SST_PRESENCES (SITE_ID, USER_ID, DATE);
create index SST_REPORTS_SITE_ID_IX on SST_REPORTS (SITE_ID);
create index SST_RESOURCES_USER_ID_IX on SST_RESOURCES (USER_ID);
create index SST_RESOURCES_SITE_ID_IX on SST_RESOURCES (SITE_ID);
create index SST_RESOURCES_RES_ACT_IDX on SST_RESOURCES (RESOURCE_ACTION);
create index SST_RESOURCES_DATE_IX on SST_RESOURCES (RESOURCE_DATE);
create index SST_SITEACTIVITY_EVENT_ID_IX on SST_SITEACTIVITY (EVENT_ID);
create index SST_SITEACTIVITY_DATE_IX on SST_SITEACTIVITY (ACTIVITY_DATE);
create index SST_SITEACTIVITY_SITE_ID_IX on SST_SITEACTIVITY (SITE_ID);
create index SST_SITEVISITS_DATE_IX on SST_SITEVISITS (VISITS_DATE);
create index SST_SITEVISITS_SITE_ID_IX on SST_SITEVISITS (SITE_ID);


--MSGCNTR-360
--Hibernate could have missed this index, if this fails, then the index may already be in the table
CREATE INDEX user_type_context_idx ON MFR_PVT_MSG_USR_T ( USER_ID(36), TYPE_UUID(36), CONTEXT_ID(36), READ_STATUS);


