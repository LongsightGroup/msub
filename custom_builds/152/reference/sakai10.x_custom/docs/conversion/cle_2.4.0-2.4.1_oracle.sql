/*
  Oracle Upgrade script
  Melete 2.3 to 2.4
*/

CREATE TABLE melete_module_bkup as SELECT * FROM melete_module WHERE MODULE_ID in (SELECT MODULE_ID FROM melete_course_module where DELETE_FLAG=0);
CREATE TABLE melete_section_bkup as SELECT * FROM melete_section where DELETE_FLAG=0;
CREATE TABLE melete_migrate_status (START_FLAG number(1),COMPLETE_FLAG number(1));
CREATE TABLE melete_license (
 CODE number(11) NOT NULL,
 DESCRIPTION varchar2(40),
 PRIMARY KEY  (CODE)
 );
CREATE TABLE melete_resource (
  RESOURCE_ID varchar2(255) NOT NULL,
  VERSION number(11) NOT NULL,
  LICENSE_CODE number(11),
  CC_LICENSE_URL varchar2(70),
  REQ_ATTR number(1),
  ALLOW_CMRCL number(1),
  ALLOW_MOD number(11),
  COPYRIGHT_OWNER varchar2(55),
  COPYRIGHT_YEAR varchar2(25),
  PRIMARY KEY  (RESOURCE_ID)
);
CREATE TABLE melete_section_resource (
  SECTION_ID number(11) NOT NULL,
  RESOURCE_ID varchar2(255),
  PRIMARY KEY  (SECTION_ID)
);

ALTER TABLE melete_section_resource ADD (FOREIGN KEY (RESOURCE_ID) REFERENCES melete_resource(RESOURCE_ID));
ALTER TABLE melete_section_resource ADD (FOREIGN KEY (SECTION_ID) REFERENCES melete_section(SECTION_ID));

ALTER TABLE melete_module ADD (SEQ_XML CLOB);
ALTER TABLE melete_module MODIFY (CREATED_BY_FNAME VARCHAR2(50));
ALTER TABLE melete_module MODIFY (CREATED_BY_LNAME varchar2(50));
ALTER TABLE melete_module MODIFY (MODIFIED_BY_FNAME varchar2(50));
ALTER TABLE melete_module MODIFY (MODIFIED_BY_LNAME varchar2(50));
ALTER TABLE melete_module DROP COLUMN CC_LICENSE_URL;
ALTER TABLE melete_module DROP COLUMN REQ_ATTR;
ALTER TABLE melete_module DROP COLUMN ALLOW_CMRCL;
ALTER TABLE melete_module DROP COLUMN ALLOW_MOD;

ALTER TABLE melete_section MODIFY (CREATED_BY_FNAME varchar2(50));
ALTER TABLE melete_section MODIFY (CREATED_BY_LNAME varchar2(50));
ALTER TABLE melete_section MODIFY (MODIFIED_BY_FNAME varchar2(50));
ALTER TABLE melete_section MODIFY (MODIFIED_BY_LNAME varchar2(50));
ALTER TABLE melete_section DROP COLUMN SEQ_NO;
ALTER TABLE melete_section DROP COLUMN CONTENT_PATH;
ALTER TABLE melete_section DROP COLUMN UPLOAD_PATH;
ALTER TABLE melete_section DROP COLUMN LINK;


ALTER TABLE melete_user_preference ADD (EXP_CHOICE number(1));
UPDATE melete_user_preference SET EXP_CHOICE=1;

ALTER TABLE melete_course_module MODIFY ("DATE_ARCHIVED" DATE);

ALTER TABLE melete_module MODIFY ("CREATION_DATE" DATE);
ALTER TABLE melete_module MODIFY ("MODIFICATION_DATE" DATE);

ALTER TABLE melete_module_bkup MODIFY ("CREATION_DATE" DATE);
ALTER TABLE melete_module_bkup MODIFY ("MODIFICATION_DATE" DATE);

ALTER TABLE melete_module_shdates MODIFY ("START_DATE" DATE);
ALTER TABLE melete_module_shdates MODIFY ("END_DATE" DATE);

ALTER TABLE melete_module_student_privs MODIFY ("START_DATE" DATE);
ALTER TABLE melete_module_student_privs MODIFY ("END_DATE" DATE);

ALTER TABLE melete_section MODIFY ("CREATION_DATE" DATE);
ALTER TABLE melete_section MODIFY ("MODIFICATION_DATE" DATE);

ALTER TABLE melete_section_bkup MODIFY ("CREATION_DATE" DATE);
ALTER TABLE melete_section_bkup MODIFY ("MODIFICATION_DATE" DATE);

ALTER TABLE melete_module DROP COLUMN LICENSE_CODE cascade constraints;
DROP TABLE melete_module_license cascade constraints;

-- SAK-9808: Implement ability to delete threaded messages within Forums
alter table MFR_MESSAGE_T add DELETED number(1, 0) default '0' not null;
update MFR_MESSAGE_T set DELETED=0 where DELETED is NULL;
create index MFR_MESSAGE_DELETED_I on MFR_MESSAGE_T (DELETED);

-- SAK-10454: Added indexes to imporve Samigo performance
create index SAM_AMETADATA_ASSESSMENTID_I on SAM_ASSESSMETADATA_T (ASSESSMENTID);
create index SAM_ANSWER_ITEMID_I on SAM_ANSWER_T (ITEMID);
create index SAM_ASSGRAD_AID_PUBASSEID_T on SAM_ASSESSMENTGRADING_T (AGENTID,PUBLISHEDASSESSMENTID);
create index SAM_PUBMETDATA_ASSESSMENT_I on SAM_PUBLISHEDMETADATA_T(ASSESSMENTID);
create index SAM_QPOOLITEM_QPOOL_I on SAM_QUESTIONPOOLITEM_T (QUESTIONPOOLID);
create index SAM_SECUREDIP_ASSESSMENTID_I on SAM_SECUREDIP_T (ASSESSMENTID);
create index SAM_SECTION_ASSESSMENTID_I on SAM_SECTION_T (ASSESSMENTID);
create index SAM_SECTIONMETA_SECTIONID_I on SAM_SECTIONMETADATA_T (SECTIONID);

-- CLE-958
update OSP_PORTAL_CATEGORY_PAGES set PAGE_LOCALE = 'en_US' where PAGE_LOCALE is null or PAGE_LOCALE = '';
