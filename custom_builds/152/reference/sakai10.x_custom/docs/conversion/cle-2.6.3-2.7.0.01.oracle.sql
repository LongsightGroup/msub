-- Copyright (c)2010 rSmart. Members of The rSmart Network (RSN) may reproduce, 
-- repurpose, and create derivative works from this document at their discretion.
-- They may also share this document and all derivative works with other organizations
-- in The rSmart Network. All other rights reserved.

-- Contributor(s):
--     mpd

-- This is the Oracle Sakai CLE 2.6.3 -> 2.7.0 conversion script


-- SAK-16610 introduced a new osp presentation review permission
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.presentation.review');

-- SAK-16686/KNL-241 Support exceptions to dynamic page localization
-- this does not work for CLE since ~admin is dynamically generated
-- INSERT INTO SAKAI_SITE_PAGE_PROPERTY VALUES ('~admin','~admin-400','sitePage.customTitle','true');

-- SAK-16832
ALTER TABLE SAM_PUBLISHEDASSESSMENT_T ADD LASTNEEDRESUBMITDATE timestamp NULL;

-- SAK-16880 collaborative portfolio editing
ALTER TABLE osp_presentation ADD isCollab number(1,0) DEFAULT 0 NOT NULL;

-- SAK-17447
alter table EMAIL_TEMPLATE_ITEM add HTMLMESSAGE clob;

-- SAK-16984 new column in sakai-Person
alter TABLE SAKAI_PERSON_T add NORMALIZEDMOBILE varchar2(255) NULL;

-- SAK-17485/SAK-10559
alter table MFR_MESSAGE_T add NUM_READERS int;
update MFR_MESSAGE_T set NUM_READERS = 0;

-- SAK-15710
ALTER TABLE osp_wizard_page_def
  ADD (defaultCustomForm number(1,0), defaultReflectionForm number(1,0), defaultFeedbackForm number(1,0),
  defaultReviewers number(1,0), defaultEvaluationForm number(1,0), defaultEvaluators number(1,0));
UPDATE osp_wizard_page_def
  SET defaultCustomForm = 0, defaultReflectionForm = 0, defaultFeedbackForm = 0,
  defaultReviewers = 0, defaultEvaluationForm = 0, defaultEvaluators = 0;

ALTER TABLE osp_scaffolding ADD (allowRequestFeedback number(1,0));
UPDATE osp_scaffolding SET allowRequestFeedback = 0;

ALTER TABLE osp_scaffolding ADD (hideEvaluations number(1,0));
UPDATE osp_scaffolding SET hideEvaluations = 0;

ALTER TABLE osp_wizard_page_def ADD (allowRequestFeedback number(1,0));
UPDATE osp_wizard_page_def SET allowRequestFeedback = 0;

ALTER TABLE osp_wizard_page_def ADD (hideEvaluations number(1,0));
UPDATE osp_wizard_page_def SET hideEvaluations = 0;

ALTER TABLE osp_scaffolding Drop column reviewerGroupAccess;

ALTER TABLE osp_scaffolding ADD (defaultFormsMatrixVersion number(1,0));
UPDATE osp_scaffolding SET defaultFormsMatrixVersion = 1;

alter table osp_scaffolding add (returnedColor varchar2(7), modifiedDate timestamp);
update osp_scaffolding set returnedColor = '';

create table osp_scaffolding_attachments (
        id varchar2(36) not null,
        artifact_id varchar2(255),
        seq_num number(10,0) not null,
        primary key (id, seq_num)
    );
   
alter table osp_scaffolding_attachments
    add ( constraint FK529713EAE023FB45
	foreign key(id)
	references osp_scaffolding(id));
	
create index FK529713EAE023FB45 on osp_scaffolding_attachments(id);	

create table osp_scaffolding_form_defs (
        id varchar2(36) not null,
        form_def_id varchar2(255),
        seq_num number(10,0) not null,
        primary key (id, seq_num)
    );
   
alter table osp_scaffolding_form_defs
    add ( constraint FK95431263E023FB45
	foreign key(id)
	references osp_scaffolding(id));
	
create index FK95431263E023FB45 on osp_scaffolding_form_defs(id);
   
create table SITEASSOC_CONTEXT_ASSOCIATION (
		FROM_CONTEXT varchar2(99 char) not null,
		TO_CONTEXT varchar2(99 char) not null,
		VERSION number(10,0) not null,
		primary key (FROM_CONTEXT, TO_CONTEXT)
	);

alter table osp_wizard_page_def add (type varchar2(1) default '0');

update osp_wizard_page_def set type = '0' where id in (
select distinct s.wiz_page_def_id From osp_scaffolding_cell s );

update osp_wizard_page_def set type = '1' where id in (
select distinct wps.wiz_page_def_id
From osp_wizard w
join osp_wizard_category wc on wc.wizard_id = w.id
join osp_wizard_page_sequence wps on wps.category_id = wc.id
where w.wizard_type = 'org.theospi.portfolio.wizard.model.Wizard.hierarchical'
);

update osp_wizard_page_def set type = '2' where id in (
select distinct wps.wiz_page_def_id
From osp_wizard w
join osp_wizard_category wc on wc.wizard_id = w.id
join osp_wizard_page_sequence wps on wps.category_id = wc.id
where w.wizard_type = 'org.theospi.portfolio.wizard.model.Wizard.sequential'
);

-- since scaffolding are now extending osp_workflow_parent
insert into OSP_WORKFLOW_PARENT select s.id, null, null, null, null, null, null from osp_scaffolding s where s.id not in (select wp.id from osp_workflow_parent wp);

-- Move the use permission from site to each newly created scaffolding realms and delete the old osp.matrix.scaffolding.use permissions --
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffolding.revise.any');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffolding.revise.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffolding.delete.any');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffolding.delete.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffolding.publish.any');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffolding.publish.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffolding.export.any');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffolding.export.own');

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffoldingSpecific.accessAll');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffoldingSpecific.viewEvalOther');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffoldingSpecific.viewFeedbackOther');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffoldingSpecific.manageStatus');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffoldingSpecific.accessUserList');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffoldingSpecific.viewAllGroups');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffoldingSpecific.use');

INSERT INTO SAKAI_REALM VALUES (SAKAI_REALM_SEQ.NEXTVAL, '!matrix.template.portfolio', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessAll'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewEvalOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewFeedbackOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.manageStatus'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewAllGroups'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));
-- rSmart adds following for !site.template.portfolio defaults
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessAll'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.manageStatus'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewAllGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewEvalOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewFeedbackOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Guest'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.review'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.review'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.review'));
-- end rSmart additions for !site.template.portfolio defaults

-- rSmart adds following for !site.template.course defaults
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessAll'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.manageStatus'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewAllGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewEvalOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewFeedbackOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Guest'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.review'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.review'));
-- end rSmart additions for !site.template.course defaults

-- rSmart adds following for !site.template.project defaults
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessAll'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.manageStatus'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewAllGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewEvalOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewFeedbackOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Guest'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.review'));
-- end rSmart additions for !site.template.project defaults

-- rSmart adds following for !site.template.portfolioAdmin defaults
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));
-- end rSmart additions for !site.template.portfolioAdmin defaults

INSERT INTO SAKAI_REALM VALUES (SAKAI_REALM_SEQ.NEXTVAL, '!matrix.template.course', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessAll'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewEvalOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewFeedbackOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.manageStatus'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewAllGroups'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessAll'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewEvalOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewFeedbackOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.manageStatus'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewAllGroups'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));

INSERT INTO SAKAI_REALM VALUES (SAKAI_REALM_SEQ.NEXTVAL, '!matrix.template.project', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessAll'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewEvalOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewFeedbackOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.manageStatus'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewAllGroups'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));

INSERT INTO SAKAI_REALM (REALM_KEY, REALM_ID, PROVIDER_ID, MAINTAIN_ROLE, CREATEDBY, MODIFIEDBY, CREATEDON, MODIFIEDON)
(select SAKAI_REALM_SEQ.NEXTVAL, concat('/scaffolding/', concat(worksiteId, concat('/', id))) as new_realm_id, '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from osp_scaffolding);

insert into SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
select distinct sr.REALM_KEY, srrf.ROLE_KEY, (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use')
from sakai_realm sr, osp_scaffolding os, SAKAI_REALM_RL_FN srrf
where sr.REALM_ID = concat('/scaffolding/', concat(os.WORKSITEID, concat('/', os.id))) 
and srrf.FUNCTION_KEY = (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.use')
and srrf.REALM_KEY = (select REALM_KEY from SAKAI_REALM Where REALM_ID = concat('/site/', os.worksiteid));

-- delete from SAKAI_REALM_RL_FN where function_key = (select function_key From SAKAI_REALM_FUNCTION where function_name = 'osp.matrix.scaffolding.use');
-- delete From SAKAI_REALM_FUNCTION where function_name = 'osp.matrix.scaffolding.use';

create table permissions_backfill_src_temp (function_name varchar2(99), TYPE INTEGER);
CREATE TABLE permissions_backfill_temp (FUNCTION_KEY INTEGER, TYPE INTEGER);

INSERT INTO permissions_backfill_src_temp values ('osp.matrix.scaffoldingSpecific.accessAll', 1);
INSERT INTO permissions_backfill_src_temp values ('osp.matrix.scaffoldingSpecific.viewEvalOther', 1);
INSERT INTO permissions_backfill_src_temp values ('osp.matrix.scaffoldingSpecific.viewFeedbackOther', 1);
INSERT INTO permissions_backfill_src_temp values ('osp.matrix.scaffoldingSpecific.accessUserList', 1);
INSERT INTO permissions_backfill_src_temp values ('osp.matrix.scaffoldingSpecific.accessAll', 2);
INSERT INTO permissions_backfill_src_temp values ('osp.matrix.scaffoldingSpecific.viewFeedbackOther', 2);
INSERT INTO permissions_backfill_src_temp values ('osp.matrix.scaffoldingSpecific.accessUserList', 2);

insert into permissions_backfill_temp
select rf.function_key, pbst.type
from SAKAI_REALM_FUNCTION rf
join permissions_backfill_src_temp pbst on (pbst.function_name = rf.FUNCTION_NAME);

insert into SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
select distinct sr.REALM_KEY, srrf.ROLE_KEY, pbt.FUNCTION_KEY
from sakai_realm sr, osp_scaffolding os, SAKAI_REALM_RL_FN srrf, permissions_backfill_temp pbt
where sr.REALM_ID = concat('/scaffolding/', concat(os.WORKSITEID, concat('/', os.id)))
and srrf.FUNCTION_KEY = (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.evaluate')
and srrf.REALM_KEY = (select REALM_KEY from SAKAI_REALM Where REALM_ID = concat('/site/', os.worksiteid))
and pbt.TYPE = 1
and not exists (select 1 from SAKAI_REALM_RL_FN rrf_tmp where rrf_tmp.REALM_KEY = sr.REALM_KEY and rrf_tmp.ROLE_KEY = srrf.ROLE_KEY and rrf_tmp.FUNCTION_KEY = pbt.FUNCTION_KEY);

insert into SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
select distinct sr.REALM_KEY, srrf.ROLE_KEY, pbt.FUNCTION_KEY
from sakai_realm sr, osp_scaffolding os, SAKAI_REALM_RL_FN srrf, permissions_backfill_temp pbt
where sr.REALM_ID = concat('/scaffolding/', concat(os.WORKSITEID, concat('/', os.id)))
and srrf.FUNCTION_KEY = (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.review')
and srrf.REALM_KEY = (select REALM_KEY from SAKAI_REALM Where REALM_ID = concat('/site/', os.worksiteid))
and pbt.TYPE = 2
and not exists (select 1 from SAKAI_REALM_RL_FN rrf_tmp where rrf_tmp.REALM_KEY = sr.REALM_KEY and rrf_tmp.ROLE_KEY = srrf.ROLE_KEY and rrf_tmp.FUNCTION_KEY = pbt.FUNCTION_KEY);

drop table permissions_backfill_src_temp;
drop table permissions_backfill_temp;
-- END

-- Backfill sites
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);

INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','osp.matrix.scaffoldingSpecific.manageStatus');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','osp.matrix.scaffoldingSpecific.viewAllGroups');

INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','osp.matrix.scaffoldingSpecific.manageStatus');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','osp.matrix.scaffoldingSpecific.viewAllGroups');

-- rSmart adds following for 'Organizer' role in project & portfolio sites
INSERT INTO PERMISSIONS_SRC_TEMP values ('Organizer','osp.matrix.scaffoldingSpecific.manageStatus');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Organizer','osp.matrix.scaffoldingSpecific.viewAllGroups');
-- end rSmart additions for 'Organizer' role in project & portfolio sites

INSERT INTO PERMISSIONS_SRC_TEMP values ('Project Owner','osp.matrix.scaffoldingSpecific.manageStatus');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Project Owner','osp.matrix.scaffoldingSpecific.viewAllGroups');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','osp.matrix.scaffoldingSpecific.manageStatus');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','osp.matrix.scaffoldingSpecific.viewAllGroups');

-- Lookup the role and function keys
insert into PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
select SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
join SAKAI_REALM_ROLE SRR on (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
join SAKAI_REALM_FUNCTION SRF on (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

-- Insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper" or any group realms)
insert into SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
select
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
from
    (select distinct SRRF.REALM_KEY, SRRF.ROLE_KEY from SAKAI_REALM_RL_FN SRRF) SRRFD
    join PERMISSIONS_TEMP TMP on (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    join SAKAI_REALM SR on (SRRFD.REALM_KEY = SR.REALM_KEY)
    where SR.REALM_ID like '/scaffolding/%'
   and not exists (
        select 1
            from SAKAI_REALM_RL_FN SRRFI
            where SRRFI.REALM_KEY=SRRFD.REALM_KEY and SRRFI.ROLE_KEY=SRRFD.ROLE_KEY and  SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

-- clean up the temp tables to use again for group permissions
drop table PERMISSIONS_TEMP;
drop table PERMISSIONS_SRC_TEMP;

CREATE TABLE permissions_convertl_temp (OLD_FUNCTION_KEY INTEGER, OLD_FUNCTION_NAME VARCHAR2(99), FUNCTION_KEY INTEGER, FUNCTION_NAME VARCHAR2(99));

INSERT INTO permissions_convertl_temp
select rf.FUNCTION_KEY, rf.FUNCTION_NAME, rf2.FUNCTION_KEY, rf2.FUNCTION_NAME
from SAKAI_REALM_FUNCTION rf, SAKAI_REALM_FUNCTION rf2
where rf.FUNCTION_NAME = 'osp.matrix.scaffolding.edit' and (rf2.function_name = 'osp.matrix.scaffolding.revise.any' or rf2.function_name = 'osp.matrix.scaffolding.revise.own');

INSERT INTO permissions_convertl_temp
select rf.FUNCTION_KEY, rf.FUNCTION_NAME, rf2.FUNCTION_KEY, rf2.FUNCTION_NAME
from SAKAI_REALM_FUNCTION rf, SAKAI_REALM_FUNCTION rf2
where rf.FUNCTION_NAME = 'osp.matrix.scaffolding.delete' and (rf2.function_name = 'osp.matrix.scaffolding.delete.any' or rf2.function_name = 'osp.matrix.scaffolding.delete.own');

INSERT INTO permissions_convertl_temp
select rf.FUNCTION_KEY, rf.FUNCTION_NAME, rf2.FUNCTION_KEY, rf2.FUNCTION_NAME
from SAKAI_REALM_FUNCTION rf, SAKAI_REALM_FUNCTION rf2
where rf.FUNCTION_NAME = 'osp.matrix.scaffolding.export' and (rf2.function_name = 'osp.matrix.scaffolding.export.any' or rf2.function_name = 'osp.matrix.scaffolding.export.own');

INSERT INTO permissions_convertl_temp
select rf.FUNCTION_KEY, rf.FUNCTION_NAME, rf2.FUNCTION_KEY, rf2.FUNCTION_NAME
from SAKAI_REALM_FUNCTION rf, SAKAI_REALM_FUNCTION rf2
where rf.FUNCTION_NAME = 'osp.matrix.scaffolding.publish' and (rf2.function_name = 'osp.matrix.scaffolding.publish.any' or rf2.function_name = 'osp.matrix.scaffolding.publish.own');

insert into SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
select distinct srrf.REALM_KEY, srrf.ROLE_KEY, pct.function_key
from SAKAI_REALM_RL_FN srrf
join permissions_convertl_temp pct on (srrf.FUNCTION_KEY = pct.old_function_key);

drop table permissions_convertl_temp;

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.portfolio.evaluation.use');

update SAKAI_REALM_RL_FN set FUNCTION_KEY = (select function_key from SAKAI_REALM_FUNCTION where function_name = 'osp.portfolio.evaluation.use')
where function_key = (select function_key From SAKAI_REALM_FUNCTION where function_name = 'osp.matrix.evaluate');

-- backfill for new returned status
create table tmp_workflow_guid_map (old_id varchar2(99), new_id varchar2(99),
  parent_id varchar2(99), old_status varchar2(99), new_status varchar2(99));

insert into tmp_workflow_guid_map select id, sys_guid(), parent_id, 'READY', 'RETURNED' from OSP_WORKFLOW where title = 'Return Workflow';

delete From tmp_workflow_guid_map where PARENT_ID in (
select distinct parent_id from osp_workflow where title = 'Returned Workflow');

insert into osp_workflow
select NEW_ID, 'Returned Workflow', parent_id from tmp_workflow_guid_map;

insert into osp_workflow_item
select sys_guid(), owi.actiontype, owi.action_object_id, t.new_status, t.new_id
from osp_workflow_item owi
join tmp_workflow_guid_map t on (t.old_id = owi.WORKFLOW_ID and t.old_status = owi.ACTION_VALUE);

insert into osp_workflow_item
select sys_guid(), owi.actiontype, owi.action_object_id, owi.action_value, t.new_id
from osp_workflow_item owi
join tmp_workflow_guid_map t on (t.old_id = owi.WORKFLOW_ID)
where t.old_status <> owi.ACTION_VALUE;

drop table tmp_workflow_guid_map;
-- end backfill for returned status
-- END SAK-15710

-- SAK-16835 columns for new quartz version
--alter table QRTZ_TRIGGERS add PRIORITY number(13) NULL;
--alter table QRTZ_FIRED_TRIGGERS add PRIORITY number(13) NOT NULL;
ALTER TABLE QRTZ_FIRED_TRIGGERS ADD PRIORITY NUMBER(13) DEFAULT 5 NOT NULL;

-- SAK-16835 migrate existing triggers to have default value
-- see http://www.opensymphony.com/quartz/wikidocs/Quartz%201.6.0.html
update QRTZ_TRIGGERS set PRIORITY = 5 where PRIORITY IS NULL;
update QRTZ_FIRED_TRIGGERS set PRIORITY = 5 where PRIORITY IS NULL;

-- Replace Profile by Profile2 for new and existing sites:
-- update SAKAI_SITE_TOOL set REGISTRATION='sakai.profile2' where REGISTRATION='sakai.profile';

-- Replace Profile by Profile2 only for new sites:
-- update SAKAI_SITE_TOOL set REGISTRATION='sakai.profile2' where REGISTRATION='sakai.profile' and SITE_ID='!user';
-- END Profile2 1.3 (SAK-17773)

-- If you set up profile2 by executing the 1st statement above for new & existing sites, then execute the following to update the categorization of the tool in existing myworkspace sites
-- update rsn_to_tool rtt inner join rsn_to_cat rtc on rtt.category_id=rtc.id inner join rsn_to_sitetype rts on rts.id=rtc.site_type_id set rtt.tool_id='sakai.profile2' where rtt.tool_id='sakai.profile' and rts.site_type_name like 'myworkspace%' ;

-- SAK-11740 email notification of new posts to forum
-- You may need to run these drop commands if this table still exists in your db
-- DROP TABLE MFR_EMAIL_NOTIFICATION_TIF EXISTS;
-- drop sequence MFR_EMAIL_NOTIFICATION_S;

CREATE TABLE  "MFR_EMAIL_NOTIFICATION_T"
   (    "ID" NUMBER(19,0) NOT NULL ENABLE,
        "VERSION" NUMBER(10,0) NOT NULL ENABLE,
        "USER_ID" VARCHAR2(255 BYTE) NOT NULL ENABLE,
        "CONTEXT_ID" VARCHAR2(255 BYTE) NOT NULL ENABLE,
        "NOTIFICATION_LEVEL" VARCHAR2(1) NOT NULL ENABLE,
         PRIMARY KEY ("ID")
   );
  
CREATE INDEX "MFR_EMAIL_USER_ID_I" ON  "MFR_EMAIL_NOTIFICATION_T" ("USER_ID")  ;
CREATE INDEX  "MFR_EMAIL_CONTEXT_ID_I" ON  "MFR_EMAIL_NOTIFICATION_T" ("CONTEXT_ID") ;

create sequence MFR_EMAIL_NOTIFICATION_S;

-- SAK-15052 update cafe versions to 2.7.0-SNAPSHOT
alter table MFR_MESSAGE_T add THREADID NUMBER(20);
alter table MFR_MESSAGE_T add LASTTHREADATE TIMESTAMP;
alter table MFR_MESSAGE_T add LASTTHREAPOST NUMBER(20);

update MFR_MESSAGE_T set THREADID=IN_REPLY_TO,LASTTHREADATE=CREATED;

-- SAK-10869 displaying all messages should mark them as read

-- SAK-10869 add AutoMarkThreadsRead functionality to Message Center
-- add column to allow AutoMarkThreadsRead as template setting
alter table MFR_AREA_T add (AUTO_MARK_THREADS_READ NUMBER(1,0));
update MFR_AREA_T set AUTO_MARK_THREADS_READ=0 where AUTO_MARK_THREADS_READ is NULL;
alter table MFR_AREA_T modify (AUTO_MARK_THREADS_READ NUMBER(1,0) not null);

-- add column to allow AutoMarkThreadsRead to be set at the forum level
alter table MFR_OPEN_FORUM_T add (AUTO_MARK_THREADS_READ NUMBER(1,0));
update MFR_OPEN_FORUM_T set AUTO_MARK_THREADS_READ=0 where AUTO_MARK_THREADS_READ is NULL;
alter table MFR_OPEN_FORUM_T modify (AUTO_MARK_THREADS_READ NUMBER(1,0) not null);

-- add column to allow AutoMarkThreadsRead to be set at the topic level
alter table MFR_TOPIC_T add (AUTO_MARK_THREADS_READ NUMBER(1,0));
update MFR_TOPIC_T set AUTO_MARK_THREADS_READ=0 where AUTO_MARK_THREADS_READ is NULL;
alter table MFR_TOPIC_T modify (AUTO_MARK_THREADS_READ NUMBER(1,0) not null);

-- SAK-15655 rework MyWorkspace Synoptic view of Messages & Forums
create table MFR_SYNOPTIC_ITEM
(SYNOPTIC_ITEM_ID number(19,0) not null,
VERSION number(10,0) not null,
USER_ID varchar2(36 char) not null,
SITE_ID varchar2(99 char) not null,
SITE_TITLE varchar2(255 char),
NEW_MESSAGES_COUNT number(10,0),
MESSAGES_LAST_VISIT_DT timestamp,
NEW_FORUM_COUNT number(10,0),
FORUM_LAST_VISIT_DT timestamp,
HIDE_ITEM NUMBER(1,0),
primary key (SYNOPTIC_ITEM_ID),
unique (USER_ID, SITE_ID));

create sequence MFR_SYNOPTIC_ITEM_S;

create index MRF_SYN_USER on MFR_SYNOPTIC_ITEM (USER_ID);

-- MSGCNTR-177 MyWorkspace/Home does now show the Messages & Forums Notifications by default
update SAKAI_SITE_TOOL
Set TITLE = 'Unread Messages and Forums'
Where REGISTRATION = 'sakai.synoptic.messagecenter';

INSERT INTO SAKAI_SITE_TOOL VALUES('!user-145', '!user-100', '!user', 'sakai.synoptic.messagecenter', 2, 'Unread Messages and Forums', '1,1' );

create table MSGCNTR_TMP(
    PAGE_ID VARCHAR2(99),
    SITE_ID VARCHAR2(99)
);

insert into MSGCNTR_TMP
(  
    Select PAGE_ID, SITE_ID
    from SAKAI_SITE_PAGE
    where SITE_ID like '~%'
    and TITLE = 'Home'
    and PAGE_ID not in (Select PAGE_ID from SAKAI_SITE_TOOL where REGISTRATION = 'sakai.synoptic.messagecenter')
);

insert into SAKAI_SITE_TOOL
(select SYS_GUID(), PAGE_ID, SITE_ID, 'sakai.synoptic.messagecenter', 2, 'Unread Messages and Forums', '1,1' from MSGCNTR_TMP);

drop table MSGCNTR_TMP;

-- MSGCNTR-25 .UIPermissionsManagerImpl - query did not return a unique result: 4 Error in catalina.out
alter table MFR_AREA_T add constraint MFR_AREA_CONTEXT_UUID_UNIQUE unique (CONTEXT_ID, TYPE_UUID);

-- MSGCNTR-148 unique constraint not created on MFR_PRIVATE_FORUM_T
-- If this alter query fails, use this select query to find duplicates and remove the duplicate:
-- select OWNER, surrogateKey, COUNT(OWNER) FROM MFR_PRIVATE_FORUM_T GROUP BY OWNER, surrogateKey HAVING COUNT(OWNER)>1;
-- CREATE UNIQUE INDEX MFR_PVT_FRM_OWNER ON  MFR_PRIVATE_FORUM_T (OWNER, surrogateKey);

-- MSGCNTR-132 drop unused MC table columns
ALTER TABLE MFR_MESSAGE_T
DROP COLUMN GRADEBOOK;

ALTER TABLE MFR_MESSAGE_T
DROP COLUMN GRADEBOOK_ASSIGNMENT;

ALTER TABLE MFR_MESSAGE_T
DROP COLUMN GRADECOMMENT;

ALTER TABLE MFR_TOPIC_T
DROP COLUMN GRADEBOOK;

ALTER TABLE MFR_TOPIC_T
DROP COLUMN GRADEBOOK_ASSIGNMENT;

alter table GB_GRADABLE_OBJECT_T
add (
	IS_NULL_ZERO number(1,0)
);
-- END SAK-17428

-- SAK-15311
ALTER TABLE GB_GRADABLE_OBJECT_T
ADD (
SORT_ORDER number(10,0)
);

-- SAK-17679/SAK-18116
alter table EMAIL_TEMPLATE_ITEM add VERSION number(10,0) DEFAULT NULL;

-- SAM-818
alter table SAM_ITEM_T add PARTIAL_CREDIT_FLAG number(1,0) NULL;
alter table SAM_PUBLISHEDITEM_T add PARTIAL_CREDIT_FLAG number(1,0) NULL;
alter table SAM_ANSWER_T add PARTIAL_CREDIT float NULL;
alter table SAM_PUBLISHEDANSWER_T add PARTIAL_CREDIT float NULL;

-- SAM-676
create table SAM_GRADINGATTACHMENT_T (ATTACHMENTID number(19,0) not null, ATTACHMENTTYPE varchar2(255 char) not null, RESOURCEID varchar2(255 char), FILENAME varchar2(255 char), MIMETYPE varchar2(80 char), FILESIZE number(19,0), DESCRIPTION varchar2(4000 char), LOCATION varchar2(4000 char), ISLINK number(1,0), STATUS number(10,0) not null, CREATEDBY varchar2(255 char) not null, CREATEDDATE timestamp not null, LASTMODIFIEDBY varchar2(255 char) not null, LASTMODIFIEDDATE timestamp not null, ITEMGRADINGID number(19,0), primary key (ATTACHMENTID));
create index SAM_GA_ITEMGRADINGID_I on SAM_GRADINGATTACHMENT_T (ITEMGRADINGID);
alter table SAM_GRADINGATTACHMENT_T add constraint FK28156C6C4D7EA7B3 foreign key (ITEMGRADINGID) references SAM_ITEMGRADING_T;
create sequence SAM_GRADINGATTACHMENT_ID_S;

-- SAM-834
UPDATE SAM_ASSESSFEEDBACK_T
SET FEEDBACKDELIVERY = 3, SHOWSTUDENTRESPONSE = 0, SHOWCORRECTRESPONSE = 0, SHOWSTUDENTSCORE = 0, SHOWSTUDENTQUESTIONSCORE = 0,
SHOWQUESTIONLEVELFEEDBACK = 0, SHOWSELECTIONLEVELFEEDBACK = 0, SHOWGRADERCOMMENTS = 0, SHOWSTATISTICS = 0
WHERE ASSESSMENTID in (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TYPEID='142' AND ISTEMPLATE=1);

-- SAK-18370
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.export'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewprofile'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewallmembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewenrollmentstatus'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewhidden'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewofficialphoto'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewgroup'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewprofile'));
-- end SAK-18370

-- SAK-17206
alter table POLL_OPTION add DELETED number(1,0) DEFAULT NULL;

-- rSmart CLE specific updates (includes contrib & other tools)
-- gradebook2 1.1.x -> 1.2.0
update GB_GRADABLE_OBJECT_T
set NOT_COUNTED=IS_UNWEIGHTED
where IS_UNWEIGHTED is not NULL;

update GB_GRADABLE_OBJECT_T
set SORT_ORDER=ITEM_ORDER
where ITEM_ORDER is not NULL;

-- jForum 2.7
-- for quartz job
CREATE TABLE jforum_search_indexing (
  status NUMBER(1) DEFAULT 0 NOT NULL ,
  PRIMARY KEY  (status)
);

-- add gradable to jforum_categories
ALTER TABLE jforum_categories ADD gradable NUMBER(1) DEFAULT 0 NOT NULL;

-- add category id to jforum_grade
ALTER TABLE jforum_grade ADD categories_id NUMBER(10) DEFAULT 0 NOT NULL;

-- add lock due date for jforum_forums
ALTER TABLE jforum_forums ADD lock_end_date NUMBER(1) DEFAULT 0 NOT NULL;

-- for melete 2.7.1
-- alter table melete_user_preference add LICENSE_CODE number(11);
-- alter table melete_user_preference add CC_LICENSE_URL varchar2(275);
-- alter table melete_user_preference add REQ_ATTR number(1);
-- alter table melete_user_preference add ALLOW_CMRCL number(1);
-- alter table melete_user_preference add ALLOW_MOD number(11);
-- alter table melete_user_preference add COPYRIGHT_OWNER varchar2(255);
-- alter table melete_user_preference add COPYRIGHT_YEAR varchar2(25);

-- alter table melete_module_shdates add START_EVENT_ID varchar2(99);
-- alter table melete_module_shdates add END_EVENT_ID varchar2(99);
-- alter table melete_module_shdates add ADDTO_SCHEDULE number(1);

-- alter table melete_resource modify CC_LICENSE_URL varchar2(275);
-- alter table melete_resource modify COPYRIGHT_OWNER varchar2(255);

-- drop table melete_migrate_status;
