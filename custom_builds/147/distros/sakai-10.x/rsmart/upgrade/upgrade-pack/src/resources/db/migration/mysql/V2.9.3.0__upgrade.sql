-- CLE-10543 / SAK-16499

-- ALTER TABLE `sakai_user` ADD COLUMN `DISABLED` INT NOT NULL DEFAULT 0;
call AddColumnUnlessExists(DATABASE(), 'SAKAI_USER', 'DISABLED', 'INT NOT NULL DEFAULT 0');
UPDATE sakai_user SET DISABLED = 0 WHERE DISABLED is NULL;

-- CLE-10632 / SAM-973

call UpdateColumnDataIfExists(DATABASE(), 'SAM_ITEMGRADING_T', 'ISCORRECT', 'update SAM_ITEMGRADING_T set ISCORRECT = NULL');
call AddColumnUnlessExists(DATABASE(), 'SAM_ITEMGRADING_T', 'ISCORRECT', 'bit');

-- CLE-10648

ALTER TABLE signup_meetings MODIFY COLUMN category VARCHAR(255);

-- CLE-10659
update rsn_tool_final_config set prop_value = '/sakai-web-portlet-basiclti' where (tool_id = (select id from rsn_tool where tool_id = 'sakai.web.168') and (prop_key = 'portlet-app-name' or prop_key = 'portlet-context'));

-- update jforum_posts poster_ip length
ALTER TABLE jforum_posts MODIFY COLUMN poster_ip VARCHAR(50) DEFAULT NULL;
--
-- change the user_lang from NOT NULL to NULL
ALTER TABLE jforum_users MODIFY user_lang VARCHAR(255) NULL DEFAULT NULL;
--
-- jforum_categories add columns allow_until_date, hide_until_open
-- ALTER TABLE jforum_categories ADD COLUMN allow_until_date DATETIME NULL DEFAULT NULL  AFTER lock_end_date , ADD COLUMN hide_until_open TINYINT(1) NULL DEFAULT 0  AFTER allow_until_date ;
call AddColumnUnlessExists(DATABASE(), 'jforum_categories', 'allow_until_date', 'DATETIME NULL DEFAULT NULL AFTER lock_end_date');
call AddColumnUnlessExists(DATABASE(), 'jforum_categories', 'hide_until_open', 'TINYINT(1) NULL DEFAULT 0 AFTER allow_until_date');
--
-- jforum_forums add columns allow_until_date, hide_until_open
-- ALTER TABLE jforum_forums ADD COLUMN allow_until_date DATETIME NULL DEFAULT NULL  AFTER lock_end_date , ADD COLUMN hide_until_open TINYINT(1) NULL DEFAULT 0  AFTER allow_until_date ;
call AddColumnUnlessExists(DATABASE(), 'jforum_forums', 'allow_until_date', 'DATETIME NULL DEFAULT NULL AFTER lock_end_date');
call AddColumnUnlessExists(DATABASE(), 'jforum_forums', 'hide_until_open', 'TINYINT(1) NULL DEFAULT 0 AFTER allow_until_date');
--
-- jforum_topics add columns allow_until_date, hide_until_open
-- ALTER TABLE jforum_topics ADD COLUMN allow_until_date DATETIME NULL DEFAULT NULL  AFTER lock_end_date , ADD COLUMN hide_until_open TINYINT(1) NULL DEFAULT 0  AFTER allow_until_date ;
call AddColumnUnlessExists(DATABASE(), 'jforum_topics', 'allow_until_date', 'DATETIME NULL DEFAULT NULL AFTER lock_end_date');
call AddColumnUnlessExists(DATABASE(), 'jforum_topics', 'hide_until_open', 'TINYINT(1) NULL DEFAULT 0 AFTER allow_until_date');
--
-- jforum_special_access add columns allow_until_date, hide_until_open, override_allow_until_date, override_hide_until_open
-- ALTER TABLE jforum_special_access ADD COLUMN allow_until_date DATETIME NULL  AFTER users , ADD COLUMN hide_until_open TINYINT(1) NULL DEFAULT 0  AFTER allow_until_date , ADD COLUMN override_allow_until_date TINYINT(1) NULL DEFAULT 0  AFTER hide_until_open , ADD COLUMN override_hide_until_open TINYINT(1) NULL DEFAULT 0  AFTER override_allow_until_date ;
call AddColumnUnlessExists(DATABASE(), 'jforum_special_access', 'allow_until_date', 'DATETIME NULL AFTER users');
call AddColumnUnlessExists(DATABASE(), 'jforum_special_access', 'hide_until_open', 'TINYINT(1) NULL DEFAULT 0 AFTER allow_until_date');
call AddColumnUnlessExists(DATABASE(), 'jforum_special_access', 'override_allow_until_date', 'TINYINT(1) NULL DEFAULT 0 AFTER hide_until_open');
call AddColumnUnlessExists(DATABASE(), 'jforum_special_access', 'override_hide_until_open', 'TINYINT(1) NULL DEFAULT 0 AFTER override_allow_until_date');

-- added user accounts for google plus, skype, linkedIn
-- ALTER TABLE jforum_users ADD COLUMN user_google_plus VARCHAR(255) NULL DEFAULT NULL  AFTER user_twitter_account , ADD COLUMN user_skype VARCHAR(255) NULL DEFAULT NULL  AFTER user_google_plus , ADD COLUMN user_linkedIn VARCHAR(255) NULL DEFAULT NULL  AFTER user_skype;
call AddColumnUnlessExists(DATABASE(), 'jforum_users', 'user_google_plus', 'VARCHAR(255) NULL DEFAULT NULL AFTER user_twitter_account');
call AddColumnUnlessExists(DATABASE(), 'jforum_users', 'user_skype', 'VARCHAR(255) NULL DEFAULT NULL AFTER user_google_plus');
call AddColumnUnlessExists(DATABASE(), 'jforum_users', 'user_linkedIn', 'VARCHAR(255) NULL DEFAULT NULL AFTER user_skype');
-- --------------------------------------------------------------------------------------------------------------------------------------
-- Community 2.9.1-2.9.2 conversion
-- --------------------------------------------------------------------------------------------------------------------------------------
-- BLTI-222
-- ALTER TABLE lti_content ADD pagetitle VARCHAR(255);
call AddColumnUnlessExists(DATABASE(), 'lti_content', 'pagetitle', 'VARCHAR(255)');
ALTER TABLE lti_content MODIFY launch TEXT(1024);
-- ALTER TABLE lti_content ADD consumerkey VARCHAR(255);
call AddColumnUnlessExists(DATABASE(), 'lti_content', 'consumerkey', 'VARCHAR(255)');
-- ALTER TABLE lti_content ADD secret VARCHAR(255);
call AddColumnUnlessExists(DATABASE(), 'lti_content', 'secret', 'VARCHAR(255)');
-- ALTER TABLE lti_content ADD settings TEXT(8096);
call AddColumnUnlessExists(DATABASE(), 'lti_content', 'settings', 'TEXT(8096)');
-- ALTER TABLE lti_content ADD placementsecret TEXT(512);
call AddColumnUnlessExists(DATABASE(), 'lti_content', 'placementsecret', 'TEXT(512)');
-- ALTER TABLE lti_content ADD oldplacementsecret TEXT(512);
call AddColumnUnlessExists(DATABASE(), 'lti_content', 'oldplacementsecret', 'TEXT(512)');
-- ALTER TABLE lti_tools ADD allowtitle TINYINT DEFAULT '0';
call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'allowtitle', 'TINYINT DEFAULT 0');
-- ALTER TABLE lti_tools ADD pagetitle VARCHAR(255);
call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'pagetitle', 'VARCHAR(255)');
-- ALTER TABLE lti_tools ADD allowpagetitle TINYINT DEFAULT '0';
call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'allowpagetitle', 'TINYINT DEFAULT 0');
ALTER TABLE lti_tools MODIFY launch TEXT(1024);
-- ALTER TABLE lti_tools ADD allowlaunch TINYINT DEFAULT '0';
call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'allowlaunch', 'TINYINT DEFAULT 0');
-- ALTER TABLE lti_tools ADD domain VARCHAR(255);
call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'domain', 'VARCHAR(255)');
-- ALTER TABLE lti_tools ADD allowconsumerkey TINYINT DEFAULT '0';
call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'allowconsumerkey', 'TINYINT DEFAULT 0');
-- ALTER TABLE lti_tools ADD allowsecret TINYINT DEFAULT '0';
call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'allowsecret', 'TINYINT DEFAULT 0');
-- ALTER TABLE lti_tools ADD allowoutcomes TINYINT DEFAULT '0';
call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'allowoutcomes', 'TINYINT DEFAULT 0');
-- ALTER TABLE lti_tools ADD allowroster TINYINT DEFAULT '0';
call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'allowroster', 'TINYINT DEFAULT 0');
-- ALTER TABLE lti_tools ADD allowsettings TINYINT DEFAULT '0';
call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'allowsettings', 'TINYINT DEFAULT 0');
-- ALTER TABLE lti_tools ADD allowlori TINYINT DEFAULT '0';
call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'allowlori', 'TINYINT DEFAULT 0');

-- BLTI-208
ALTER TABLE lti_tools MODIFY launch VARCHAR(255) NULL;
ALTER TABLE lti_tools MODIFY consumerkey VARCHAR(255) NULL;
ALTER TABLE lti_tools MODIFY secret VARCHAR(255) NULL;

-- SAK-23452 Roster throws errors in 2.9 after MySQL upgrade
update PROFILE_PREFERENCES_T set USE_GRAVATAR = false where USE_GRAVATAR is null;
update PROFILE_PREFERENCES_T set EMAIL_WALL_ITEM_NEW = true where EMAIL_WALL_ITEM_NEW is null;
update PROFILE_PREFERENCES_T set EMAIL_WORKSITE_NEW = true where EMAIL_WORKSITE_NEW is null;
update PROFILE_PREFERENCES_T set SHOW_ONLINE_STATUS = true where SHOW_ONLINE_STATUS is null;
update PROFILE_PRIVACY_T set MY_WALL = 0 where MY_WALL is null;
update PROFILE_PRIVACY_T set ONLINE_STATUS = 0 where ONLINE_STATUS is null;

-- --------------------------------------------------------------------------------------------------------------------------------------
-- Community 2.9.2-2.9.3 conversion
-- --------------------------------------------------------------------------------------------------------------------------------------
-- LSNBLDR-227
-- alter table lesson_builder_groups add siteId varchar(250);
call AddColumnUnlessExists(DATABASE(), 'lesson_builder_groups', 'siteId', 'varchar(250)');
alter table lesson_builder_items modify description text;
alter table lesson_builder_items modify groups text;

create table if not exists lesson_builder_properties (
     id bigint not null auto_increment,
     attribute varchar(255) not null unique,
     value longtext,
     primary key (id)
);

-- create index lesson_builder_group_site on lesson_builder_groups(siteId);
call AddIndexUnlessExists(DATABASE(), 'lesson_builder_groups', 'lesson_builder_group_site', 'siteId');
-- create index lesson_builder_item_gb on lesson_builder_items(gradebookid);
call AddIndexUnlessExists(DATABASE(), 'lesson_builder_items', 'lesson_builder_item_gb', 'gradebookid');
-- create index lesson_builder_item_altgb on lesson_builder_items(altGradebook);
call AddIndexUnlessExists(DATABASE(), 'lesson_builder_items', 'lesson_builder_item_altgb', 'altGradebook');
-- create index lesson_builder_prop_idx on lesson_builder_properties(attribute);
call AddIndexUnlessExists(DATABASE(), 'lesson_builder_properties', 'lesson_builder_prop_idx', 'attribute');
-- end LSNBLDR-227

-- BLTI-238
ALTER TABLE lti_mapping MODIFY     matchpattern VARCHAR(255);
ALTER TABLE lti_mapping MODIFY     launch VARCHAR(255);
ALTER TABLE lti_content MODIFY     title VARCHAR(255);
ALTER TABLE lti_tools   MODIFY     title VARCHAR(255);
ALTER TABLE lti_tools   MODIFY     launch TEXT(1024);
ALTER TABLE lti_tools   MODIFY     consumerkey VARCHAR(255);
ALTER TABLE lti_tools   MODIFY     secret VARCHAR(255);
-- end BLTI-238

-- SAM-973
-- alter table SAM_ITEMGRADING_t add ISCORRECT bit;
call AddColumnUnlessExists(DATABASE(), 'SAM_ITEMGRADING_t', 'ISCORRECT', 'bit');
-- end SAM-973

-- CLE-10126 Profile photos not displaying properly
update profile_images_t set RESOURCE_AVATAR = RESOURCE_MAIN where RESOURCE_AVATAR is NULL;
