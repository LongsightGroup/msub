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


-- it is ok if 1 or more statements fail in this script

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'poll.add');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'poll.deleteAny');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'poll.deleteOwn');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'poll.editAny');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'poll.editOwn');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'poll.vote');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'mailtool.admin');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'mailtool.send');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.presentation.edit');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.presentation.copy');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.presentation.layout.suggestPublish');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'asn.receive.notifications');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'sitestats.view');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'sitestats.admin.view');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'skinmanager.create');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'skinmanager.delete');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'skinmanager.edit');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'skinmanager.view');

-- these will get recreated during tomcat startup.  need to drop or else Samigo startup will fail
drop index SAM_ASSGRAD_AID_PUBASSEID_T;
drop index SAM_PUBLISHEDASSESSMENT2_I;
drop index SAM_QPOOL_OWNER_I;

-- verify these are recreated after 2.5 startup
drop index SAM_ANSWER_ITEMID_I;
drop index SAM_PUBMETDATA_ASSESSMENT_I;
drop index SAM_QPOOLITEM_QPOOL_I;
drop index SAM_SECUREDIP_ASSESSMENTID_I;
drop index SAM_SECTION_ASSESSMENTID_I;
drop index SAM_SECTIONMETA_SECTIONID_I;

-- make sure temp tables are gone:
drop table PERMISSIONS_TEMP;
drop table PERMISSIONS_SRC_TEMP;

----Drop existing indexes on jforum_search_wordmatch table
drop index post_id;
drop index word_id;

drop table search_transaction;

-- update for tool category proper functioning
update SAKAI_SITE set custom_page_ordered=0;

