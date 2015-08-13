-- This is the Oracle Sakai 2.7.1 -> 2.8.0 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.7.1 to 2.8.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement
-- --------------------------------------------------------------------------------------------------------------------------------------

-- SAK-8005
alter table ANNOUNCEMENT_MESSAGE add MESSAGE_ORDER INT;

-- SAK-20560
-- next three statements needed if the xml field is of type Long
-- alter table announcement_message modify xml clob;
-- select 'alter index '||index_name||' rebuild online;' from user_indexes where status = 'INVALID' or status = 'UNUSABLE';
-- execute all resulting statements from the previous step
update ANNOUNCEMENT_MESSAGE set MESSAGE_ORDER='1', XML=REPLACE(XML, ' subject=', ' message_order="1" subject=') WHERE MESSAGE_ORDER is null; 

-- SAK-17821 Add additional fields to SakaiPerson
alter table SAKAI_PERSON_T add STAFF_PROFILE CLOB;
alter table SAKAI_PERSON_T add UNIVERSITY_PROFILE_URL CLOB;
alter table SAKAI_PERSON_T add ACADEMIC_PROFILE_URL CLOB;
alter table SAKAI_PERSON_T add PUBLICATIONS CLOB;
alter table SAKAI_PERSON_T add BUSINESS_BIOGRAPHY CLOB;

-- Samigo
-- SAM-666
alter table SAM_ASSESSFEEDBACK_T add FEEDBACKCOMPONENTOPTION number(10,0) default null;
update SAM_ASSESSFEEDBACK_T set FEEDBACKCOMPONENTOPTION = 2;
alter table SAM_PUBLISHEDFEEDBACK_T add FEEDBACKCOMPONENTOPTION number(10,0) default null;
update SAM_PUBLISHEDFEEDBACK_T set FEEDBACKCOMPONENTOPTION = 2;

-- SAM-756 (SAK-16822): oracle only
alter table SAM_ITEMTEXT_T add (TEMP_CLOB_TEXT clob);
update SAM_ITEMTEXT_T SET TEMP_CLOB_TEXT = TEXT;
alter table SAM_ITEMTEXT_T drop column TEXT;
alter table SAM_ITEMTEXT_T rename column TEMP_CLOB_TEXT to TEXT;

alter table SAM_PUBLISHEDITEMTEXT_T add (TEMP_CLOB_TEXT clob);
update SAM_PUBLISHEDITEMTEXT_T SET TEMP_CLOB_TEXT = TEXT;
alter table SAM_PUBLISHEDITEMTEXT_T drop column TEXT;
alter table SAM_PUBLISHEDITEMTEXT_T rename column TEMP_CLOB_TEXT to TEXT;

alter table SAM_ITEMGRADING_T add (TEMP_CLOB_TEXT clob);
update SAM_ITEMGRADING_T SET TEMP_CLOB_TEXT = ANSWERTEXT;
alter table SAM_ITEMGRADING_T drop column ANSWERTEXT;
alter table SAM_ITEMGRADING_T rename column TEMP_CLOB_TEXT to ANSWERTEXT;

-- SAM-971
alter table SAM_ASSESSMENTGRADING_T add LASTVISITEDPART number(10,0) default null;
alter table SAM_ASSESSMENTGRADING_T add LASTVISITEDQUESTION number(10,0) default null;

-- Gradebook2 support
-- SAK-19080 / GRBK-736
alter table GB_GRADE_RECORD_T add USER_ENTERED_GRADE varchar2(255 CHAR);


--MSGCNTR-309
--Start and End dates on Forums and Topics
alter table MFR_AREA_T add (AVAILABILITY_RESTRICTED NUMBER(1,0));
update MFR_AREA_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_AREA_T modify (AVAILABILITY_RESTRICTED NUMBER(1,0) default 0 not null );

alter table MFR_AREA_T add (AVAILABILITY NUMBER(1,0));
update MFR_AREA_T set AVAILABILITY=1 where AVAILABILITY is NULL;
alter table MFR_AREA_T modify (AVAILABILITY NUMBER(1,0) default 1 not null);

alter table MFR_AREA_T add (OPEN_DATE timestamp);

alter table MFR_AREA_T add (CLOSE_DATE timestamp);


alter table MFR_OPEN_FORUM_T add (AVAILABILITY_RESTRICTED NUMBER(1,0));
update MFR_OPEN_FORUM_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_OPEN_FORUM_T modify (AVAILABILITY_RESTRICTED NUMBER(1,0) default 0 not null );

alter table MFR_OPEN_FORUM_T add (AVAILABILITY NUMBER(1,0));
update MFR_OPEN_FORUM_T set AVAILABILITY=1 where AVAILABILITY is NULL;
alter table MFR_OPEN_FORUM_T modify (AVAILABILITY NUMBER(1,0) default 1 not null );

alter table MFR_OPEN_FORUM_T add (OPEN_DATE timestamp);

alter table MFR_OPEN_FORUM_T add (CLOSE_DATE timestamp);

alter table MFR_TOPIC_T add (AVAILABILITY_RESTRICTED NUMBER(1,0));
update MFR_TOPIC_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_TOPIC_T modify (AVAILABILITY_RESTRICTED NUMBER(1,0) default 0 not null );

alter table MFR_TOPIC_T add (AVAILABILITY NUMBER(1,0));
update MFR_TOPIC_T set AVAILABILITY=1 where AVAILABILITY is NULL;
alter table MFR_TOPIC_T modify (AVAILABILITY NUMBER(1,0) default 1 not null );

alter table MFR_TOPIC_T add (OPEN_DATE timestamp);

alter table MFR_TOPIC_T add (CLOSE_DATE timestamp);


--MSGCNTR-355
insert into MFR_TOPIC_T (ID, UUID, MODERATED, AUTO_MARK_THREADS_READ, SORT_INDEX, MUTABLE, TOPIC_DTYPE, VERSION, CREATED, CREATED_BY, MODIFIED, MODIFIED_BY, TITLE, SHORT_DESCRIPTION, EXTENDED_DESCRIPTION, TYPE_UUID, pf_surrogateKey, USER_ID)

	(select MFR_TOPIC_S.nextval as ID, sys_guid() as UUID, MODERATED, 0 as AUTO_MARK_THREADS_READ, 3 as SORT_INDEX, 0 as MUTABLE, TOPIC_DTYPE, 0 as VERSION, sysdate as CREATED, CREATED_BY, sysdate as MODIFIED, MODIFIED_BY, 'pvt_drafts' as TITLE, 'short-desc' as SHORT_DESCRIPTION, 'ext-desc' as EXTENDED_DESCRIPTION, TYPE_UUID, pf_surrogateKey, USER_ID from (
		select count(*) as c1, mtt.MODERATED, mtt.TOPIC_DTYPE, mtt.CREATED_BY, mtt.MODIFIED_BY, mtt.TYPE_UUID, mtt.pf_surrogateKey, mtt.USER_ID
		from MFR_PRIVATE_FORUM_T mpft, MFR_TOPIC_T mtt
		where mpft.ID = mtt.pf_surrogateKey and mpft.TYPE_UUID = mtt.TYPE_UUID
		Group By mtt.USER_ID, mtt.pf_surrogateKey, mtt.MODERATED, mtt.TOPIC_DTYPE, mtt.CREATED_BY, mtt.MODIFIED_BY, mtt.TYPE_UUID) s1
	where s1.c1 = 3);


--MSGCNTR-360
--Hibernate could have missed this index, if this fails, then the index may already be in the table
--CREATE INDEX user_type_context_idx ON MFR_PVT_MSG_USR_T ( USER_ID, TYPE_UUID, CONTEXT_ID, READ_STATUS);

-- New column for Email Template service
-- SAK-18532/SAK-19522
alter table EMAIL_TEMPLATE_ITEM add EMAILFROM varchar2(255 CHAR);

-- SAK-18855
alter table POLL_POLL add POLL_IS_PUBLIC number(1,0);


-- Profile2 1.3-1.4 upgrade start

-- add company profile table and index (PRFL-224)
create table PROFILE_COMPANY_PROFILES_T (
	ID number(19,0) not null,
	USER_UUID varchar2(99 CHAR) not null,
	COMPANY_NAME varchar2(255 CHAR),
	COMPANY_DESCRIPTION varchar2(4000),
	COMPANY_WEB_ADDRESS varchar2(255 CHAR),
	primary key (ID)
);
create sequence COMPANY_PROFILES_S;
create index PROFILE_CP_USER_UUID_I on PROFILE_COMPANY_PROFILES_T (USER_UUID);

-- add message tables and indexes
create table PROFILE_MESSAGES_T (
	ID varchar2(36 CHAR) not null,
	FROM_UUID varchar2(99 CHAR) not null,
	MESSAGE_BODY varchar2(4000) not null,
	MESSAGE_THREAD varchar2(36 CHAR) not null,
	DATE_POSTED timestamp(6) not null,
	primary key (ID)
);

create table PROFILE_MESSAGE_PARTICIPANTS_T (
	ID number(19,0) not null,
	MESSAGE_ID varchar2(36 CHAR) not null,
	PARTICIPANT_UUID varchar2(99 CHAR) not null,
	MESSAGE_READ number(1,0) not null,
	MESSAGE_DELETED number(1,0) not null,
	primary key (ID)
);

create table PROFILE_MESSAGE_THREADS_T (
	ID varchar2(36 CHAR) not null,
	SUBJECT varchar2(255 CHAR) not null,
	primary key (ID)
);

create sequence PROFILE_MESSAGE_PARTICIPANTS_S;
create index PROFILE_M_THREAD_I on PROFILE_MESSAGES_T (MESSAGE_THREAD);
create index PROFILE_M_DATE_POSTED_I on PROFILE_MESSAGES_T (DATE_POSTED);
create index PROFILE_M_FROM_UUID_I on PROFILE_MESSAGES_T (FROM_UUID);
create index PROFILE_M_P_UUID_I on PROFILE_MESSAGE_PARTICIPANTS_T (PARTICIPANT_UUID);
create index PROFILE_M_P_MESSAGE_ID_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_ID);
create index PROFILE_M_P_DELETED_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_DELETED);
create index PROFILE_M_P_READ_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_READ);

-- add gallery table and indexes (PRFL-134, PRFL-171)
create table PROFILE_GALLERY_IMAGES_T (
	ID number(19,0) not null,
	USER_UUID varchar2(99 CHAR) not null,
	RESOURCE_MAIN varchar2(4000) not null,
	RESOURCE_THUMB varchar2(4000) not null,
	DISPLAY_NAME varchar2(255 CHAR) not null,
	primary key (ID)
);
create sequence GALLERY_IMAGES_S;
create index PROFILE_GI_USER_UUID_I on PROFILE_GALLERY_IMAGES_T (USER_UUID);

-- add social networking table (PRFL-252, PRFL-224)
create table PROFILE_SOCIAL_INFO_T (
	USER_UUID varchar2(99 CHAR) not null,
	FACEBOOK_URL varchar2(255 CHAR),
	LINKEDIN_URL varchar2(255 CHAR),
	MYSPACE_URL varchar2(255 CHAR),
	SKYPE_USERNAME varchar2(255 CHAR),
	TWITTER_URL varchar2(255 CHAR),
	primary key (USER_UUID)
);

-- add official image table
create table PROFILE_IMAGES_OFFICIAL_T (
	USER_UUID varchar2(99 CHAR) not null,
	URL varchar2(4000) not null,
	primary key (USER_UUID)
);

-- add kudos table
create table PROFILE_KUDOS_T (
	USER_UUID varchar2(99 CHAR) not null,
	SCORE number(10,0) not null,
	PERCENTAGE number(19,2) not null,
	DATE_ADDED timestamp(6) not null,
	primary key (USER_UUID)
);

-- add the new email message preference columns, default to 0, (PRFL-152, PRFL-186)
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_NEW number(1,0) default 0 not null;
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_REPLY number(1,0) default 0 not null;

-- add social networking privacy column (PRFL-285)
alter table PROFILE_PRIVACY_T add SOCIAL_NETWORKING_INFO number(10,0) default 0 not null;

-- add the new gallery column (PRFL-171)
alter table PROFILE_PRIVACY_T add MY_PICTURES number(10,0) default 0 not null;

-- add the new message column (PRFL-194), default to 1 (PRFL-593)
alter table PROFILE_PRIVACY_T add MESSAGES number(10,0) default 1 not null;

-- add the new businessInfo column (PRFL-210)
alter table PROFILE_PRIVACY_T add BUSINESS_INFO number(10,0) default 0 not null;

-- add the new staff and student info columns and copy old ACADEMIC_INFO value into them to maintain privacy (PRFL-267)
alter table PROFILE_PRIVACY_T add STAFF_INFO number(10,0) default 0 not null;
alter table PROFILE_PRIVACY_T add STUDENT_INFO number(10,0) default 0 not null;
update PROFILE_PRIVACY_T set STAFF_INFO = ACADEMIC_INFO;
update PROFILE_PRIVACY_T set STUDENT_INFO = ACADEMIC_INFO;
alter table PROFILE_PRIVACY_T drop column ACADEMIC_INFO;

-- add the new useOfficialImage column (PRFL-90)
alter table PROFILE_PREFERENCES_T add USE_OFFICIAL_IMAGE number(1,0) default 0 not null;

-- remove search privacy setting (PRFL-293)
alter table PROFILE_PRIVACY_T drop column SEARCH;

-- add kudos preference (PRFL-336)
alter table PROFILE_PREFERENCES_T add SHOW_KUDOS number(1,0) default 1 not null;

-- add kudos privacy (PRFL-336)
alter table PROFILE_PRIVACY_T add MY_KUDOS number(10,0) default 0 not null;

-- add gallery feed preference (PRFL-382)
alter table PROFILE_PREFERENCES_T add SHOW_GALLERY_FEED number(1,0) default 1 not null;

-- adjust size of the profile images resource uri columns (PRFL-392)
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN varchar2(4000);
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB varchar2(4000);
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN varchar2(4000);
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_THUMB varchar2(4000);

-- add indexes to commonly searched columns (PRFL-540)
create index PROFILE_FRIENDS_CONFIRMED_I on PROFILE_FRIENDS_T (CONFIRMED);
create index PROFILE_STATUS_DATE_ADDED_I on PROFILE_STATUS_T (DATE_ADDED);

-- Profile2 1.3-1.4 upgrade end

-- SHORTURL-26 shortenedurlservice 1.0
create table URL_RANDOMISED_MAPPINGS_T (
	ID number(19,0) not null,
	TINY varchar2(255 CHAR) not null,
	URL varchar2(4000) not null,
	primary key (ID)
);

create index URL_INDEX on URL_RANDOMISED_MAPPINGS_T (URL);
create index KEY_INDEX on URL_RANDOMISED_MAPPINGS_T (TINY);
create sequence URL_RANDOMISED_MAPPINGS_S;

-- SAK-18864/SAK-19951/SAK-19965 added create statement for scheduler_trigger_events
create table SCHEDULER_TRIGGER_EVENTS (
	UUID varchar2(36 CHAR) NOT NULL,
	EVENTTYPE varchar2(255 CHAR) NOT NULL,
	JOBNAME varchar2(255 CHAR) NOT NULL,
	TRIGGERNAME varchar2(255 CHAR),
	EVENTTIME timestamp NOT NULL,
	MESSAGE clob,
	primary key (UUID)
);

-- STAT-241: Tracking of time spent in site
create table SST_PRESENCES (
	ID number(19,0) not null,
	SITE_ID varchar2(99 char) not null,
	USER_ID varchar2(99 char) not null,
	P_DATE date not null,
	DURATION number(19,0) default 0 not null,
	LAST_VISIT_START_TIME timestamp default null,
	primary key (ID)
);

-- STAT-286: missing SiteStats sequence
create sequence SST_PRESENCE_ID;

-- SAK-20076: missing Sitestats indexes
create index SST_PRESENCE_DATE_IX on SST_PRESENCES (P_DATE);
create index SST_PRESENCE_USER_ID_IX on SST_PRESENCES (USER_ID);
create index SST_PRESENCE_SITE_ID_IX on SST_PRESENCES (SITE_ID);
create index SST_PRESENCE_SUD_ID_IX on SST_PRESENCES (SITE_ID, USER_ID, P_DATE);

-- KNL-563: dynamic bundling loading
CREATE TABLE SAKAI_MESSAGE_BUNDLE(
        ID NUMBER(19) NOT NULL,
        MODULE_NAME VARCHAR2(255 CHAR) NOT NULL,
        BASENAME VARCHAR2(255 CHAR) NOT NULL,
        PROP_NAME VARCHAR2(255 CHAR) NOT NULL,
        PROP_VALUE VARCHAR2(4000 CHAR),
        LOCALE VARCHAR2(255 CHAR) NOT NULL,
        DEFAULT_VALUE VARCHAR2(4000 CHAR) NOT NULL,
        PRIMARY KEY (ID)
);
create sequence SAKAI_MESSAGEBUNDLE_S;
create index SMB_SEARCH on sakai_message_bundle (BASENAME, MODULE_NAME, LOCALE, PROP_NAME);

-- RES-2: table structure for validationaccount_item
CREATE TABLE VALIDATIONACCOUNT_ITEM (
        ID NUMBER(19) NOT NULL,
        USER_ID VARCHAR2(255 CHAR) NOT NULL,
        VALIDATION_TOKEN VARCHAR2(255 CHAR) NOT NULL,
        VALIDATION_SENT TIMESTAMP(6),
        VALIDATION_RECEIVED TIMESTAMP(6),
        VALIDATIONS_SENT NUMBER(10),
        STATUS NUMBER(10),
        FIRST_NAME VARCHAR2(255 CHAR) NOT NULL,
        SURNAME VARCHAR2(255 CHAR) NOT NULL,
        ACCOUNT_STATUS NUMBER(10),
        PRIMARY KEY (ID)
);

create sequence VALIDATIONACCOUNT_ITEM_ID_SEQ;

-- SAK-20005
-- Starting up sakai-2.8.0 in order to populate an empty Oracle database (auto.ddl=true) can result
-- in certain tools relying on Hibernate 3.2.7.ga to generate indexes to fail to do so.

-- Check your database and run this script if indexes are missing.

-- Note: create index statements that have been commented out have been included for review purposes.

-- --------------------------------------------------------------------------------------------------------------------------------------
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement
-- --------------------------------------------------------------------------------------------------------------------------------------

-- KNL-563
create index SMB_SEARCH on sakai_message_bundle (BASENAME, MODULE_NAME, LOCALE, PROP_NAME);

-- MSGCNTR-360
create index user_type_context_idx ON MFR_PVT_MSG_USR_T ( USER_ID, TYPE_UUID, CONTEXT_ID, READ_STATUS);

-- PRFL-224
create index PROFILE_CP_USER_UUID_I on PROFILE_COMPANY_PROFILES_T (USER_UUID);
create index PROFILE_M_THREAD_I on PROFILE_MESSAGES_T (MESSAGE_THREAD);
create index PROFILE_M_DATE_POSTED_I on PROFILE_MESSAGES_T (DATE_POSTED);
create index PROFILE_M_FROM_UUID_I on PROFILE_MESSAGES_T (FROM_UUID);
create index PROFILE_M_P_UUID_I on PROFILE_MESSAGE_PARTICIPANTS_T (PARTICIPANT_UUID);
create index PROFILE_M_P_MESSAGE_ID_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_ID);
create index PROFILE_M_P_DELETED_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_DELETED);
create index PROFILE_M_P_READ_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_READ);

-- PRFL-134, PRFL-171
create index PROFILE_GI_USER_UUID_I on PROFILE_GALLERY_IMAGES_T (USER_UUID);

-- PRFL-540
create index PROFILE_FRIENDS_CONFIRMED_I on PROFILE_FRIENDS_T (CONFIRMED);
create index PROFILE_STATUS_DATE_ADDED_I on PROFILE_STATUS_T (DATE_ADDED);

-- SAM-775 if you get an error when running this script, you will need to clean the duplicates first. See SAM-775 for details.
-- create UNIQUE INDEX ASSESSMENTGRADINGID ON SAM_ITEMGRADING_T (ASSESSMENTGRADINGID, PUBLISHEDITEMID, PUBLISHEDITEMTEXTID, AGENTID, PUBLISHEDANSWERID);

-- SAK-20076 missing Sitestats indexes
create index SST_PRESENCE_DATE_IX on SST_PRESENCES (P_DATE);
create index SST_PRESENCE_USER_ID_IX on SST_PRESENCES (USER_ID);
create index SST_PRESENCE_SITE_ID_IX on SST_PRESENCES (SITE_ID);
create index SST_PRESENCE_SUD_ID_IX on SST_PRESENCES (SITE_ID, USER_ID, P_DATE);

-- SHORTURL-27
create index URL_INDEX on URL_RANDOMISED_MAPPINGS_T (URL);
create index KEY_INDEX on URL_RANDOMISED_MAPPINGS_T (TINY);


--------------------------------------------------------------------------
-- This is for Oracle, JForum 2.7.1 to JForum 2.8.1
--------------------------------------------------------------------------
--Note : Before running this script back up the updated tables

--add is_read to jforum_topics_mark to mark messages unread
ALTER TABLE jforum_topics_mark ADD is_read NUMBER(1) DEFAULT 0 NOT NULL;

--add released to jforum_evaluations to mark evaluations released/not released
ALTER TABLE jforum_evaluations ADD released NUMBER(1) DEFAULT 0;

--add user facebook, twitter accounts to jforum_users
ALTER TABLE jforum_users ADD (user_facebook_account VARCHAR2(255), user_twitter_account VARCHAR2(255));

--add priority to private messages
ALTER TABLE jforum_privmsgs ADD privmsgs_priority NUMBER(1) DEFAULT 0 NOT NULL;

--add start date, end date and lock end date to jforum_categories
ALTER TABLE jforum_categories ADD (start_date DATE , end_date DATE , lock_end_date NUMBER(1) DEFAULT 0);

--sequence for jforum_special_access
CREATE SEQUENCE jforum_special_access_seq
INCREMENT BY 1
    START WITH 1 MAXVALUE 2.0E9 MINVALUE 1 NOCYCLE
    CACHE 200 ORDER;

--table for jforum special access
CREATE TABLE jforum_special_access (
  special_access_id NUMBER(10) NOT NULL,
  forum_id NUMBER(10) DEFAULT 0 NOT NULL ,
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  lock_end_date NUMBER(1) DEFAULT 0 NOT NULL,
  override_start_date NUMBER(1) DEFAULT 0 NOT NULL,
  override_end_date NUMBER(1) DEFAULT 0 NOT NULL,
  password VARCHAR(56) DEFAULT NULL,
  users CLOB,
  PRIMARY KEY(special_access_id)
);



--------------------------------------------------------------------
-- rSmart additions from schema comparing 2.7.1 and 2.8.0 databases
--------------------------------------------------------------------
ALTER TABLE BBB_MEETING ADD COLUMN HOST_URL VARCHAR2(255) NOT NULL AFTER NAME;

CREATE
    TABLE CLOG_AUTHOR
    (
        USER_ID CHAR(36) NOT NULL,
        SITE_ID VARCHAR2(255) NOT NULL,
        TOTAL_POSTS NUMBER(10) NOT NULL,
        LAST_POST_DATE TIMESTAMP(6),
        TOTAL_COMMENTS NUMBER(10) NOT NULL,
        CONSTRAINT BLOG_AUTHOR_PK PRIMARY KEY (USER_ID, SITE_ID)
    );

CREATE
    TABLE CLOG_AUTOSAVED_POST
    (
        POST_ID CHAR(36) NOT NULL,
        SITE_ID VARCHAR2(255),
        TITLE VARCHAR2(255) NOT NULL,
        CONTENT CLOB NOT NULL,
        CREATED_DATE TIMESTAMP(6) NOT NULL,
        MODIFIED_DATE TIMESTAMP(6),
        CREATOR_ID VARCHAR2(255) NOT NULL,
        KEYWORDS VARCHAR2(255),
        ALLOW_COMMENTS NUMBER(10),
        VISIBILITY VARCHAR2(16) NOT NULL,
        CONSTRAINT AUTOSAVED_POST_PK PRIMARY KEY (POST_ID)
    );

CREATE
    TABLE CLOG_COMMENT
    (
        COMMENT_ID CHAR(36) NOT NULL,
        POST_ID CHAR(36) NOT NULL,
        CREATOR_ID CHAR(36) NOT NULL,
        CREATED_DATE TIMESTAMP(6) NOT NULL,
        MODIFIED_DATE TIMESTAMP(6) NOT NULL,
        CONTENT CLOB NOT NULL,
        CONSTRAINT COMMENT_PK PRIMARY KEY (COMMENT_ID)
    );

CREATE
    TABLE CLOG_POST
    (
        POST_ID CHAR(36) NOT NULL,
        SITE_ID VARCHAR2(255),
        TITLE VARCHAR2(255) NOT NULL,
        CONTENT CLOB NOT NULL,
        CREATED_DATE TIMESTAMP(6) NOT NULL,
        MODIFIED_DATE TIMESTAMP(6),
        CREATOR_ID VARCHAR2(255) NOT NULL,
        KEYWORDS VARCHAR2(255),
        ALLOW_COMMENTS NUMBER(10),
        VISIBILITY VARCHAR2(16) NOT NULL,
        CONSTRAINT POST_PK PRIMARY KEY (POST_ID)
    );
CREATE
    TABLE CLOG_PREFERENCES
    (
        USER_ID VARCHAR2(36) NOT NULL,
        SITE_ID VARCHAR2(255) NOT NULL,
        EMAIL_FREQUENCY VARCHAR2(32) NOT NULL,
        CONSTRAINT BLOG_PREFERENCES_PK PRIMARY KEY (USER_ID, SITE_ID)
    );

ALTER TABLE contentreview_item ADD ERRORCODE NUMBER(10);
       
CREATE
    TABLE CONTENTREVIEW_SYNC_ITEM
    (
        ID NUMBER(19) NOT NULL,
        SITEID VARCHAR2(255 CHAR) NOT NULL,
        DATEQUEUED TIMESTAMP(6) NOT NULL,
        LASTTRIED TIMESTAMP(6),
        STATUS NUMBER(10) NOT NULL,
        MESSAGES CLOB,
        PRIMARY KEY (ID)
    );

ALTER TABLE EVAL_EVALUATION ADD  EMAIL_OPEN_NOTIFICATION NUMBER(1); 
ALTER TABLE EVAL_EVALUATION ADD  REMINDER_STATUS VARCHAR2(255 CHAR);

ALTER TABLE HIERARCHY_NODE_META ADD ISDISABLED NUMBER(1) DEFAULT 0 ;

CREATE
    TABLE SIGNUP_MEETINGS
    (
        ID NUMBER(19) NOT NULL,
        VERSION NUMBER(10) NOT NULL,
        TITLE VARCHAR2(255 CHAR) NOT NULL,
        DESCRIPTION CLOB,
        LOCATION VARCHAR2(255 CHAR) NOT NULL,
        MEETING_TYPE VARCHAR2(50 CHAR) NOT NULL,
        CREATOR_USER_ID VARCHAR2(255 CHAR) NOT NULL,
        START_TIME TIMESTAMP(6) NOT NULL,
        END_TIME TIMESTAMP(6) NOT NULL,
        SIGNUP_BEGINS TIMESTAMP(6),
        SIGNUP_DEADLINE TIMESTAMP(6),
        CANCELED NUMBER(1),
        LOCKED NUMBER(1),
        ALLOW_WAITLIST NUMBER(1) DEFAULT 1,
        ALLOW_COMMENT NUMBER(1) DEFAULT 1,
        AUTO_REMINDER NUMBER(1) DEFAULT 0,
        EID_INPUT_MODE NUMBER(1) DEFAULT 0,
        RECEIVE_EMAIL_OWNER NUMBER(1) DEFAULT 0,
        ALLOW_ATTENDANCE NUMBER(1) DEFAULT 0,
        RECURRENCE_ID NUMBER(19),
        REPEAT_TYPE VARCHAR2(20 CHAR),
        PRIMARY KEY (ID)
    );

CREATE
    TABLE SIGNUP_ATTACHMENTS
    (
        MEETING_ID NUMBER(19) NOT NULL,
        RESOURCE_ID VARCHAR2(255 CHAR),
        FILE_NAME VARCHAR2(255 CHAR),
        MIME_TYPE VARCHAR2(80 CHAR),
        FILESIZE NUMBER(19),
        LOCATION VARCHAR2(255 CHAR),
        ISLINK NUMBER(1),
        TIMESLOT_ID NUMBER(19),
        VIEW_BY_ALL NUMBER(1) DEFAULT 1,
        CREATED_BY VARCHAR2(255 CHAR) NOT NULL,
        CREATED_DATE TIMESTAMP(6) NOT NULL,
        LAST_MODIFIED_BY VARCHAR2(255 CHAR) NOT NULL,
        LAST_MODIFIED_DATE TIMESTAMP(6) NOT NULL,
        LIST_INDEX NUMBER(10) NOT NULL,
        PRIMARY KEY (MEETING_ID, LIST_INDEX),
        CONSTRAINT FK3BCB709CB1E8A17 FOREIGN KEY (MEETING_ID) REFERENCES SIGNUP_MEETINGS (ID)
    );
    
CREATE
    TABLE SIGNUP_SITES
    (
        ID NUMBER(19) NOT NULL,
        VERSION NUMBER(10) NOT NULL,
        TITLE VARCHAR2(255 CHAR),
        SITE_ID VARCHAR2(255 CHAR) NOT NULL,
        CALENDAR_EVENT_ID VARCHAR2(2000 CHAR),
        CALENDAR_ID VARCHAR2(255 CHAR),
        MEETING_ID NUMBER(19) NOT NULL,
        LIST_INDEX NUMBER(10),
        PRIMARY KEY (ID),
        CONSTRAINT FKCCD4AC25CB1E8A17 FOREIGN KEY (MEETING_ID) REFERENCES SIGNUP_MEETINGS (ID)
    );

CREATE
    TABLE SIGNUP_SITE_GROUPS
    (
        SIGNUP_SITE_ID NUMBER(19) NOT NULL,
        TITLE VARCHAR2(255 CHAR),
        GROUP_ID VARCHAR2(255 CHAR) NOT NULL,
        CALENDAR_EVENT_ID VARCHAR2(2000 CHAR),
        CALENDAR_ID VARCHAR2(255 CHAR),
        LIST_INDEX NUMBER(10) NOT NULL,
        PRIMARY KEY (SIGNUP_SITE_ID, LIST_INDEX),
        CONSTRAINT FKC72B75255084316 FOREIGN KEY (SIGNUP_SITE_ID) REFERENCES SIGNUP_SITES (ID)
    );

CREATE
    TABLE SIGNUP_TS
    (
        ID NUMBER(19) NOT NULL,
        VERSION NUMBER(10) NOT NULL,
        START_TIME TIMESTAMP(6) NOT NULL,
        END_TIME TIMESTAMP(6) NOT NULL,
        MAX_NO_OF_ATTENDEES NUMBER(10),
        DISPLAY_ATTENDEES NUMBER(1),
        CANCELED NUMBER(1),
        LOCKED NUMBER(1),
        MEETING_ID NUMBER(19) NOT NULL,
        LIST_INDEX NUMBER(10),
        PRIMARY KEY (ID),
        CONSTRAINT FK41154B06CB1E8A17 FOREIGN KEY (MEETING_ID) REFERENCES SIGNUP_MEETINGS (ID)
    );

CREATE
    TABLE SIGNUP_TS_ATTENDEES
    (
        TIMESLOT_ID NUMBER(19) NOT NULL,
        ATTENDEE_USER_ID VARCHAR2(255 CHAR) NOT NULL,
        COMMENTS CLOB,
        SIGNUP_SITE_ID VARCHAR2(255 CHAR) NOT NULL,
        CALENDAR_EVENT_ID VARCHAR2(255 CHAR),
        CALENDAR_ID VARCHAR2(255 CHAR),
        ATTENDED NUMBER(1) DEFAULT 0,
        LIST_INDEX NUMBER(10) NOT NULL,
        PRIMARY KEY (TIMESLOT_ID, LIST_INDEX),
        CONSTRAINT FKBAB08100CDB30B3D FOREIGN KEY (TIMESLOT_ID) REFERENCES SIGNUP_TS (ID)
    );

CREATE
    TABLE SIGNUP_TS_WAITINGLIST
    (
        TIMESLOT_ID NUMBER(19) NOT NULL,
        ATTENDEE_USER_ID VARCHAR2(255 CHAR) NOT NULL,
        COMMENTS CLOB,
        SIGNUP_SITE_ID VARCHAR2(255 CHAR) NOT NULL,
        CALENDAR_EVENT_ID VARCHAR2(255 CHAR),
        CALENDAR_ID VARCHAR2(255 CHAR),
        ATTENDED NUMBER(1) DEFAULT 0,
        LIST_INDEX NUMBER(10) NOT NULL,
        PRIMARY KEY (TIMESLOT_ID, LIST_INDEX),
        CONSTRAINT FK3AB9A8B2CDB30B3D FOREIGN KEY (TIMESLOT_ID) REFERENCES SIGNUP_TS (ID)
    );

CREATE 
    TABLE RSN_USER
    (
        USER_EID VARCHAR2(255 CHAR) NOT NULL,
        EMAIL VARCHAR2(255 CHAR),
        EMAIL_LC VARCHAR2(255 CHAR),
        FIRST_NAME VARCHAR2(255 CHAR),
        LAST_NAME VARCHAR2(255 CHAR),
        TYPE VARCHAR2(255 CHAR),
        PW VARCHAR2(255 CHAR),
        CREATEDBY VARCHAR2(99 CHAR) NOT NULL,
        MODIFIEDBY VARCHAR2(99 CHAR) NOT NULL,
        CREATEDON VARCHAR2(99 CHAR),
        MODIFIEDON VARCHAR2(99 CHAR) NOT NULL,
        PRIMARY KEY (USER_EID)
    );

CREATE
    TABLE RSN_USER_PROPERTY
    (
        USER_ID VARCHAR2(255 CHAR) NOT NULL,
        PROPERTY_VALUE VARCHAR2(512 CHAR) NOT NULL,
        NAME VARCHAR2(99 CHAR) NOT NULL,
        PRIMARY KEY (USER_ID, NAME),
        CONSTRAINT FK759239574F4DCDBB FOREIGN KEY (USER_ID) REFERENCES RSN_USER (USER_EID)
    );


CREATE
    TABLE SAKORA_JOB
    (
        UUID VARCHAR2(36 CHAR) NOT NULL,
        START_TIME TIMESTAMP(6),
        END_TIME TIMESTAMP(6),
        STATUS VARCHAR2(255 CHAR),
        PRIMARY KEY (UUID)
    );

CREATE
    TABLE SAKORA_OPERATION
    (
        UUID VARCHAR2(36 CHAR) NOT NULL,
        TRANS_OP_ID VARCHAR2(255 CHAR),
        SOURCED_ID VARCHAR2(255 CHAR),
        SERVICE VARCHAR2(255 CHAR),
        OPERATION VARCHAR2(255 CHAR),
        STATUS VARCHAR2(255 CHAR),
        RECEIVED_DATE TIMESTAMP(6),
        UPDATED_DATE TIMESTAMP(6),
        COMPLETED_DATE TIMESTAMP(6),
        STATUS_MESSAGE CLOB,
        JOB_ID VARCHAR2(36 CHAR),
        PRIMARY KEY (UUID),
        CONSTRAINT FKAED638693B8983AF FOREIGN KEY (JOB_ID) REFERENCES SAKORA_JOB (UUID)
    );

CREATE
    TABLE SAKORA_MEMBERSHIP
    (
        ID NUMBER(19) NOT NULL,
        USER_EID VARCHAR2(255 CHAR),
        COURSE_EID VARCHAR2(255 CHAR),
        ROLE_STR VARCHAR2(255 CHAR),
        MODE_STR VARCHAR2(8 CHAR),
        INPUT_TIME TIMESTAMP(6),
        PRIMARY KEY (ID)
    );


ALTER TABLE scheduler_trigger_events rename column type to eventType;
ALTER TABLE scheduler_trigger_events rename column time to eventTime;

ALTER TABLE EVAL_EVALUATION ADD  EMAIL_OPEN_NOTIFICATION NUMBER(1);

ALTER TABLE  OAUTH_PROVIDER ADD RSAKEY CLOB;
ALTER TABLE  OAUTH_PROVIDER ADD SIGNATUREMETHOD VARCHAR2(255 CHAR) NOT NULL;
ALTER TABLE  OAUTH_PROVIDER ADD ENABLED NUMBER(1) NOT NULL;

-- mneme upgrade

CREATE SEQUENCE MNEME_ASSESSMENT_DETAIL_SEQ;
ALTER TABLE MNEME_ASSESSMENT_PART_DETAIL ADD (ID NUMBER, SEQ NUMBER, POINTS FLOAT);

-- Populate the new ID field sequentially across the current set of detail records from the sequence
UPDATE MNEME_ASSESSMENT_PART_DETAIL SET ID=MNEME_ASSESSMENT_DETAIL_SEQ.NEXTVAL;

ALTER TABLE MNEME_ASSESSMENT_PART_DETAIL MODIFY (ID NUMBER NOT NULL PRIMARY KEY);

ALTER TABLE MNEME_ASSESSMENT ADD (POOL NUMBER, NEEDSPOINTS CHAR(1), SHOW_MODEL_ANSWER CHAR(1));

UPDATE MNEME_ASSESSMENT_PART_DETAIL SET SEQ=NUM_QUESTIONS_SEQ;

CREATE INDEX MNEME_APD_IDX_QID ON MNEME_ASSESSMENT_PART_DETAIL
(
	QUESTION_ID	ASC
);

CREATE INDEX MNEME_APD_IDX_POOLID ON MNEME_ASSESSMENT_PART_DETAIL
(
	POOL_ID	ASC
);

ALTER TABLE MNEME_ASSESSMENT ADD (FORMAL_EVAL CHAR (1), RESULTS_EMAIL VARCHAR2(255), RESULTS_SENT NUMBER);

CREATE INDEX MNEME_ASSESSMENT_IDX_RESULTS ON MNEME_ASSESSMENT
(
	RESULTS_EMAIL	ASC,
	PUBLISHED		ASC,
	RESULTS_SENT	ASC
);

ALTER TABLE MNEME_QUESTION ADD (PRESENTATION_ATTACHMENTS CLOB);

------------------------------------------------------------------------------
--- BEGIN: CLE-9062 Re-registration of OAuth tools with new names/tool IDs
------------------------------------------------------------------------------

-- Remove deprecated tool mappings

  -- First create a temporary table to hold the pertinent tool definitions we
  -- are deleting. This helps us eliminate the tools first to prevent
  -- foreign key violations
CREATE TABLE TEMP_TOOLS_TO_DELETE
(
    TOOL_ID         VARCHAR2(99),
    SITE_ID         VARCHAR2(99),
    PAGE_ID         VARCHAR2(99),
    REGISTRATION    VARCHAR2(99)
);

  -- populate the temporary tool table
INSERT INTO TEMP_TOOLS_TO_DELETE (TOOL_ID, SITE_ID, PAGE_ID, REGISTRATION)
    (SELECT TOOL_ID, SITE_ID, PAGE_ID, REGISTRATION
        FROM SAKAI_SITE_TOOL WHERE REGISTRATION LIKE 'com.rsmart.oauth.%');

  -- now its safe to delete the tools
DELETE FROM SAKAI_SITE_TOOL WHERE TOOL_ID IN (SELECT tttd.TOOL_ID FROM TEMP_TOOLS_TO_DELETE tttd);

-- Delete the deprecated pages that contained the tools; only delete pages that had *only* an OAuth tool
-- Start by deleting any page properties to prevent FK constraint violations
DELETE FROM SAKAI_SITE_PAGE_PROPERTY WHERE PAGE_ID IN
    (SELECT TOOL_COUNT.PAGE_ID
        FROM
            (SELECT tool.PAGE_ID AS PAGE_ID, COUNT(tool.REGISTRATION) AS REG_COUNT
                FROM TEMP_TOOLS_TO_DELETE tool, SAKAI_SITE site
                WHERE (site.TYPE like 'myworkspace%' or site.TYPE in ('aw','myworkspacetadmin'))
                      and site.SITE_ID=tool.SITE_ID and tool.REGISTRATION like 'com.rsmart.oauth%'
                GROUP BY tool.PAGE_ID) TOOL_COUNT
        WHERE TOOL_COUNT.REG_COUNT = 1);

-- Delete the actual pages now that the dependent properties are gone
DELETE FROM SAKAI_SITE_PAGE WHERE PAGE_ID IN
    (SELECT TOOL_COUNT.PAGE_ID
        FROM
            (SELECT tool.PAGE_ID AS PAGE_ID, COUNT(tool.REGISTRATION) AS REG_COUNT
                FROM TEMP_TOOLS_TO_DELETE tool, SAKAI_SITE site
                WHERE (site.TYPE like 'myworkspace%' or site.TYPE in ('aw','myworkspacetadmin'))
                      and site.SITE_ID=tool.SITE_ID and tool.REGISTRATION like 'com.rsmart.oauth%'
                GROUP BY tool.PAGE_ID) TOOL_COUNT
        WHERE TOOL_COUNT.REG_COUNT = 1);

-- Update tool categories to use the new tool IDs
UPDATE rsn_to_tool SET tool_id = 'com.rsmart.oauth.token' WHERE tool_id='com.rsmart.oauth.tools';
UPDATE rsn_to_tool SET tool_id = 'com.rsmart.oauth.provider' WHERE tool_id='com.rsmart.oauth.management.tools';

-- Add OAuth Tokens tool to all user workspaces and workspace templates
INSERT INTO SAKAI_SITE_PAGE (SITE_ID, PAGE_ID, TITLE, LAYOUT, SITE_ORDER, POPUP)
    SELECT page.SITE_ID, page.SITE_ID||'.oauth.token', 'OAuth Tokens', 0, page.max_order + 1, 0
        FROM (SELECT SITE_ID, max(SITE_ORDER) AS max_order FROM SAKAI_SITE_PAGE GROUP BY SITE_ID) page,
            SAKAI_SITE site
        WHERE page.SITE_ID = site.SITE_ID
            AND site.TYPE LIKE 'myworkspace%';

-- Add the OAuth Tokens tool to Page Category "My Settings"
INSERT INTO SAKAI_SITE_PAGE_PROPERTY (SITE_ID, PAGE_ID, NAME, "VALUE")
    SELECT page.SITE_ID, page.SITE_ID||'.oauth.token', 'sitePage.pageCategory', 'My Settings'
        FROM SAKAI_SITE_PAGE page, SAKAI_SITE site
        WHERE page.SITE_ID = site.SITE_ID
            AND site.TYPE LIKE 'myworkspace%';

-- Add the Token tool to my workspace
INSERT INTO SAKAI_SITE_TOOL (TOOL_ID, PAGE_ID, SITE_ID, REGISTRATION, PAGE_ORDER, TITLE, LAYOUT_HINTS)
    SELECT page.PAGE_ID, page.PAGE_ID, page.SITE_ID,
        'com.rsmart.oauth.token', 1, NULL, '0,0'
        FROM SAKAI_SITE_PAGE page
        WHERE page.PAGE_ID like '%.oauth.token';

-- Add OAuth Providers page  to admin template and admin workspace
INSERT INTO SAKAI_SITE_PAGE (SITE_ID, PAGE_ID, TITLE, LAYOUT, SITE_ORDER, POPUP)
    select page.SITE_ID, page.SITE_ID||'.oauth.provider', 'OAuth Providers', 0, page.max_order + 1, 0
        from (SELECT SITE_ID, max(SITE_ORDER) AS max_order FROM SAKAI_SITE_PAGE GROUP BY SITE_ID) page,
            SAKAI_SITE site
        WHERE page.SITE_ID = site.SITE_ID
            AND site.TYPE IN ('aw','myworkspacetadmin');

-- Add the Provider tool to admin pages
INSERT INTO SAKAI_SITE_TOOL (TOOL_ID, PAGE_ID, SITE_ID, REGISTRATION, PAGE_ORDER, TITLE, LAYOUT_HINTS)
    SELECT page.PAGE_ID, page.PAGE_ID, page.SITE_ID, 'com.rsmart.oauth.provider', 1, NULL, '0,0'
        FROM SAKAI_SITE_PAGE page, SAKAI_SITE site
        WHERE page.PAGE_ID like '%.oauth.provider'
            AND site.SITE_ID = page.SITE_ID
            AND site.TYPE IN ('aw','myworkspacetadmin');

DROP TABLE TEMP_TOOLS_TO_DELETE;

------------------------------------------------------------------------------
--- END: CLE-9062 Re-registration of OAuth tools with new names/tool IDs
------------------------------------------------------------------------------

/*-----------------------------------
-- BEGIN: CLE-9515
-----------------------------------*/
DELETE FROM oauth_headers WHERE
  providerUUID IN (SELECT uuid FROM oauth_provider WHERE consumer_key='duffy.rsmart.com') AND
  providerUUID NOT IN (SELECT providerUUID FROM oauth_token);

DELETE FROM oauth_provider WHERE
  consumer_key = 'duffy.rsmart.com' AND
  uuid NOT IN (SELECT providerUUID FROM oauth_token) AND
  uuid NOT IN (SELECT providerUUID FROM oauth_headers);

UPDATE oauth_provider
  SET enabled=1 WHERE enabled is NULL;
/*-----------------------------------
-- END: CLE-9515
-----------------------------------*/