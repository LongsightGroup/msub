-- This is the DB2 Sakai 2.6.x -> 2.7.0 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.6.x to 2.7.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- --------------------------------------------------------------------------------------------------------------------------------------

-- SAK-16610 introduced a new osp presentation review permission
INSERT INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES ( 'osp.presentation.review' );

-- SAK-16686/KNL-241 Support exceptions to dynamic page localization
INSERT INTO SAKAI_SITE_PAGE_PROPERTY VALUES ('~admin','~admin-400','sitePage.customTitle','true');

-- SAK-16832
ALTER TABLE SAM_PUBLISHEDASSESSMENT_T ADD LASTNEEDRESUBMITDATE DATE NULL;

-- SAK-16880 collaborative portfolio editing
-- tbd

-- SAK-15165 new fields for SakaiPerson
alter table SAKAI_PERSON_T add FAVOURITE_BOOKS varchar(4000); 
alter table SAKAI_PERSON_T add FAVOURITE_TV_SHOWS varchar(4000); 
alter table SAKAI_PERSON_T add FAVOURITE_MOVIES varchar(4000); 
alter table SAKAI_PERSON_T add FAVOURITE_QUOTES varchar(4000); 
alter table SAKAI_PERSON_T add EDUCATION_COURSE varchar(4000); 
alter table SAKAI_PERSON_T add EDUCATION_SUBJECTS varchar(4000);

-- SAK-16984 new column in sakai-Person
alter TABLE SAKAI_PERSON_T add NORMALIZEDMOBILE varchar(255);

-- SAK-17485/SAK-10559
alter table MFR_MESSAGE_T add NUM_READERS int;
update MFR_MESSAGE_T set NUM_READERS = 0;

-- SAK-16835 columns for new quartz version
alter table QRTZ_TRIGGERS add PRIORITY int;
alter table QRTZ_FIRED_TRIGGERS add PRIORITY int;

-- SAK-17679/SAK-18116
alter table EMAIL_TEMPLATE_ITEM add column VERSION int;
