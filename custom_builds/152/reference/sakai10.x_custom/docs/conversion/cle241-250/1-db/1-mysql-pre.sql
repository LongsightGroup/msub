-- Copyright 2008 The rSmart Group

-- The contents of this file are subject to the Mozilla Public License
-- Version 1.1 (the "License"); you may not use this file except in
-- compliance with the License. You may obtain a copy of the License at
-- http://www.mozilla.org/MPL/

-- Software distributed under the License is distributed on an "AS IS"
-- basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
-- License for the specific language governing rights and limitations
-- under the License.

-- Contributor(s):
--     mpd 

-- it is ok if 1 or more statements fail in this script with duplicate key errors

INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'poll.add');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'poll.deleteAny');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'poll.deleteOwn');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'poll.editAny');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'poll.editOwn');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'poll.vote');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'mailtool.admin');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'mailtool.send');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'osp.presentation.edit');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'osp.presentation.copy');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'osp.presentation.layout.suggestPublish');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'asn.receive.notifications');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'sitestats.view');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'sitestats.admin.view');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'skinmanager.create');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'skinmanager.delete');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'skinmanager.edit');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'skinmanager.view');

-- these will get recreated during tomcat startup.  need to drop or else Samigo startup will fail
alter table SAM_ASSESSMENTGRADING_T drop index SAM_ASSGRAD_AID_PUBASSEID_T;
alter table SAM_STUDENTGRADINGSUMMARY_T drop index SAM_PUBLISHEDASSESSMENT2_I;
alter table SAM_QUESTIONPOOL_T drop index SAM_QPOOL_OWNER_I;

-- make sure temp tables are gone:
drop table if exists PERMISSIONS_TEMP;
drop table if exists PERMISSIONS_SRC_TEMP;

-- jforum schema changes for mysql
ALTER TABLE jforum_forums MODIFY COLUMN forum_id MEDIUMINT(8) UNSIGNED NOT NULL AUTO_INCREMENT;
ALTER TABLE jforum_posts MODIFY COLUMN forum_id MEDIUMINT(8) UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE jforum_topics MODIFY COLUMN forum_id MEDIUMINT(8) UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE jforum_search_topics MODIFY COLUMN forum_id MEDIUMINT(8) UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE jforum_forum_sakai_groups MODIFY COLUMN forum_id MEDIUMINT(8) UNSIGNED NOT NULL DEFAULT 0;

----Drop existing indexes on jforum_search_wordmatch table
DROP INDEX post_id ON jforum_search_wordmatch;
DROP INDEX word_id ON jforum_search_wordmatch;

drop table if exists search_transaction;

-- update for tool category proper functioning
update SAKAI_SITE set custom_page_ordered=0;

