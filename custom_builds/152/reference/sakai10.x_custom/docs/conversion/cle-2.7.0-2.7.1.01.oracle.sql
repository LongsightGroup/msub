-- This is the Oracle Sakai 2.7.0 -> 2.7.1 conversion script
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

-- PRFL-94 remove twitter from preferences
-- NOTE: users will need to re-add their Twitter details to Profile2
alter table PROFILE_PREFERENCES_T drop column TWITTER_ENABLED;
alter table PROFILE_PREFERENCES_T drop column TWITTER_USERNAME;
alter table PROFILE_PREFERENCES_T drop column TWITTER_PASSWORD;

-- PRFL-94 add external integration table
-- NOTE: users will need to re-add their Twitter details to Profile2
create table PROFILE_EXTERNAL_INTEGRATION_T (
	USER_UUID varchar2(99) not null,
	TWITTER_TOKEN varchar2(255),
	TWITTER_SECRET varchar2(255),
	primary key (USER_UUID)
);
-- SAK-5742 create SAKAI_PERSON_T indexes
create index SAKAI_PERSON_SURNAME_I on SAKAI_PERSON_T (SURNAME);
create index SAKAI_PERSON_ferpaEnabled_I on SAKAI_PERSON_T (ferpaEnabled);
create index SAKAI_PERSON_GIVEN_NAME_I on SAKAI_PERSON_T (GIVEN_NAME);
create index SAKAI_PERSON_UID_I on SAKAI_PERSON_T (UID_C);

-- fixes slowness in message bundle queries
create index SMB_SEARCH on sakai_message_bundle (BASENAME , MODULE_NAME , LOCALE , PROP_VALUE );

--MSGCNTR-360
--Hibernate could have missed this index, if this fails, then the index may already be in the table
CREATE INDEX user_type_context_idx ON MFR_PVT_MSG_USR_T ( USER_ID(36), TYPE_UUID(36), CONTEXT_ID(36), READ_STATUS);

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
create sequence SST_EVENTS_ID;
create sequence SST_JOB_RUN_ID;
create sequence SST_PREFERENCES_ID;
create sequence SST_PRESENCE_ID;
create sequence SST_REPORTS_ID;
create sequence SST_RESOURCES_ID;
create sequence SST_SITEACTIVITY_ID;
create sequence SST_SITEVISITS_ID;
