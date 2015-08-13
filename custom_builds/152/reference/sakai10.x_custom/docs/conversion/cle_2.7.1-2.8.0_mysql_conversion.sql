-- This is the MYSQL Sakai 2.7.1 -> 2.8.0 conversion script
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
alter table ANNOUNCEMENT_MESSAGE add column MESSAGE_ORDER int(11) default null;

-- SAK-20560
update ANNOUNCEMENT_MESSAGE set MESSAGE_ORDER='1', XML=REPLACE(XML, ' subject=', ' message_order="1" subject=') WHERE MESSAGE_ORDER IS NULL;


drop index IE_ANNC_MSG_ATTRIB on ANNOUNCEMENT_MESSAGE;
create index IE_ANNC_MSG_ATTRIB on ANNOUNCEMENT_MESSAGE (DRAFT, PUBVIEW, OWNER, MESSAGE_ORDER);

drop index ANNOUNCEMENT_MESSAGE_CDD on ANNOUNCEMENT_MESSAGE;
create index ANNOUNCEMENT_MESSAGE_CDD on ANNOUNCEMENT_MESSAGE (CHANNEL_ID, MESSAGE_DATE, MESSAGE_ORDER, DRAFT);

-- SAK-18532/SAK-19522 new column for Email Template service
alter table EMAIL_TEMPLATE_ITEM add column EMAILFROM varchar(255) default null;

-- SAK-19448
alter table EMAIL_TEMPLATE_ITEM modify HTMLMESSAGE LONGTEXT;

-- SAK-19080 / GRBK-736 Gradebook2 support
alter table GB_GRADE_RECORD_T add column USER_ENTERED_GRADE varchar(255) default null;

-- GRBK-945
alter table GB_ACTION_RECORD_PROPERTY_T modify POPERTY_VALUE varchar(756);

-- MSGCNTR-309 start and end dates on Forums and Topics
alter table MFR_AREA_T add column AVAILABILITY_RESTRICTED bit;
update MFR_AREA_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is null;
alter table MFR_AREA_T modify column AVAILABILITY_RESTRICTED bit not null default false;

alter table MFR_AREA_T add column AVAILABILITY bit;
update MFR_AREA_T set AVAILABILITY=1 where AVAILABILITY is null;
alter table MFR_AREA_T modify column AVAILABILITY bit not null default true;

alter table MFR_AREA_T add column OPEN_DATE datetime;

alter table MFR_AREA_T add column CLOSE_DATE datetime;

alter table MFR_OPEN_FORUM_T add column AVAILABILITY_RESTRICTED bit;
update MFR_OPEN_FORUM_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is null;
alter table MFR_OPEN_FORUM_T modify column AVAILABILITY_RESTRICTED bit not null default false;

alter table MFR_OPEN_FORUM_T add column AVAILABILITY bit;
update MFR_OPEN_FORUM_T set AVAILABILITY=1 where AVAILABILITY is null;
alter table MFR_OPEN_FORUM_T modify column AVAILABILITY bit not null default true;

alter table MFR_OPEN_FORUM_T add column OPEN_DATE datetime;

alter table MFR_OPEN_FORUM_T add column CLOSE_DATE datetime;

alter table MFR_TOPIC_T add column AVAILABILITY_RESTRICTED bit;
update MFR_TOPIC_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is null;
alter table MFR_TOPIC_T modify column AVAILABILITY_RESTRICTED bit not null default false;

alter table MFR_TOPIC_T add column AVAILABILITY bit;
update MFR_TOPIC_T set AVAILABILITY=1 where AVAILABILITY is null;
alter table MFR_TOPIC_T modify column AVAILABILITY bit not null default true;

alter table MFR_TOPIC_T add column OPEN_DATE datetime null;
alter table MFR_TOPIC_T add column CLOSE_DATE datetime null;

-- MSGCNTR-355
insert into MFR_TOPIC_T (UUID, MODERATED, AUTO_MARK_THREADS_READ, SORT_INDEX, MUTABLE, TOPIC_DTYPE, VERSION, CREATED, CREATED_BY, MODIFIED, MODIFIED_BY, TITLE, SHORT_DESCRIPTION, EXTENDED_DESCRIPTION, TYPE_UUID, pf_surrogateKey, USER_ID)

select UUID, MODERATED, AUTO_MARK_THREADS_READ, 3 as SORT_INDEX, 0 as MUTABLE, TOPIC_DTYPE, VERSION, CREATED, CREATED_BY, MODIFIED, MODIFIED_BY, TITLE, SHORT_DESCRIPTION, EXTENDED_DESCRIPTION, TYPE_UUID, pf_surrogateKey, USER_ID from (
    select count(*) as c1, uuid() as UUID, mtt.MODERATED, mtt.AUTO_MARK_THREADS_READ, mtt.TOPIC_DTYPE, 0 as VERSION, mtt.CREATED, mtt.CREATED_BY, mtt.MODIFIED, mtt.MODIFIED_BY, 'pvt_drafts' as TITLE, 'short-desc' as SHORT_DESCRIPTION, 'ext-desc' as EXTENDED_DESCRIPTION, mtt.TYPE_UUID, mtt.pf_surrogateKey, mtt.USER_ID
    from MFR_PRIVATE_FORUM_T mpft, MFR_TOPIC_T mtt
    where mpft.ID = mtt.pf_surrogateKey and mpft.TYPE_UUID = mtt.TYPE_UUID
    Group By mtt.USER_ID, mtt.pf_surrogateKey) s1
where s1.c1 = 3;


-- SAK-18855
alter table POLL_POLL add column POLL_IS_PUBLIC bit(1) not null default 0;
-- alter table POLL_POLL add column POLL_IS_PUBLIC bit not null default false;

-- Profile2 1.3-1.4 upgrade start

-- PRFL-224 add company profile table and index
create table PROFILE_COMPANY_PROFILES_T (
	ID bigint not null auto_increment,
	USER_UUID varchar(99) not null,
	COMPANY_NAME varchar(255),
	COMPANY_DESCRIPTION text,
	COMPANY_WEB_ADDRESS varchar(255),
	primary key (ID)
);


-- add private messaging tables and indexes
create table PROFILE_MESSAGES_T (
	ID varchar(36) not null,
	FROM_UUID varchar(99) not null,
	MESSAGE_BODY text not null,
	MESSAGE_THREAD varchar(36) not null,
	DATE_POSTED datetime not null,
	primary key (ID)
);

create table PROFILE_MESSAGE_PARTICIPANTS_T (
	ID bigint not null auto_increment,
	MESSAGE_ID varchar(36) not null,
	PARTICIPANT_UUID varchar(99) not null,
	MESSAGE_READ bit not null,
	MESSAGE_DELETED bit not null,
	primary key (ID)
);

create table PROFILE_MESSAGE_THREADS_T (
	ID varchar(36) not null,
	SUBJECT varchar(255) not null,
	primary key (ID)
);



-- PRFL-134, PRFL-171 add gallery table and indexes
create table PROFILE_GALLERY_IMAGES_T (
	ID bigint not null auto_increment,
	USER_UUID varchar(99) not null,
	RESOURCE_MAIN text not null,
	RESOURCE_THUMB text not null,
	DISPLAY_NAME varchar(255) not null,
	primary key (ID)
);

-- Data type changes
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN text not null;
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB text not null;

-- PRFL-252, PRFL-224 add social networking table
create table PROFILE_SOCIAL_INFO_T (
	USER_UUID varchar(99) not null,
	FACEBOOK_URL varchar(255),
	LINKEDIN_URL varchar(255),
	MYSPACE_URL varchar(255),
	SKYPE_USERNAME varchar(255),
	TWITTER_URL varchar(255),
	primary key (USER_UUID)
);

-- add official image table
create table PROFILE_IMAGES_OFFICIAL_T (
	USER_UUID varchar(99) not null,
	URL text not null,
	primary key (USER_UUID)
);

-- add kudos table
create table PROFILE_KUDOS_T (
	USER_UUID varchar(99) not null,
	SCORE integer not null,
	PERCENTAGE numeric(19,2) not null,
	DATE_ADDED datetime not null,
	primary key (USER_UUID)
);

-- PRFL-152, PRFL-186 add the new email message preference columns, default to 0
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_NEW bit not null default false;
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_REPLY bit not null default false;

-- PRFL-285 add social networking privacy column
alter table PROFILE_PRIVACY_T add SOCIAL_NETWORKING_INFO int not null default 0;

-- PRFL-171 add the new gallery column
alter table PROFILE_PRIVACY_T add MY_PICTURES int not null default 0;

-- PRFL-194 add the new messages column, default to 1 (PRFL-593)
alter table PROFILE_PRIVACY_T add MESSAGES int not null default 1;

-- PRFL-210 add the new businessInfo column
alter table PROFILE_PRIVACY_T add BUSINESS_INFO int not null default 0;

-- PRFL-267 add the new staff and student info columns
-- and copy old ACADEMIC_INFO value into them to maintain privacy
alter table PROFILE_PRIVACY_T add STAFF_INFO int not null default 0;
alter table PROFILE_PRIVACY_T add STUDENT_INFO int not null default 0;
update PROFILE_PRIVACY_T set STAFF_INFO = ACADEMIC_INFO;
update PROFILE_PRIVACY_T set STUDENT_INFO = ACADEMIC_INFO;
alter table PROFILE_PRIVACY_T drop ACADEMIC_INFO;

-- PRFL-90 add the new useOfficialImage column
alter table PROFILE_PREFERENCES_T add USE_OFFICIAL_IMAGE bit not null default false;

-- PRFL-293 remove search privacy setting
alter table PROFILE_PRIVACY_T drop SEARCH;

-- PRFL-336 add kudos preference
alter table PROFILE_PREFERENCES_T add SHOW_KUDOS bit not null default true;

-- PRFL-336 add kudos privacy
alter table PROFILE_PRIVACY_T add MY_KUDOS int not null default 0;

-- PRFL-382 add gallery feed preference
alter table PROFILE_PREFERENCES_T add SHOW_GALLERY_FEED bit not null default true;

-- PRFL-392 adjust size of the profile images resource uri columns
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN text;
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB text;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN text;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_THUMB text;


-- Profile2 1.3-1.4 upgrade end


-- SAK-17821 Add additional fields to SakaiPerson
alter table SAKAI_PERSON_T add column STAFF_PROFILE text;
alter table SAKAI_PERSON_T add column UNIVERSITY_PROFILE_URL text;
alter table SAKAI_PERSON_T add column ACADEMIC_PROFILE_URL text;
alter table SAKAI_PERSON_T add column PUBLICATIONS text;
alter table SAKAI_PERSON_T add column BUSINESS_BIOGRAPHY text;

-- SAM-666
alter table SAM_ASSESSFEEDBACK_T add column FEEDBACKCOMPONENTOPTION integer default null;
update SAM_ASSESSFEEDBACK_T set FEEDBACKCOMPONENTOPTION = 2;
alter table SAM_PUBLISHEDFEEDBACK_T add column FEEDBACKCOMPONENTOPTION integer default null;
update SAM_PUBLISHEDFEEDBACK_T set FEEDBACKCOMPONENTOPTION = 2;

-- SAM-971
alter table SAM_ASSESSMENTGRADING_T add column LASTVISITEDPART integer default null;
alter table SAM_ASSESSMENTGRADING_T add column LASTVISITEDQUESTION integer default null;


-- SAM-756
alter table SAM_ITEMTEXT_T add column(TEMP_CLOB_TEXT TEXT);
update SAM_ITEMTEXT_T SET TEMP_CLOB_TEXT = TEXT;
alter table SAM_ITEMTEXT_T drop column TEXT;
alter table SAM_ITEMTEXT_T change column TEMP_CLOB_TEXT TEXT TEXT;


alter table SAM_PUBLISHEDITEMTEXT_T add column(TEMP_CLOB_TEXT TEXT);
update SAM_PUBLISHEDITEMTEXT_T SET TEMP_CLOB_TEXT = TEXT;
alter table SAM_PUBLISHEDITEMTEXT_T drop column TEXT;
alter table SAM_PUBLISHEDITEMTEXT_T change column TEMP_CLOB_TEXT TEXT TEXT;


alter table SAM_ITEMGRADING_T add column(TEMP_CLOB_TEXT TEXT);
update SAM_ITEMGRADING_T SET TEMP_CLOB_TEXT = ANSWERTEXT;
alter table SAM_ITEMGRADING_T drop column ANSWERTEXT;
alter table SAM_ITEMGRADING_T change column TEMP_CLOB_TEXT ANSWERTEXT TEXT;


alter table SAM_ANSWER_T add column(TEMP_CLOB_TEXT TEXT);
update SAM_ANSWER_T SET TEMP_CLOB_TEXT = TEXT;
alter table SAM_ANSWER_T drop column TEXT;
alter table SAM_ANSWER_T change column TEMP_CLOB_TEXT TEXT TEXT;


alter table SAM_PUBLISHEDANSWER_T add column(TEMP_CLOB_TEXT TEXT);
update SAM_PUBLISHEDANSWER_T SET TEMP_CLOB_TEXT = TEXT;
alter table SAM_PUBLISHEDANSWER_T drop column TEXT;
alter table SAM_PUBLISHEDANSWER_T change column TEMP_CLOB_TEXT TEXT TEXT;


-- SHORTURL-26 shortenedurlservice 1.0
create table URL_RANDOMISED_MAPPINGS_T (
	ID bigint not null auto_increment,
	TINY varchar(255) not null,
	URL text not null,
	primary key (ID)
);


-- STAT-241 table structure for sst_presences
create table SST_PRESENCES (
    ID bigint(20) not null auto_increment,
    SITE_ID varchar(99) not null,
    USER_ID varchar(99) not null,
    P_DATE date not null,
    DURATION bigint(20) not null default '0',
    LAST_VISIT_START_TIME datetime default null,
    primary key (ID)
);


--  RES-2: table structure for validationaccount_item
create table VALIDATIONACCOUNT_ITEM (
    id bigint(20) not null auto_increment,
    USER_ID varchar(255) not null,
    VALIDATION_TOKEN varchar(255) not null,
    VALIDATION_SENT datetime default null,
    VALIDATION_RECEIVED datetime default null,
    VALIDATIONS_SENT int(11) default null,
    STATUS int(11) default null,
    FIRST_NAME varchar(255) not null,
    SURNAME varchar(255) not null,
    ACCOUNT_STATUS int(11) default null,
    PRIMARY KEY (id)
);



-- ------------------------------------------------------------------------
-- This is for MySQL, JForum 2.7.1 to JForum 2.8.1
-- ------------------------------------------------------------------------
-- Note : Before running this script back up the updated tables

-- add is_read to jforum_topics_mark to mark messages unread
ALTER TABLE jforum_topics_mark ADD COLUMN is_read TINYINT(1) NOT NULL DEFAULT 0 AFTER mark_time;

-- add released to jforum_evaluations to mark evaluations released/not released
ALTER TABLE jforum_evaluations ADD COLUMN released TINYINT(1) DEFAULT 0 AFTER evaluated_date;

-- add user facebook, twitter accounts to jforum_users
ALTER TABLE jforum_users ADD COLUMN user_facebook_account VARCHAR(255) AFTER sakai_user_id, ADD COLUMN user_twitter_account VARCHAR(255) AFTER user_facebook_account;

-- add priority to private messages
ALTER TABLE jforum_privmsgs ADD COLUMN privmsgs_priority TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 AFTER privmsgs_replied;

-- add start date, end date and lock end date to jforum_categories
ALTER TABLE jforum_categories ADD COLUMN start_date DATETIME AFTER gradable, ADD COLUMN end_date DATETIME AFTER start_date, ADD COLUMN lock_end_date TINYINT(1) UNSIGNED DEFAULT 0 AFTER end_date;

-- table for jforum special access
CREATE TABLE jforum_special_access (
  special_access_id INT UNSIGNED NOT NULL auto_increment,
  forum_id MEDIUMINT(8) UNSIGNED NOT NULL DEFAULT 0,
  start_date DATETIME DEFAULT NULL,
  end_date DATETIME DEFAULT NULL,
  lock_end_date TINYINT(1) UNSIGNED NOT NULL DEFAULT 0,
  override_start_date TINYINT(1) UNSIGNED NOT NULL DEFAULT 0,
  override_end_date TINYINT(1) UNSIGNED NOT NULL DEFAULT 0,
  password VARCHAR(56) DEFAULT NULL,
  users LONGTEXT,
  PRIMARY KEY(special_access_id)
) ENGINE=InnoDB;



-- ------------------------------------------------------------------
-- rSmart additions from schema comparing 2.7.1 and 2.8.0 databases
-- ------------------------------------------------------------------
ALTER TABLE BBB_MEETING ADD COLUMN HOST_URL VARCHAR(255) NOT NULL AFTER NAME;

/* Create table in target */
CREATE TABLE `clog_author`(
	`USER_ID` char(36) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`SITE_ID` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`TOTAL_POSTS` int(11) NOT NULL  ,
	`LAST_POST_DATE` datetime NULL  ,
	`TOTAL_COMMENTS` int(11) NOT NULL  ,
	PRIMARY KEY (`USER_ID`,`SITE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';


/* Create table in target */
CREATE TABLE `clog_autosaved_post`(
	`POST_ID` char(36) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`SITE_ID` varchar(255) COLLATE utf8_general_ci NULL  ,
	`TITLE` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`CONTENT` mediumtext COLLATE utf8_general_ci NOT NULL  ,
	`CREATED_DATE` datetime NOT NULL  ,
	`MODIFIED_DATE` datetime NULL  ,
	`CREATOR_ID` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`KEYWORDS` varchar(255) COLLATE utf8_general_ci NULL  ,
	`ALLOW_COMMENTS` int(11) NULL  ,
	`VISIBILITY` varchar(16) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	PRIMARY KEY (`POST_ID`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';


/* Create table in target */
CREATE TABLE `clog_comment`(
	`COMMENT_ID` char(36) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`POST_ID` char(36) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`CREATOR_ID` char(36) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`CREATED_DATE` datetime NOT NULL  ,
	`MODIFIED_DATE` datetime NOT NULL  ,
	`CONTENT` mediumtext COLLATE utf8_general_ci NOT NULL  ,
	PRIMARY KEY (`COMMENT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';


/* Create table in target */
CREATE TABLE `clog_post`(
	`POST_ID` char(36) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`SITE_ID` varchar(255) COLLATE utf8_general_ci NULL  ,
	`TITLE` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`CONTENT` mediumtext COLLATE utf8_general_ci NOT NULL  ,
	`CREATED_DATE` datetime NOT NULL  ,
	`MODIFIED_DATE` datetime NULL  ,
	`CREATOR_ID` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`KEYWORDS` varchar(255) COLLATE utf8_general_ci NULL  ,
	`ALLOW_COMMENTS` int(11) NULL  ,
	`VISIBILITY` varchar(16) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	PRIMARY KEY (`POST_ID`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';


/* Create table in target */
CREATE TABLE `clog_preferences`(
	`USER_ID` varchar(36) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`SITE_ID` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`EMAIL_FREQUENCY` varchar(32) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	PRIMARY KEY (`USER_ID`,`SITE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';


/* Alter table in target */
ALTER TABLE `contentreview_item`
	ADD COLUMN `errorCode` int(11)   NULL after `nextRetryTime`;

/* Create table in target */
CREATE TABLE `contentreview_sync_item`(
	`id` bigint(20) NOT NULL  auto_increment ,
	`siteId` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`dateQueued` datetime NOT NULL  ,
	`lastTried` datetime NULL  ,
	`status` int(11) NOT NULL  ,
	`messages` text COLLATE utf8_general_ci NULL  ,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';


/* Alter table in target */
ALTER TABLE `eval_evaluation`
	ADD COLUMN `EMAIL_OPEN_NOTIFICATION` bit(1)   NULL after `SELECTION_SETTINGS`,
	ADD COLUMN `REMINDER_STATUS` varchar(255)  COLLATE utf8_general_ci NULL after `EMAIL_OPEN_NOTIFICATION`;



/* Alter table in target */
ALTER TABLE `hierarchy_node_meta`
	ADD COLUMN `isDisabled` bit(1)   NOT NULL after `permToken`;


/* Alter table in target */
ALTER TABLE `mneme_assessment`
	ADD COLUMN `POOL` bigint(20) unsigned   NULL after `TYPE`,
	ADD COLUMN `NEEDSPOINTS` char(1)  COLLATE utf8_general_ci NULL after `POOL`,
	ADD COLUMN `SHOW_MODEL_ANSWER` char(1)  COLLATE utf8_general_ci NULL after `NEEDSPOINTS`,
	ADD COLUMN `FORMAL_EVAL` char(1)  COLLATE utf8_general_ci NULL after `SHOW_MODEL_ANSWER`,
	ADD COLUMN `RESULTS_EMAIL` varchar(255)  COLLATE utf8_general_ci NULL after `FORMAL_EVAL`,
	ADD COLUMN `RESULTS_SENT` bigint(20)   NULL after `RESULTS_EMAIL`,
	ADD KEY `MNEME_ASSESSMENT_IDX_RESULTS`(`RESULTS_EMAIL`,`PUBLISHED`,`RESULTS_SENT`);

/* Alter table in target */
ALTER TABLE `mneme_assessment_part_detail`
	ADD COLUMN `ID` bigint(20) unsigned   NOT NULL auto_increment after `ASSESSMENT_ID`,
	CHANGE `NUM_QUESTIONS_SEQ` `NUM_QUESTIONS_SEQ` int(10) unsigned   NULL after `ID`,
	CHANGE `ORIG_PID` `ORIG_PID` bigint(20) unsigned   NULL after `NUM_QUESTIONS_SEQ`,
	CHANGE `ORIG_QID` `ORIG_QID` bigint(20) unsigned   NULL after `ORIG_PID`,
	CHANGE `PART_ID` `PART_ID` bigint(20) unsigned   NULL after `ORIG_QID`,
	CHANGE `POOL_ID` `POOL_ID` bigint(20) unsigned   NULL after `PART_ID`,
	CHANGE `QUESTION_ID` `QUESTION_ID` bigint(20) unsigned   NULL after `POOL_ID`,
	ADD COLUMN `SEQ` int(10) unsigned   NULL after `QUESTION_ID`,
	ADD COLUMN `POINTS` float   NULL after `SEQ`,
	ADD KEY `MNEME_ASSESSMENT_PART_DETAIL_IDX_POOLID`(`POOL_ID`),
	ADD KEY `MNEME_ASSESSMENT_PART_DETAIL_IDX_QID`(`QUESTION_ID`),
	ADD PRIMARY KEY(`ID`);

/* Alter table in target */
ALTER TABLE `mneme_question`
	ADD COLUMN `PRESENTATION_ATTACHMENTS` longtext  COLLATE utf8_general_ci NULL after `PRESENTATION_TEXT`,
	CHANGE `SURVEY` `SURVEY` char(1)  COLLATE utf8_general_ci NULL after `PRESENTATION_ATTACHMENTS`,
	CHANGE `TYPE` `TYPE` varchar(99)  COLLATE utf8_general_ci NULL after `SURVEY`,
	CHANGE `VALID` `VALID` char(1)  COLLATE utf8_general_ci NULL after `TYPE`,
	CHANGE `GUEST` `GUEST` longtext  COLLATE utf8_general_ci NULL after `VALID`;

/* Alter table in target */
ALTER TABLE `oauth_provider`
  CHANGE `description` `description` text COLLATE utf8_general_ci NULL after `uuid`,
  CHANGE `consumer_key` `consumer_key` varchar(100) COLLATE utf8_general_ci NOT NULL DEFAULT '' after `description`,
  CHANGE `consumer_secret` `hmacSha1SharedSecret` varchar(255) COLLATE utf8_general_ci NULL after `consumer_key`,
  CHANGE `accessTokenURL` `accessTokenURL` varchar(255) COLLATE utf8_general_ci NOT NULL DEFAULT '' after `hmacSha1SharedSecret`,
  CHANGE `requestTokenURL` `requestTokenURL` varchar(255) COLLATE utf8_general_ci NOT NULL DEFAULT '' after `accessTokenURL`,
  CHANGE `userAuthorizationURL` `userAuthorizationURL` varchar(255) COLLATE utf8_general_ci NOT NULL DEFAULT '' after `requestTokenURL`,
  CHANGE `realm` `realm` varchar(100) COLLATE utf8_general_ci NULL after `userAuthorizationURL`,
  CHANGE `name` `name` varchar(100) COLLATE utf8_general_ci NOT NULL DEFAULT '' after `realm`,
  ADD COLUMN `rsakey` text COLLATE utf8_general_ci NULL after `name`,
  ADD COLUMN `signatureMethod` varchar(255) COLLATE utf8_general_ci NOT NULL DEFAULT 'HMAC_SHA1' after `rsakey`,
  ADD COLUMN `enabled` bit(1) DEFAULT 1 NOT NULL DEFAULT 1 after `signatureMethod`;


/* Create table in target */
CREATE TABLE `rsn_user`(
	`USER_EID` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`EMAIL` varchar(255) COLLATE utf8_general_ci NULL  ,
	`EMAIL_LC` varchar(255) COLLATE utf8_general_ci NULL  ,
	`FIRST_NAME` varchar(255) COLLATE utf8_general_ci NULL  ,
	`LAST_NAME` varchar(255) COLLATE utf8_general_ci NULL  ,
	`TYPE` varchar(255) COLLATE utf8_general_ci NULL  ,
	`PW` varchar(255) COLLATE utf8_general_ci NULL  ,
	`CREATEDBY` varchar(99) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`MODIFIEDBY` varchar(99) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`CREATEDON` varchar(99) COLLATE utf8_general_ci NULL  ,
	`MODIFIEDON` varchar(99) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	PRIMARY KEY (`USER_EID`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';


/* Create table in target */
CREATE TABLE `rsn_user_property`(
	`USER_ID` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`PROPERTY_VALUE` text COLLATE utf8_general_ci NOT NULL  ,
	`NAME` varchar(99) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	PRIMARY KEY (`USER_ID`,`NAME`) ,
	KEY `FK759239574F4DCDBB`(`USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';



/* Create table in target */
CREATE TABLE `sakora_job`(
	`UUID` varchar(36) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`START_TIME` datetime NULL  ,
	`END_TIME` datetime NULL  ,
	`STATUS` varchar(255) COLLATE utf8_general_ci NULL  ,
	PRIMARY KEY (`UUID`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';


/* Alter table in target */
ALTER TABLE `sakora_membership`
	CHANGE `ROLE` `ROLE_STR` varchar(255)  COLLATE utf8_general_ci NULL after `COURSE_EID`,
	CHANGE `MODE` `MODE_STR` varchar(8)  COLLATE utf8_general_ci NULL after `ROLE_STR`;

/* Create table in target */
CREATE TABLE `sakora_operation`(
	`UUID` varchar(36) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`TRANS_OP_ID` varchar(255) COLLATE utf8_general_ci NULL  ,
	`SOURCED_ID` varchar(255) COLLATE utf8_general_ci NULL  ,
	`SERVICE` varchar(255) COLLATE utf8_general_ci NULL  ,
	`OPERATION` varchar(255) COLLATE utf8_general_ci NULL  ,
	`STATUS` varchar(255) COLLATE utf8_general_ci NULL  ,
	`RECEIVED_DATE` datetime NULL  ,
	`UPDATED_DATE` datetime NULL  ,
	`COMPLETED_DATE` datetime NULL  ,
	`STATUS_MESSAGE` text COLLATE utf8_general_ci NULL  ,
	`JOB_ID` varchar(36) COLLATE utf8_general_ci NULL  ,
	PRIMARY KEY (`UUID`) ,
	KEY `FKAED638693B8983AF`(`JOB_ID`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';


/* Alter table in target */
ALTER TABLE `scheduler_trigger_events`
	CHANGE `type` `eventType` varchar(255)  COLLATE utf8_general_ci NOT NULL DEFAULT '' after `uuid`,
	CHANGE `time` `eventTime` datetime   NOT NULL after `triggerName`;

/* Create table in target */
CREATE TABLE `signup_attachments`(
	`meeting_id` bigint(20) NOT NULL  ,
	`resource_Id` varchar(255) COLLATE utf8_general_ci NULL  ,
	`file_name` varchar(255) COLLATE utf8_general_ci NULL  ,
	`mime_type` varchar(80) COLLATE utf8_general_ci NULL  ,
	`fileSize` bigint(20) NULL  ,
	`location` varchar(255) COLLATE utf8_general_ci NULL  ,
	`isLink` bit(1) NULL  ,
	`timeslot_id` bigint(20) NULL  ,
	`view_by_all` bit(1) NULL  DEFAULT '' ,
	`created_by` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`created_date` datetime NOT NULL  ,
	`last_modified_by` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`last_modified_date` datetime NOT NULL  ,
	`list_index` int(11) NOT NULL  ,
	PRIMARY KEY (`meeting_id`,`list_index`) ,
	KEY `FK3BCB709CB1E8A17`(`meeting_id`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';


/* Create table in target */
CREATE TABLE `signup_meetings`(
	`id` bigint(20) NOT NULL  auto_increment ,
	`version` int(11) NOT NULL  ,
	`title` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`description` text COLLATE utf8_general_ci NULL  ,
	`location` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`meeting_type` varchar(50) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`creator_user_id` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`start_time` datetime NOT NULL  ,
	`end_time` datetime NOT NULL  ,
	`signup_begins` datetime NULL  ,
	`signup_deadline` datetime NULL  ,
	`canceled` bit(1) NULL  ,
	`locked` bit(1) NULL  ,
	`allow_waitList` bit(1) NULL  DEFAULT '' ,
	`allow_comment` bit(1) NULL  DEFAULT '' ,
	`auto_reminder` bit(1) NULL  ,
	`eid_input_mode` bit(1) NULL  ,
	`receive_email_owner` bit(1) NULL  ,
	`allow_attendance` bit(1) NULL  ,
	`recurrence_id` bigint(20) NULL  ,
	`repeat_type` varchar(20) COLLATE utf8_general_ci NULL  ,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';


/* Create table in target */
CREATE TABLE `signup_site_groups`(
	`signup_site_id` bigint(20) NOT NULL  ,
	`title` varchar(255) COLLATE utf8_general_ci NULL  ,
	`group_id` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`calendar_event_id` text COLLATE utf8_general_ci NULL  ,
	`calendar_id` varchar(255) COLLATE utf8_general_ci NULL  ,
	`list_index` int(11) NOT NULL  ,
	PRIMARY KEY (`signup_site_id`,`list_index`) ,
	KEY `FKC72B75255084316`(`signup_site_id`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';


/* Create table in target */
CREATE TABLE `signup_sites`(
	`id` bigint(20) NOT NULL  auto_increment ,
	`version` int(11) NOT NULL  ,
	`title` varchar(255) COLLATE utf8_general_ci NULL  ,
	`site_id` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`calendar_event_id` text COLLATE utf8_general_ci NULL  ,
	`calendar_id` varchar(255) COLLATE utf8_general_ci NULL  ,
	`meeting_id` bigint(20) NOT NULL  ,
	`list_index` int(11) NULL  ,
	PRIMARY KEY (`id`) ,
	KEY `FKCCD4AC25CB1E8A17`(`meeting_id`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';


/* Create table in target */
CREATE TABLE `signup_ts`(
	`id` bigint(20) NOT NULL  auto_increment ,
	`version` int(11) NOT NULL  ,
	`start_time` datetime NOT NULL  ,
	`end_time` datetime NOT NULL  ,
	`max_no_of_attendees` int(11) NULL  ,
	`display_attendees` bit(1) NULL  ,
	`canceled` bit(1) NULL  ,
	`locked` bit(1) NULL  ,
	`meeting_id` bigint(20) NOT NULL  ,
	`list_index` int(11) NULL  ,
	PRIMARY KEY (`id`) ,
	KEY `FK41154B06CB1E8A17`(`meeting_id`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';


/* Create table in target */
CREATE TABLE `signup_ts_attendees`(
	`timeslot_id` bigint(20) NOT NULL  ,
	`attendee_user_id` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`comments` text COLLATE utf8_general_ci NULL  ,
	`signup_site_id` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`calendar_event_id` varchar(255) COLLATE utf8_general_ci NULL  ,
	`calendar_id` varchar(255) COLLATE utf8_general_ci NULL  ,
	`attended` bit(1) NULL  ,
	`list_index` int(11) NOT NULL  ,
	PRIMARY KEY (`timeslot_id`,`list_index`) ,
	KEY `FKBAB08100CDB30B3D`(`timeslot_id`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';


/* Create table in target */
CREATE TABLE `signup_ts_waitinglist`(
	`timeslot_id` bigint(20) NOT NULL  ,
	`attendee_user_id` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`comments` text COLLATE utf8_general_ci NULL  ,
	`signup_site_id` varchar(255) COLLATE utf8_general_ci NOT NULL  DEFAULT '' ,
	`calendar_event_id` varchar(255) COLLATE utf8_general_ci NULL  ,
	`calendar_id` varchar(255) COLLATE utf8_general_ci NULL  ,
	`attended` bit(1) NULL  ,
	`list_index` int(11) NOT NULL  ,
	PRIMARY KEY (`timeslot_id`,`list_index`) ,
	KEY `FK3AB9A8B2CDB30B3D`(`timeslot_id`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8';

/*-----------------------------------
-- BEGIN: CLE-9062 re-registration of OAuth Tools
-----------------------------------

-- Remove deprecated tool mappings */
DELETE FROM SAKAI_SITE_PAGE WHERE PAGE_ID IN
    (SELECT TOOL_COUNT.PAGE_ID
        FROM
            (SELECT tool.PAGE_ID AS PAGE_ID, COUNT(tool.REGISTRATION) AS REG_COUNT
                FROM SAKAI_SITE_TOOL tool, SAKAI_SITE site
                WHERE (site.TYPE like 'myworkspace%' or site.TYPE in ('aw','myworkspacetadmin'))
                      and site.SITE_ID=tool.SITE_ID and tool.REGISTRATION like 'com.rsmart.oauth%'
                GROUP BY tool.PAGE_ID) AS TOOL_COUNT
        WHERE TOOL_COUNT.REG_COUNT = 1);

DELETE FROM SAKAI_SITE_PAGE_PROPERTY WHERE PAGE_ID IN
    (SELECT TOOL_COUNT.PAGE_ID
        FROM
            (SELECT tool.PAGE_ID AS PAGE_ID, COUNT(tool.REGISTRATION) AS REG_COUNT
                FROM SAKAI_SITE_TOOL tool, SAKAI_SITE site
                WHERE (site.TYPE like 'myworkspace%' or site.TYPE in ('aw','myworkspacetadmin'))
                      and site.SITE_ID=tool.SITE_ID and tool.REGISTRATION like 'com.rsmart.oauth%'
                GROUP BY tool.PAGE_ID) AS TOOL_COUNT
        WHERE TOOL_COUNT.REG_COUNT = 1);

DELETE FROM SAKAI_SITE_TOOL WHERE REGISTRATION LIKE 'com.rsmart.oauth.%';

/*-- Update tool categories*/
UPDATE rsn_to_tool SET tool_id = 'com.rsmart.oauth.token' WHERE tool_id='com.rsmart.oauth.tools';
UPDATE rsn_to_tool SET tool_id = 'com.rsmart.oauth.provider' WHERE tool_id='com.rsmart.oauth.management.tools';

/*-- Add OAuth Tokens tool to all user workspaces and workspace templates*/
INSERT INTO SAKAI_SITE_PAGE (SITE_ID, PAGE_ID, TITLE, LAYOUT, SITE_ORDER, POPUP)
    SELECT page.SITE_ID, concat(page.SITE_ID,'.oauth.token'), 'OAuth Tokens', 0, page.max_order + 1, 0
        FROM (SELECT SITE_ID, max(SITE_ORDER) AS max_order FROM SAKAI_SITE_PAGE GROUP BY SITE_ID) AS page,
            SAKAI_SITE site
        WHERE page.SITE_ID = site.SITE_ID
            AND site.TYPE LIKE 'myworkspace%';

INSERT INTO SAKAI_SITE_PAGE_PROPERTY (SITE_ID, PAGE_ID, NAME, VALUE)
    SELECT page.SITE_ID, concat(page.SITE_ID, '.oauth.token'), 'sitePage.pageCategory', 'My Settings'
        FROM SAKAI_SITE_PAGE  page, SAKAI_SITE site
        WHERE page.SITE_ID = site.SITE_ID
            AND page.PAGE_ID = concat(page.SITE_ID, '.oauth.token')
            AND site.TYPE LIKE 'myworkspace%';

INSERT INTO SAKAI_SITE_TOOL (TOOL_ID, PAGE_ID, SITE_ID, REGISTRATION, PAGE_ORDER, TITLE, LAYOUT_HINTS)
    SELECT page.PAGE_ID, page.PAGE_ID, page.SITE_ID,
        'com.rsmart.oauth.token', 1, NULL, '0,0'
        FROM SAKAI_SITE_PAGE page
        WHERE page.PAGE_ID like '%.oauth.token';

/*-- Add OAuth Providers tool to admin template and admin workspace*/
INSERT INTO SAKAI_SITE_PAGE (SITE_ID, PAGE_ID, TITLE, LAYOUT, SITE_ORDER, POPUP)
    select page.SITE_ID, concat(page.SITE_ID,'.oauth.provider'), 'OAuth Providers', 0, page.max_order + 1, 0
        from (SELECT SITE_ID, max(SITE_ORDER) AS max_order FROM SAKAI_SITE_PAGE GROUP BY SITE_ID) AS page,
            SAKAI_SITE site
        WHERE page.SITE_ID = site.SITE_ID
            AND site.TYPE IN ('aw','myworkspacetadmin');

INSERT INTO SAKAI_SITE_TOOL (TOOL_ID, PAGE_ID, SITE_ID, REGISTRATION, PAGE_ORDER, TITLE, LAYOUT_HINTS)
    SELECT page.PAGE_ID, page.PAGE_ID, page.SITE_ID, 'com.rsmart.oauth.provider', 1, NULL, '0,0'
        FROM SAKAI_SITE_PAGE page, SAKAI_SITE site
        WHERE page.PAGE_ID like '%.oauth.provider'
            AND site.SITE_ID = page.SITE_ID
            AND site.TYPE IN ('aw','myworkspacetadmin');

/*-----------------------------------
-- END: CLE-9062 re-registration of OAuth Tools
-----------------------------------*/

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