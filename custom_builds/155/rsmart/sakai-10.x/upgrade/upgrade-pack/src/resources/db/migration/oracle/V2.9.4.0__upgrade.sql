ALTER TABLE oauth_provider ADD clientId VARCHAR(100) DEFAULT NULL;
ALTER TABLE oauth_provider ADD clientSecret VARCHAR(100) DEFAULT NULL;
ALTER TABLE oauth_provider ADD authUrl VARCHAR(255) DEFAULT NULL;
ALTER TABLE oauth_provider ADD tokenUrl VARCHAR(255) DEFAULT NULL;

ALTER TABLE oauth_provider MODIFY consumer_key VARCHAR(100) DEFAULT NULL;
ALTER TABLE oauth_provider MODIFY accessTokenURL VARCHAR(255) DEFAULT NULL;
ALTER TABLE oauth_provider MODIFY requestTokenURL VARCHAR(255) DEFAULT NULL;
ALTER TABLE oauth_provider MODIFY userAuthorizationURL VARCHAR(255) DEFAULT NULL;
ALTER TABLE oauth_provider MODIFY signatureMethod VARCHAR(255) DEFAULT NULL;

ALTER TABLE oauth_token ADD protocolVersion VARCHAR(10) DEFAULT '1.0';
UPDATE oauth_token SET protocolVersion = '1.0' WHERE protocolVersion IS NULL;
ALTER TABLE oauth_token MODIFY oAuthTokenSecret VARCHAR(100) DEFAULT NULL;

CREATE TABLE ASN_PEER_ASSESSMENT_ITEM_T  (
	SUBMISSION_ID   	varchar2(255) NOT NULL,
	ASSESSOR_USER_ID	varchar2(255) NOT NULL,
	ASSIGNMENT_ID   	varchar2(255) NOT NULL,
	SCORE           	NUMBER(11) NULL,
	REVIEW_COMMENT    clob null,
	REMOVED         	NUMBER(1) NULL,
	SUBMITTED         	NUMBER(1) NULL,
	PRIMARY KEY(SUBMISSION_ID,ASSESSOR_USER_ID)
);

create index PEER_ASSESSOR_I on ASN_PEER_ASSESSMENT_ITEM_T (SUBMISSION_ID, ASSESSOR_USER_ID);
create index PEER_ASSESSOR2_I on ASN_PEER_ASSESSMENT_ITEM_T (ASSIGNMENT_ID, ASSESSOR_USER_ID);

ALTER TABLE sakai_syllabus_data ADD START_DATE TIMESTAMP DEFAULT NULL;
ALTER TABLE sakai_syllabus_data ADD END_DATE TIMESTAMP DEFAULT NULL;
ALTER TABLE sakai_syllabus_data ADD link_calendar CHAR(1) DEFAULT NULL;
ALTER TABLE sakai_syllabus_data ADD calendar_event_id_start VARCHAR(99) DEFAULT NULL;
ALTER TABLE sakai_syllabus_data ADD calendar_event_id_end VARCHAR(99) DEFAULT NULL;

alter table lesson_builder_items add temp clob;
update lesson_builder_items set temp=ownerGroups;
alter table lesson_builder_items drop column ownerGroups;
alter table lesson_builder_items rename column temp to ownerGroups;
alter table lesson_builder_items modify gradebookId varchar2(100 char);
alter table lesson_builder_items modify altGradebook varchar2(100 char);

create index lb_qr_questionId_userId on lesson_builder_q_responses(questionId, userId);
create index lb_qr_total_qi on lesson_builder_qr_totals(questionId);
create index lb_qr_questionId on lesson_builder_q_responses(questionId);