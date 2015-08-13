--  This is the MYSQL Sakai 2.9.0 conversion script
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
-- 
--  use this to convert a Sakai database from 2.8.x to 2.9.0.  Run this before you run your first app server.
--  auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
-- 
--  Script insertion format
--  --  [TICKET] [short comment]
--  --  [comment continued] (repeat as necessary)
--  SQL statement
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 

--  KNL-576 provider_id field is too small for large site with long list of provider id
alter table SAKAI_REALM modify PROVIDER_ID varchar(4000);

--  KNL-705 new soft deletion of sites
alter table SAKAI_SITE add IS_SOFTLY_DELETED char(1) not null default 0;
alter table SAKAI_SITE add SOFTLY_DELETED_DATE datetime;

-- CLE-10178 Modify existing softly deleted sites to use column from community soft delete
UPDATE SAKAI_SITE SET IS_SOFTLY_DELETED = 1 WHERE IS_DELETED = 'y';
-- Add current time to sites softly_deleted
UPDATE SAKAI_SITE SET SOFTLY_DELETED_DATE = NOW() WHERE IS_SOFTLY_DELETED = 1;
-- Remove is_deleted column
ALTER TABLE SAKAI_SITE DROP COLUMN IS_DELETED;

--  KNL-725 use a column type that stores the timezone
alter table SAKAI_CLUSTER change UPDATE_TIME UPDATE_TIME timestamp;

--  See KNL-985 - These were removed from 2.9.x pending further investigation

--  KNL-734 type of session and event date column
--  alter table SAKAI_SESSION change SESSION_START SESSION_START timestamp;
--  alter table SAKAI_SESSION change SESSION_END SESSION_END timestamp;
--  alter table SAKAI_EVENT change EVENT_DATE EVENT_DATE timestamp;

--  SAK-19964 Gradebook drop highest and/or lowest or keep highest score for a student
alter table GB_CATEGORY_T add column DROP_HIGHEST int(11) null;
update GB_CATEGORY_T set DROP_HIGHEST = 0;

alter table GB_CATEGORY_T add column KEEP_HIGHEST int(11) null;
update GB_CATEGORY_T set KEEP_HIGHEST = 0;

--  SAK-19731 - Add ability to hide columns in All Grades View for instructors
alter table GB_GRADABLE_OBJECT_T add column (HIDE_IN_ALL_GRADES_TABLE bit default false);
update GB_GRADABLE_OBJECT_T set HIDE_IN_ALL_GRADES_TABLE = 0 where HIDE_IN_ALL_GRADES_TABLE is null;

-- GRBK-945--  increasing column length for GB property value
ALTER TABLE gb_action_record_property_t MODIFY COLUMN PROPERTY_VALUE VARCHAR(2000);
--  SAK-20598 change column type to mediumtext
alter table SAKAI_PERSON_T change NOTES NOTES mediumtext null;
alter table SAKAI_PERSON_T change FAVOURITE_BOOKS FAVOURITE_BOOKS mediumtext null;
alter table SAKAI_PERSON_T change FAVOURITE_TV_SHOWS FAVOURITE_TV_SHOWS mediumtext null;
alter table SAKAI_PERSON_T change FAVOURITE_MOVIES FAVOURITE_MOVIES mediumtext null;
alter table SAKAI_PERSON_T change FAVOURITE_QUOTES FAVOURITE_QUOTES mediumtext null;
alter table SAKAI_PERSON_T change EDUCATION_COURSE EDUCATION_COURSE mediumtext null;
alter table SAKAI_PERSON_T change EDUCATION_SUBJECTS EDUCATION_SUBJECTS mediumtext null;
alter table SAKAI_PERSON_T change STAFF_PROFILE STAFF_PROFILE mediumtext null;
alter table SAKAI_PERSON_T change UNIVERSITY_PROFILE_URL UNIVERSITY_PROFILE_URL mediumtext null;
alter table SAKAI_PERSON_T change ACADEMIC_PROFILE_URL ACADEMIC_PROFILE_URL mediumtext null;
alter table SAKAI_PERSON_T change PUBLICATIONS PUBLICATIONS mediumtext null;
alter table SAKAI_PERSON_T change BUSINESS_BIOGRAPHY BUSINESS_BIOGRAPHY mediumtext null;
--  end SAK-20598

INSERT INTO SAM_TYPE_T (TYPEID,AUTHORITY,DOMAIN, KEYWORD,
	DESCRIPTION,
	STATUS, CREATEDBY, CREATEDDATE, LASTMODIFIEDBY,
	LASTMODIFIEDDATE )
	VALUES (13 , 'stanford.edu' ,'assessment.item' ,'Matrix Choices Survey' ,NULL ,1 ,1 ,
	'2010-01-01 12:00:00',1 ,'2010-01-01 12:00:00');

--  SAM-1255
Update SAM_ASSESSEVALUATION_T
Set ANONYMOUSGRADING = 2
WHERE ASSESSMENTID = (Select ID from SAM_ASSESSMENTBASE_T where TITLE='Default Assessment Type' AND TYPEID='142' AND ISTEMPLATE=1);

--  SAM-1205
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, 1, 'lockedBrowser_isInstructorEditable', 'true')
;

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lockedBrowser_isInstructorEditable', 'true')
;

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lockedBrowser_isInstructorEditable', 'true')
;

--  SAM-1996 (related to SAM-1008 and SAM-1482)
ALTER TABLE SAM_ANSWER_T MODIFY COLUMN `TEXT` text NULL;
ALTER TABLE SAM_PUBLISHEDANSWER_T MODIFY COLUMN `TEXT`  text NULL;

ALTER TABLE SAM_ITEM_T MODIFY COLUMN `INSTRUCTION`  text NULL;
ALTER TABLE SAM_PUBLISHEDITEM_T MODIFY COLUMN `INSTRUCTION`  text NULL;
--  end SAM-1996

--  SAM-1550
--  Apply the following two queries only if you have SAM-988 in your instance
--  Change the date format from:
--  Wed Sep 14 11:40:53 CDT 2011 (output of Date.toString() in SAM-988)
--  to:
--  2012-08-23T10:59:34.180-05:00 (ISO8601 format in SAM-1550)
--  Please make the corresponding time zone changes to the queries:
/*
update SAM_SECTIONMETADATA_T set entry = date_format(str_to_date(entry, '%a %b %d %T SAST %Y'),'%Y-%m-%dT%H:%i:%S.000+02:00') where label='QUESTIONS_RANDOM_DRAW_DATE';
update SAM_PUBLISHEDSECTIONMETADATA_T set entry = date_format(str_to_date(entry, '%a %b %d %T SAST %Y'),'%Y-%m-%dT%H:%i:%S.000+02:00') where label='QUESTIONS_RANDOM_DRAW_DATE';
*/

--  Profile2 v 1.5 conversion START

--  PRFL-498 add the gravatar column, default to 0
alter table PROFILE_PREFERENCES_T add USE_GRAVATAR bit not null DEFAULT false;

--  PRFL-528 add the wall email notification column, default to 1
alter table PROFILE_PREFERENCES_T add EMAIL_WALL_ITEM_NEW bit not null DEFAULT true;

--  PRFL-388 add the worksite email notification column, default to 1
alter table PROFILE_PREFERENCES_T add EMAIL_WORKSITE_NEW bit not null DEFAULT true;

--  PRFL-513 add the wall privacy setting, default to 0
alter table PROFILE_PRIVACY_T add MY_WALL int not null DEFAULT 0;

--  PRFL-518 add profile wall items table
create table PROFILE_WALL_ITEMS_T (
	WALL_ITEM_ID bigint not null auto_increment,
	USER_UUID varchar(99) not null,
	CREATOR_UUID varchar(99) not null,
	WALL_ITEM_TYPE integer not null,
	WALL_ITEM_TEXT text not null,
	WALL_ITEM_DATE datetime not null,
	primary key (WALL_ITEM_ID)
);

create table PROFILE_WALL_ITEM_COMMENTS_T (
	WALL_ITEM_COMMENT_ID bigint not null auto_increment,
	WALL_ITEM_ID bigint not null,
	CREATOR_UUID varchar(99) not null,
	WALL_ITEM_COMMENT_TEXT varchar(4000) not null,
	WALL_ITEM_COMMENT_DATE datetime not null,
	primary key (WALL_ITEM_COMMENT_ID)
);

alter table PROFILE_WALL_ITEM_COMMENTS_T
	add index FK32185F67BEE209 (WALL_ITEM_ID),
	add constraint FK32185F67BEE209
	foreign key (WALL_ITEM_ID)
	references PROFILE_WALL_ITEMS_T (WALL_ITEM_ID);

--  PRFL-350 add the show online status column, default to 1
alter table PROFILE_PREFERENCES_T add SHOW_ONLINE_STATUS bit not null DEFAULT true;
alter table PROFILE_PRIVACY_T add ONLINE_STATUS int not null DEFAULT 0;

--  PRFL-720 add missing sequences
create index PROFILE_WI_USER_UUID_I on PROFILE_WALL_ITEMS_T (USER_UUID);

--  Profile2 v 1.5 conversion END

--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
--  Backfill permissions
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 

--  for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','sitestats.view');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','sitestats.view');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','site.roleswap');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','site.roleswap');

--  lookup the role and function numbers
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

--  insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper")
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
SELECT
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
FROM
    (SELECT DISTINCT SRRF.REALM_KEY, SRRF.ROLE_KEY FROM SAKAI_REALM_RL_FN SRRF) SRRFD
    JOIN PERMISSIONS_TEMP TMP ON (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    JOIN SAKAI_REALM SR ON (SRRFD.REALM_KEY = SR.REALM_KEY)
    WHERE SR.REALM_ID != '!site.helper'
    AND NOT EXISTS (
        SELECT 1
            FROM SAKAI_REALM_RL_FN SRRFI
            WHERE SRRFI.REALM_KEY=SRRFD.REALM_KEY AND SRRFI.ROLE_KEY=SRRFD.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

--  clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;


--  ADDING MSGCNTR Conversion
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
--   MSGCNTR-401   -- -- -
--   Add new Property to prevent users from
--   using Generic Recipients in "To" field (all participants, ect)
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -

INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'msg.permissions.allowToField.allParticipants');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'msg.permissions.allowToField.groups');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'msg.permissions.allowToField.roles');


--  msg.permissions.allowToField.allParticipants and groups and roles is false for all users by default
--  if you want to turn this feature on for all "student/acces" type roles, then run
--  the following conversion:


INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));



--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
--  backfill new msg.permissions.allowGenericRecipientFields permissions into existing realms
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 

--  for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.allowToField.allParticipants');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.allowToField.groups');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.allowToField.roles');


--  lookup the role and function numbers
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

--  insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper")
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
SELECT
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
FROM
    (SELECT DISTINCT SRRF.REALM_KEY, SRRF.ROLE_KEY FROM SAKAI_REALM_RL_FN SRRF) SRRFD
    JOIN PERMISSIONS_TEMP TMP ON (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    JOIN SAKAI_REALM SR ON (SRRFD.REALM_KEY = SR.REALM_KEY)
    WHERE SR.REALM_ID != '!site.helper'
    AND NOT EXISTS (
        SELECT 1
            FROM SAKAI_REALM_RL_FN SRRFI
            WHERE SRRFI.REALM_KEY=SRRFD.REALM_KEY AND SRRFI.ROLE_KEY=SRRFD.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

--  clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;


--  -- -- -- -- -- -- -- -- -- -- -- -- -
--   END MSGCNTR-401   -- -- -
--  -- -- -- -- -- -- -- -- -- -- -- -- -


--  ////////////////////////////////////////////////////
--  // MSGCNTR-411
--  // Post First Option in Forums
--  ////////////////////////////////////////////////////
--  add column to allow postFirst as template setting
alter table MFR_AREA_T add column (POST_FIRST bit);
update MFR_AREA_T set POST_FIRST =0 where POST_FIRST is NULL;
alter table MFR_AREA_T modify column POST_FIRST bit not null;

--  add column to allow POST_FIRST to be set at the forum level
alter table MFR_OPEN_FORUM_T add column (POST_FIRST bit);
update MFR_OPEN_FORUM_T set POST_FIRST =0 where POST_FIRST is NULL;
alter table MFR_OPEN_FORUM_T modify column POST_FIRST bit not null;

--  add column to allow POST_FIRST to be set at the topic level
alter table MFR_TOPIC_T add column POST_FIRST bit AFTER MODERATED;
update MFR_TOPIC_T set POST_FIRST =0 where POST_FIRST is NULL;
alter table MFR_TOPIC_T modify column POST_FIRST bit not null;


--  MSGCNTR-329 - Add BCC option to Messages
alter table MFR_PVT_MSG_USR_T add column BCC bit AFTER READ_STATUS;
update MFR_PVT_MSG_USR_T set BCC=0 where BCC is NULL;
alter table MFR_PVT_MSG_USR_T modify column BCC bit not null DEFAULT b'0';
alter table MFR_MESSAGE_T add column RECIPIENTS_AS_TEXT_BCC TEXT;

--  MSGCNTR-503 - Internationalization of message priority
--  Default locale
update MFR_MESSAGE_T set label='pvt_priority_high' where label='High';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normal';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Low';

--  Locale ar
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='\u0645\u0631\u062A\u0641\u0639';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='\u0639\u0627\u062F\u064A';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='\u0645\u0646\u062E\u0641\u0636';

--  Locale ca
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='Alta';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normal';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Baixa';

--  Locale es
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='Alta';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normal';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Baja';

--  Locale eu
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='Gutxikoa';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normala';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Handikoa';

--  Locale fr_CA
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='\u00C9lev\u00E9e';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normale';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Basse';

--  Locale fr_FR
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='Elev\u00E9e';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normale';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Basse';

--  Locale ja
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='\u9ad8\u3044';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='\u666e\u901a';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='\u4f4e\u3044';

--  Locale nl
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='Hoog';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normaal';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Laag';

--  Locale pt_BR
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='Alta';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normal';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Baixa';

--  Locale pt_PT
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='Alta';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normal';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Baixa';

--  Locale ru
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='\u0412\u044b\u0441\u043e\u043a\u0438\u0439';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='\u041e\u0431\u044b\u0447\u043d\u044b\u0439';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='\u041d\u0438\u0437\u043a\u0438\u0439';

--  Locale sv
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='H\u00F6g';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normal';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='L\u00E5g';

--  Locale zh_TW
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='\u9ad8';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='\u666e\u901a';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='\u4f4e';

--  END MSGCNTR-503 -- 


--  ////////////////////////////////////////////////////
--  // MSGCNTR-438
--  // Add Ability to hide specific groups
--  ////////////////////////////////////////////////////

CREATE TABLE MFR_HIDDEN_GROUPS_T  (
    ID                bigint(20) AUTO_INCREMENT NOT NULL,
    VERSION           int(11) NOT NULL,
    a_surrogateKey    bigint(20) NULL,
    GROUP_ID          varchar(255) NOT NULL,
    PRIMARY KEY(ID)
);

ALTER TABLE MFR_HIDDEN_GROUPS_T
    ADD CONSTRAINT FK1DDE4138A306F94D
    FOREIGN KEY(a_surrogateKey)
    REFERENCES mfr_area_t(ID)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT;

CREATE INDEX FK1DDE4138A306F94D
    ON MFR_HIDDEN_GROUPS_T(a_surrogateKey);


INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'msg.permissions.viewHidden.groups');

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
--  backfill new permission into existing realms
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 

--  for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.viewHidden.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.viewHidden.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.viewHidden.groups');


--  lookup the role and function numbers
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

--  insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper")
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
SELECT
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
FROM
    (SELECT DISTINCT SRRF.REALM_KEY, SRRF.ROLE_KEY FROM SAKAI_REALM_RL_FN SRRF) SRRFD
    JOIN PERMISSIONS_TEMP TMP ON (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    JOIN SAKAI_REALM SR ON (SRRFD.REALM_KEY = SR.REALM_KEY)
    WHERE SR.REALM_ID != '!site.helper'
    AND NOT EXISTS (
        SELECT 1
            FROM SAKAI_REALM_RL_FN SRRFI
            WHERE SRRFI.REALM_KEY=SRRFD.REALM_KEY AND SRRFI.ROLE_KEY=SRRFD.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

--  clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;

--  END MSGCNTR-438 -- 

--  MSGCNTR-569
alter table MFR_TOPIC_T modify CONTEXT_ID varchar(255);

--  END MSGCNTR Conversion

--  SAK-21754
INSERT INTO SAKAI_SITE_PROPERTY VALUES ('!error', 'display-users-present', 'false');

--  PRFL-612 add avatar image url column to uploaded and external image records
ALTER TABLE PROFILE_IMAGES_T ADD RESOURCE_AVATAR VARCHAR(4000) not null AFTER RESOURCE_THUMB;
ALTER TABLE PROFILE_IMAGES_EXTERNAL_T ADD URL_AVATAR VARCHAR(4000) NULL DEFAULT NULL;

--  BLTI-156
CREATE TABLE IF NOT EXISTS lti_mapping (
    id INTEGER NOT NULL AUTO_INCREMENT,
    matchpattern VARCHAR(255) NOT NULL,
    launch VARCHAR(255) NOT NULL,
    note VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
 PRIMARY KEY( id )
);
CREATE TABLE IF NOT EXISTS lti_content (
    id INTEGER NOT NULL AUTO_INCREMENT,
    tool_id INT,
    SITE_ID VARCHAR(99),
    title VARCHAR(255) NOT NULL,
    frameheight INT,
    newpage TINYINT DEFAULT '0',
    debug TINYINT DEFAULT '0',
    custom TEXT(1024),
    launch VARCHAR(255),
    xmlimport TEXT(16384),
    placement VARCHAR(256),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
 PRIMARY KEY( id )
);
CREATE TABLE IF NOT EXISTS lti_tools (
    id INTEGER NOT NULL AUTO_INCREMENT,
    SITE_ID VARCHAR(99),
    title VARCHAR(255) NOT NULL,
    description TEXT(4096),
    status TINYINT DEFAULT '0',
    visible TINYINT DEFAULT '0',
    launch VARCHAR(255) NOT NULL,
    consumerkey VARCHAR(255) NOT NULL,
    secret VARCHAR(255) NOT NULL,
    frameheight INT,
    allowframeheight TINYINT DEFAULT '0',
    sendname TINYINT DEFAULT '0',
    sendemailaddr TINYINT DEFAULT '0',
    newpage TINYINT DEFAULT '0',
    debug TINYINT DEFAULT '0',
    custom TEXT(1024),
    allowcustom TINYINT DEFAULT '0',
    xmlimport TEXT(16384),
    splash TEXT(4096),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
 PRIMARY KEY( id )
);
--  end of BLTI-156

--  STAT-315
CREATE TABLE IF NOT EXISTS SST_SERVERSTATS (
  ID bigint(20) NOT NULL AUTO_INCREMENT,
  ACTIVITY_DATE date NOT NULL,
  EVENT_ID varchar(32) NOT NULL,
  ACTIVITY_COUNT bigint(20) NOT NULL,
  PRIMARY KEY (ID)
);


CREATE TABLE IF NOT EXISTS SST_USERSTATS (
  ID bigint(20) NOT NULL AUTO_INCREMENT,
  LOGIN_DATE date NOT NULL,
  USER_ID varchar(99) NOT NULL,
  LOGIN_COUNT bigint(20) NOT NULL,
  PRIMARY KEY (ID)
);
--  end of STAT-315

--  SAK-21739 Enforce uniqueness on template key + locale
--  The following lines will remove duplicates from your database by filtering first on date and then on max id (tie break).
CREATE TABLE EMAIL_TEMPLATE_ITEM_TEMP (ID INTEGER);
INSERT INTO EMAIL_TEMPLATE_ITEM_TEMP SELECT ID from EMAIL_TEMPLATE_ITEM
  where LAST_MODIFIED not in (select MAX(LAST_MODIFIED) from EMAIL_TEMPLATE_ITEM GROUP BY template_key, template_locale);
DELETE FROM EMAIL_TEMPLATE_ITEM WHERE ID IN (SELECT ID FROM EMAIL_TEMPLATE_ITEM_TEMP);
DELETE FROM EMAIL_TEMPLATE_ITEM_TEMP;
INSERT INTO EMAIL_TEMPLATE_ITEM_TEMP SELECT MAX(ID) FROM EMAIL_TEMPLATE_ITEM GROUP BY UPPER(TEMPLATE_KEY), UPPER(TEMPLATE_LOCALE);
DELETE FROM EMAIL_TEMPLATE_ITEM WHERE ID NOT IN (SELECT ID FROM EMAIL_TEMPLATE_ITEM_TEMP);
DROP TABLE EMAIL_TEMPLATE_ITEM_TEMP;

alter table EMAIL_TEMPLATE_ITEM add unique key EMAIL_TEMPLATE_ITEM_KEY_LOCALE_KEY (TEMPLATE_KEY,TEMPLATE_LOCALE);
--  end of SAK-21739

--  SAK-22223 don't use null as a template key
update EMAIL_TEMPLATE_ITEM set TEMPLATE_LOCALE = 'default' where TEMPLATE_LOCALE is null or TEMPLATE_LOCALE = '';
--  end of SAK-22223


--  KNL-952 - add site.add.project permission to preserve original behaviour
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'site.add.project');
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!user.template.maintain'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.project'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!user.template.registered'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.project'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!user.template.sample'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.project'));
--  end KNL-952

--  SAK-21683 drop a descending index because not necessary for mysql
ALTER TABLE ANNOUNCEMENT_MESSAGE DROP INDEX `IE_ANNC_MSG_DATE_DESC`;
ALTER TABLE MAILARCHIVE_MESSAGE DROP INDEX `IE_MAILARC_MSG_DATE_DESC`;
--  end of SAK-21683

-- Jforum

ALTER TABLE jforum_topics ADD COLUMN start_date DATETIME AFTER topic_export, ADD COLUMN end_date DATETIME AFTER start_date, ADD COLUMN lock_end_date TINYINT(1) UNSIGNED DEFAULT 0 AFTER end_date;

-- add start date, end date and lock end date to jforum_search_topics
ALTER TABLE jforum_search_topics ADD COLUMN start_date DATETIME AFTER topic_export, ADD COLUMN end_date DATETIME AFTER start_date, ADD COLUMN lock_end_date TINYINT(1) UNSIGNED DEFAULT 0 AFTER end_date;

-- add topic_id to jforum_special_access
ALTER TABLE jforum_special_access ADD COLUMN topic_id MEDIUMINT(8) UNSIGNED NOT NULL DEFAULT 0 AFTER forum_id;

-- add min_posts to jforum_grade
ALTER TABLE jforum_grade ADD COLUMN min_posts MEDIUMINT UNSIGNED NOT NULL DEFAULT 1 AFTER categories_id;

-- add override_lock_end_date to jforum_special_access
ALTER TABLE jforum_special_access ADD COLUMN override_lock_end_date TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 AFTER override_end_date;

-- add min_posts_required to jforum_grade
ALTER TABLE jforum_grade ADD COLUMN min_posts_required TINYINT(1) NOT NULL DEFAULT 0 AFTER min_posts;

-- add reviewed_date to jforum_evaluations
ALTER TABLE jforum_evaluations ADD COLUMN reviewed_date DATETIME AFTER released;

-- add index on jforum_grade for context and grade_type
CREATE INDEX idx_jforum_grade_context_grade_type ON jforum_grade(context, grade_type);

-- change the user_lang from NOT NULL to NULL
ALTER TABLE jforum_users MODIFY user_lang VARCHAR(255) NULL DEFAULT NULL;

--  update forum description data type
ALTER TABLE jforum_forums MODIFY forum_desc LONGTEXT NULL DEFAULT NULL;

-- update jforum_posts poster_ip length
ALTER TABLE jforum_posts MODIFY COLUMN poster_ip VARCHAR(50) DEFAULT NULL;
-- 
--  change the user_lang from NOT NULL to NULL
ALTER TABLE jforum_users MODIFY user_lang VARCHAR(255) NULL DEFAULT NULL;
-- 
--  jforum_categories add columns allow_until_date, hide_until_open
ALTER TABLE jforum_categories ADD COLUMN allow_until_date DATETIME NULL DEFAULT NULL  AFTER lock_end_date , ADD COLUMN hide_until_open TINYINT(1) NULL DEFAULT 0  AFTER allow_until_date ;
-- 
--  jforum_forums add columns allow_until_date, hide_until_open
ALTER TABLE jforum_forums ADD COLUMN allow_until_date DATETIME NULL DEFAULT NULL  AFTER lock_end_date , ADD COLUMN hide_until_open TINYINT(1) NULL DEFAULT 0  AFTER allow_until_date ;
-- 
--  jforum_topics add columns allow_until_date, hide_until_open
ALTER TABLE jforum_topics ADD COLUMN allow_until_date DATETIME NULL DEFAULT NULL  AFTER lock_end_date , ADD COLUMN hide_until_open TINYINT(1) NULL DEFAULT 0  AFTER allow_until_date ;
-- 
--  jforum_special_access add columns allow_until_date, hide_until_open, override_allow_until_date, override_hide_until_open
ALTER TABLE jforum_special_access ADD COLUMN allow_until_date DATETIME NULL  AFTER users , ADD COLUMN hide_until_open TINYINT(1) NULL DEFAULT 0  AFTER allow_until_date , ADD COLUMN override_allow_until_date TINYINT(1) NULL DEFAULT 0  AFTER hide_until_open , ADD COLUMN override_hide_until_open TINYINT(1) NULL DEFAULT 0  AFTER override_allow_until_date ;

-- jforum_site_users table
SELECT j.sakai_site_id, j.user_id FROM jforum_site_users j, (SELECT MAX(user_id) AS user_id FROM jforum_users WHERE sakai_user_id != "" GROUP BY sakai_user_id HAVING COUNT(sakai_user_id) > 1 ORDER BY user_id) AS t1 WHERE j.user_id = t1.user_id;

DELETE j.* FROM jforum_site_users j, (SELECT MAX(user_id) AS user_id FROM jforum_users WHERE sakai_user_id != "" GROUP BY sakai_user_id HAVING COUNT(sakai_user_id) > 1 ORDER BY user_id) AS t1 WHERE j.user_id = t1.user_id;

-- jforum_users table
SELECT j.* FROM jforum_users j, (SELECT MAX(user_id) AS user_id FROM jforum_users WHERE sakai_user_id != "" GROUP BY sakai_user_id HAVING COUNT(sakai_user_id) > 1 ORDER BY user_id) AS t1 WHERE j.user_id = t1.user_id;

DELETE j.* FROM jforum_users j, (SELECT MAX(user_id) AS user_id FROM jforum_users WHERE sakai_user_id != "" GROUP BY sakai_user_id HAVING COUNT(sakai_user_id) > 1 ORDER BY user_id) AS t1 WHERE j.user_id = t1.user_id;

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
-- after done with the above queries
-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
-- drop existing index and add unique index on jforum_users table for the column sakai_user_id
ALTER TABLE jforum_users DROP INDEX sakai_user_id_idx;
ALTER TABLE jforum_users ADD UNIQUE INDEX sakai_user_id_idx(sakai_user_id);

-- jforum_posts (post_id)
ALTER TABLE jforum_posts MODIFY COLUMN post_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT;

-- jforum_posts_text (post_id)
ALTER TABLE jforum_posts_text MODIFY COLUMN post_id BIGINT UNSIGNED NOT NULL;

-- jforum_topics (topic_first_post_id, topic_last_post_id)
ALTER TABLE jforum_topics MODIFY COLUMN topic_first_post_id BIGINT UNSIGNED NULL DEFAULT '0', MODIFY COLUMN topic_last_post_id BIGINT UNSIGNED NOT NULL DEFAULT '0';

-- jforum_forums (forum_last_post_id)
ALTER TABLE jforum_forums MODIFY COLUMN forum_last_post_id BIGINT UNSIGNED NOT NULL DEFAULT '0';

-- jforum_attach (post_id)
ALTER TABLE jforum_attach MODIFY COLUMN post_id BIGINT UNSIGNED NULL DEFAULT NULL;

-- jforum_search_wordmatch (post_id) - Database may have huge number of records and current data type is int. This can be ignored to avoid database performance.
-- ALTER TABLE jforum_search_wordmatch MODIFY COLUMN post_id BIGINT UNSIGNED NOT NULL;

-- jforum_search_topics(topic_first_post_id, topic_last_post_id)
ALTER TABLE jforum_search_topics MODIFY COLUMN topic_first_post_id BIGINT UNSIGNED NULL DEFAULT '0', MODIFY COLUMN topic_last_post_id BIGINT UNSIGNED NOT NULL DEFAULT '0';

-- jforum_karma (post_id) - karma is not supported and not in use
ALTER TABLE jforum_karma MODIFY COLUMN post_id BIGINT UNSIGNED NOT NULL;

--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
--  2.9.0-2.9.1 conversion script:
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
--  SAK-22496
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -

UPDATE CITATION_SCHEMA_FIELD SET PROPERTY_VALUE = 'BT,T2' WHERE SCHEMA_ID = 'chapter' AND FIELD_ID = 'sourceTitle' AND PROPERTY_NAME = 'sakai:ris_identifier';
UPDATE CITATION_SCHEMA_FIELD SET PROPERTY_VALUE = 'BT,T2' WHERE SCHEMA_ID = 'proceed' AND FIELD_ID = 'sourceTitle' AND PROPERTY_NAME = 'sakai:ris_identifier';
UPDATE CITATION_SCHEMA_FIELD SET PROPERTY_VALUE = 'JF,JO,JA,J1,J2,BT,T2' WHERE SCHEMA_ID = 'article' AND FIELD_ID = 'sourceTitle' AND PROPERTY_NAME = 'sakai:ris_identifier';

--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
--  end SAK-22496
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -

--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
--  SAK-22745
--  Add realm.upd and realm.del to Instructor and maintain roles for site group templates and site group realms
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -

--  for each realm that has a role matching something in this table, we will add to that role the function from this table

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.del'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.del'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd'));

CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','realm.upd');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','realm.del');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','realm.upd');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','realm.del');

--  lookup the role and function numbers
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)

SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY from PERMISSIONS_SRC_TEMP TMPSRC JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME) JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

--  insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper")
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) SELECT SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY FROM (SELECT DISTINCT SRRF.REALM_KEY, SRRF.ROLE_KEY FROM SAKAI_REALM_RL_FN SRRF) SRRFD JOIN PERMISSIONS_TEMP TMP ON (SRRFD.ROLE_KEY = TMP.ROLE_KEY) JOIN SAKAI_REALM SR ON (SRRFD.REALM_KEY = SR.REALM_KEY) WHERE SR.REALM_ID != '!site.helper' AND SR.REALM_ID like '/site/%/group/%' AND NOT EXISTS (SELECT 1 FROM SAKAI_REALM_RL_FN SRRFI WHERE SRRFI.REALM_KEY=SRRFD.REALM_KEY AND SRRFI.ROLE_KEY=SRRFD.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY);

--  clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
--  End SAK-22745
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -

-- 
--  sam_favoritecolchoicesitem_t
-- 

CREATE TABLE sam_favoritecolchoicesitem_t (FAVORITEITEMID BIGINT(20) AUTO_INCREMENT PRIMARY KEY NOT NULL COMMENT '',
                                           FAVORITEID     BIGINT(20) NOT NULL COMMENT '',
                                           SEQUENCE       INT(11) NOT NULL COMMENT '',
                                           TEXT           TEXT NULL COMMENT '')
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    COLLATE = utf8_general_ci;
CREATE INDEX FK9EEFF4367D691837 ON sam_favoritecolchoicesitem_t (FAVORITEID ASC);



-- 
--  sam_favoritecolchoices_t
-- 

CREATE TABLE sam_favoritecolchoices_t (FAVORITEID   BIGINT(20) AUTO_INCREMENT PRIMARY KEY NOT NULL COMMENT '',
                                       FAVORITENAME VARCHAR(255) NULL COMMENT '',
                                       OWNERID      VARCHAR(255) NULL COMMENT '')
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    COLLATE = utf8_general_ci;

--  this script should be used to upgrade your database from any prior version of signups to the 1.0 release.
ALTER TABLE `signup_meetings` ADD create_groups bit(1) default '\0';
ALTER TABLE signup_meetings
ADD coordinators_user_Ids TEXT;
ALTER TABLE signup_meetings
ADD default_send_email_by_owner BIT(1) DEFAULT b'0';
ALTER TABLE signup_meetings
ADD maxnumof_slot INT(11) DEFAULT 1;


ALTER TABLE `signup_ts` ADD `group_id` VARCHAR(255)  NULL  DEFAULT NULL;

--  add the UUID columns
ALTER TABLE `signup_meetings` ADD `vevent_uuid` VARCHAR(255)  NULL  DEFAULT NULL;
ALTER TABLE `signup_ts` ADD `vevent_uuid` VARCHAR(255)  NULL  DEFAULT NULL;

ALTER TABLE BBB_MEETING ADD COLUMN RECORDING BOOLEAN AFTER END_DATE;
ALTER TABLE BBB_MEETING ADD COLUMN RECORDING_DURATION INT AFTER RECORDING;
ALTER TABLE BBB_MEETING ADD COLUMN VOICE_BRIDGE INT AFTER RECORDING_DURATION;
ALTER TABLE BBB_MEETING ADD COLUMN DELETED INT(1) DEFAULT '0' NOT NULL AFTER PROPERTIES;

-- 
--  FOREIGN KEYS [CREATE]
-- 

ALTER TABLE gb_gradable_object_t ADD CONSTRAINT FK759996A7F09DEFAE FOREIGN KEY (CATEGORY_ID) REFERENCES gb_category_t (ID)
                                                                   ON DELETE NO ACTION
                                                                   ON UPDATE NO ACTION;
ALTER TABLE gb_grade_record_t ADD CONSTRAINT FK46ACF7526F98CFF FOREIGN KEY (GRADABLE_OBJECT_ID) REFERENCES gb_gradable_object_t (ID)
                                                               ON DELETE NO ACTION
                                                               ON UPDATE NO ACTION;
ALTER TABLE mfr_hidden_groups_t ADD CONSTRAINT FK1DDE4138A306F94D FOREIGN KEY (a_surrogateKey) REFERENCES mfr_area_t (ID)
                                                                  ON DELETE NO ACTION
                                                                  ON UPDATE NO ACTION;
ALTER TABLE profile_wall_item_comments_t ADD CONSTRAINT FK32185F67BEE209 FOREIGN KEY (WALL_ITEM_ID) REFERENCES profile_wall_items_t (WALL_ITEM_ID)
                                                                         ON DELETE NO ACTION
                                                                         ON UPDATE NO ACTION;
ALTER TABLE sam_favoritecolchoicesitem_t ADD CONSTRAINT FK9EEFF4367D691837 FOREIGN KEY (FAVORITEID) REFERENCES sam_favoritecolchoices_t (FAVORITEID)
                                                                           ON DELETE NO ACTION
                                                                           ON UPDATE NO ACTION;

--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
--  CLE-10343
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -

insert into rsn_tool_final_config (tool_id, prop_value, prop_key) (select rsn_tool.id, '/sakai-web-portlet','portlet-app-name' from rsn_tool where rsn_tool.tool_id = 'sakai.iframe');
insert into rsn_tool_final_config (tool_id, prop_value, prop_key) (select rsn_tool.id, '/sakai-web-portlet','portlet-context' from rsn_tool where rsn_tool.tool_id = 'sakai.iframe');
insert into rsn_tool_final_config (tool_id, prop_value, prop_key) (select rsn_tool.id, 'PortletIFrame','portlet-name' from rsn_tool where rsn_tool.tool_id = 'sakai.iframe');

insert into rsn_tool_final_config (tool_id, prop_value, prop_key) (select rsn_tool.id, '/sakai-web-portlet','portlet-app-name' from rsn_tool where rsn_tool.tool_id = 'sakai.iframe.service');
insert into rsn_tool_final_config (tool_id, prop_value, prop_key) (select rsn_tool.id, '/sakai-web-portlet','portlet-context' from rsn_tool where rsn_tool.tool_id = 'sakai.iframe.service');
insert into rsn_tool_final_config (tool_id, prop_value, prop_key) (select rsn_tool.id, 'PortletIFrame','portlet-name' from rsn_tool where rsn_tool.tool_id = 'sakai.iframe.service');

insert into rsn_tool_final_config (tool_id, prop_value, prop_key) (select rsn_tool.id, '/sakai-web-portlet','portlet-app-name' from rsn_tool where rsn_tool.tool_id = 'sakai.iframe.myworkspace');
insert into rsn_tool_final_config (tool_id, prop_value, prop_key) (select rsn_tool.id, '/sakai-web-portlet','portlet-context' from rsn_tool where rsn_tool.tool_id = 'sakai.iframe.myworkspace');
insert into rsn_tool_final_config (tool_id, prop_value, prop_key) (select rsn_tool.id, 'PortletIFrame','portlet-name' from rsn_tool where rsn_tool.tool_id = 'sakai.iframe.myworkspace');

insert into rsn_tool_final_config (tool_id, prop_value, prop_key) (select rsn_tool.id, '/sakai-web-portlet','portlet-app-name' from rsn_tool where rsn_tool.tool_id = 'sakai.iframe.site');
insert into rsn_tool_final_config (tool_id, prop_value, prop_key) (select rsn_tool.id, '/sakai-web-portlet','portlet-context' from rsn_tool where rsn_tool.tool_id = 'sakai.iframe.site');
insert into rsn_tool_final_config (tool_id, prop_value, prop_key) (select rsn_tool.id, 'PortletIFrame','portlet-name' from rsn_tool where rsn_tool.tool_id = 'sakai.iframe.site');

--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
--  End CLE-10343
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -

-- REMOVE MNEME TABLES

DROP TABLE MNEME_ANSWER;
DROP TABLE MNEME_ASSESSMENT;
DROP TABLE MNEME_ASSESSMENT_ACCESS;
DROP TABLE MNEME_ASSESSMENT_PART;
DROP TABLE MNEME_ASSESSMENT_PART_DETAIL;
DROP TABLE MNEME_POOL;
DROP TABLE MNEME_QUESTION;
DROP TABLE MNEME_SUBMISSION;

--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
--  CLE-10351
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
ALTER TABLE cm_academic_session_t ADD (SORT_ORDER INT default 0);


ALTER TABLE gb_grade_record_t DROP FOREIGN KEY FK46ACF7525FAC8AAE;

--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
--  CLE-10490
--  -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
update jforum_posts set enable_html = 0;

-- SAK-20884  new gradebook column
ALTER TABLE GB_GRADEBOOK_T ADD COLUMN `DO_SHOW_STATISTICS_CHART`  bit(1) NULL DEFAULT NULL AFTER `DO_SHOW_ITEM_STATS`;
-- end of SAK-20884