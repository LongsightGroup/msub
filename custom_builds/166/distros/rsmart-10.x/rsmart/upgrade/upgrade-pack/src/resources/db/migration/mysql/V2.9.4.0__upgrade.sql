-- CLE-10845 Google Docs migration
call AddColumnUnlessExists(DATABASE(), 'OAUTH_PROVIDER', 'clientId', 'VARCHAR(100)');
call AddColumnUnlessExists(DATABASE(), 'OAUTH_PROVIDER', 'clientSecret', 'VARCHAR(100)');
call AddColumnUnlessExists(DATABASE(), 'OAUTH_PROVIDER', 'authUrl', 'VARCHAR(255)');
call AddColumnUnlessExists(DATABASE(), 'OAUTH_PROVIDER', 'tokenUrl', 'VARCHAR(255)');

alter table oauth_provider modify consumer_key varchar(100);
alter table oauth_provider modify accessTokenURL varchar(255);
alter table oauth_provider modify requestTokenURL varchar(255);
alter table oauth_provider modify userAuthorizationURL varchar(255);
alter table oauth_provider modify signatureMethod varchar(255);

call AddColumnUnlessExists(DATABASE(), 'OAUTH_TOKEN', 'protocolVersion', 'VARCHAR(10) DEFAULT 1.0');
update oauth_token set protocolVersion = "1.0" where protocolVersion is null;
alter table oauth_token modify oAuthTokenSecret varchar(100);
-- End CLE-10845

-- CLE-11025/SAK-23812 Peer Review feature for Assignments
CREATE TABLE IF NOT EXISTS ASN_PEER_ASSESSMENT_ITEM_T (
SUBMISSION_ID VARCHAR(255) NOT NULL,
ASSESSOR_USER_ID VARCHAR(255) NOT NULL,
ASSIGNMENT_ID VARCHAR(255) NOT NULL,
SCORE INT(11) NULL,
REVIEW_COMMENT VARCHAR(6000) NULL,
REMOVED bit(1) NULL,
SUBMITTED bit(1) NULL,
PRIMARY KEY(SUBMISSION_ID,ASSESSOR_USER_ID)
);

call AddIndexUnlessExists(DATABASE(), 'asn_peer_assessment_item_t', 'peer_assessor_i', 'submission_id, assessor_user_id');
call AddIndexUnlessExists(DATABASE(), 'asn_peer_assessment_item_t', 'peer_assessor2_i', 'assignment_id, assessor_user_id');
-- END CLE-11025/SAK-23812

-- CLE-11023 Syllabus
call AddColumnUnlessExists(DATABASE(), 'sakai_syllabus_data', 'start_date', 'datetime');
call AddColumnUnlessExists(DATABASE(), 'sakai_syllabus_data', 'end_date', 'datetime');
call AddColumnUnlessExists(DATABASE(), 'sakai_syllabus_data', 'link_calendar', 'bit(1)');
call AddColumnUnlessExists(DATABASE(), 'sakai_syllabus_data', 'calendar_event_id_start', 'varchar(99)');
call AddColumnUnlessExists(DATABASE(), 'sakai_syllabus_data', 'calendar_event_id_end', 'varchar(99)');
-- END CLE-11023

-- CLE-11042 Lesson Builder
alter table lesson_builder_items modify html mediumtext;
alter table lesson_builder_items modify ownerGroups text;
alter table lesson_builder_items modify gradebookId varchar(100);
alter table lesson_builder_items modify altGradebook varchar(100);

call AddIndexUnlessExists(DATABASE(), 'lesson_builder_q_responses', 'lesson_builder_qr_questionId_userId', 'questionId, userId');
call AddIndexUnlessExists(DATABASE(), 'lesson_builder_qr_totals', 'lesson_builder_qr_total_qi', 'questionId');
call AddIndexUnlessExists(DATABASE(), 'lesson_builder_q_responses', 'lesson_builder_qr_questionId', 'questionId');
-- END CLE-11042