-----------------------
-- Melete 2.3 to 2.4 --
-----------------------

CREATE TABLE `melete_module_bkup` as SELECT * FROM `melete_module` WHERE module_id in (SELECT module_id FROM `melete_course_module` where delete_flag=0);
CREATE TABLE `melete_section_bkup` as SELECT * FROM `melete_section` where delete_flag=0;
CREATE TABLE `melete_migrate_status` (START_FLAG tinyint(1),COMPLETE_FLAG tinyint(1));
CREATE TABLE `melete_license` (
 `CODE` int(11) NOT NULL default '0',
 `DESCRIPTION` varchar(40) default NULL,
 PRIMARY KEY  (`CODE`)
 );
CREATE TABLE `melete_resource` (
  `RESOURCE_ID` varchar(255) NOT NULL default '',
  `VERSION` int(11) NOT NULL default '0',
  `LICENSE_CODE` int(11) default NULL,
  `CC_LICENSE_URL` varchar(70) default NULL,
  `REQ_ATTR` tinyint(1) default NULL,
  `ALLOW_CMRCL` tinyint(1) default NULL,
  `ALLOW_MOD` int(11) default NULL,
  `COPYRIGHT_OWNER` varchar(55) default NULL,
  `COPYRIGHT_YEAR` varchar(25) default NULL,
  PRIMARY KEY  (`RESOURCE_ID`)
);
CREATE TABLE `melete_section_resource` (
  `SECTION_ID` int(11) NOT NULL default '0',
  `RESOURCE_ID` varchar(255) default NULL,
  PRIMARY KEY  (`SECTION_ID`)
);


ALTER TABLE melete_section_resource ADD (FOREIGN KEY (RESOURCE_ID) REFERENCES melete_resource(RESOURCE_ID));
ALTER TABLE melete_section_resource ADD (FOREIGN KEY (SECTION_ID) REFERENCES melete_section(SECTION_ID));

ALTER TABLE melete_module ADD COLUMN SEQ_XML TEXT;
ALTER TABLE melete_module MODIFY COLUMN CREATED_BY_FNAME varchar(50);
ALTER TABLE melete_module MODIFY COLUMN CREATED_BY_LNAME varchar(50);
ALTER TABLE melete_module MODIFY COLUMN MODIFIED_BY_FNAME varchar(50);
ALTER TABLE melete_module MODIFY COLUMN MODIFIED_BY_LNAME varchar(50);
ALTER TABLE melete_module DROP COLUMN CC_LICENSE_URL;
ALTER TABLE melete_module DROP COLUMN REQ_ATTR;
ALTER TABLE melete_module DROP COLUMN ALLOW_CMRCL;
ALTER TABLE melete_module DROP COLUMN ALLOW_MOD;

ALTER TABLE melete_section MODIFY COLUMN CREATED_BY_FNAME varchar(50);
ALTER TABLE melete_section MODIFY COLUMN CREATED_BY_LNAME varchar(50);
ALTER TABLE melete_section MODIFY COLUMN MODIFIED_BY_FNAME varchar(50);
ALTER TABLE melete_section MODIFY COLUMN MODIFIED_BY_LNAME varchar(50);
ALTER TABLE melete_section DROP COLUMN SEQ_NO;
ALTER TABLE melete_section DROP COLUMN CONTENT_PATH;
ALTER TABLE melete_section DROP COLUMN UPLOAD_PATH;
ALTER TABLE melete_section DROP COLUMN LINK;


ALTER TABLE melete_user_preference ADD COLUMN EXP_CHOICE tinyint(1);
UPDATE melete_user_preference SET EXP_CHOICE=1;

alter table melete_module drop foreign key `FK54E9B20956234182`;
DROP TABLE melete_module_license;

-- SAK-9808: Implement ability to delete threaded messages within Forums
alter table MFR_MESSAGE_T add DELETED bit not null default false;
create index MFR_MESSAGE_DELETED_I on MFR_MESSAGE_T (DELETED);

-- SAK-10454: Added indexes to imporve Samigo performance
create index SAM_ASSGRAD_AID_PUBASSEID_T on SAM_ASSESSMENTGRADING_T (AGENTID,PUBLISHEDASSESSMENTID);


-- CLE-1361, performace enhancement
DROP TABLE SAKAI_PRESENCE;

CREATE TABLE `SAKAI_PRESENCE` (
`SESSION_ID` varchar(36) default NULL,
`LOCATION_ID` varchar(255) default NULL,
KEY `SAKAI_PRESENCE_SESSION_INDEX` (`SESSION_ID`),
KEY `SAKAI_PRESENCE_LOCATION_INDEX` (`LOCATION_ID`)
) ENGINE=MEMORY DEFAULT CHARSET=utf8;

-- CLE-958
update osp_portal_category_pages set PAGE_LOCALE = 'en_US' where PAGE_LOCALE is null or PAGE_LOCALE = '';


