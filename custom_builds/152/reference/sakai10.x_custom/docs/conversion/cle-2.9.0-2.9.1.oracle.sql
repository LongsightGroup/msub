-- CLE-10578 / SAM-973
alter table SAM_ITEMGRADING_t add ISCORRECT number(1,0);

update SAM_ITEMGRADING_T set ISCORRECT = 1 where AUTOSCORE > 0;

-- CLE-10579
ALTER TABLE sam_assessmentgrading_t ADD HASAUTOSUBMISSIONRUN number(1,0);

update SAM_ASSESSMENTGRADING_T set HASAUTOSUBMISSIONRUN = 0 where HASAUTOSUBMISSIONRUN is null;

-- SAK-21332 LessonBuilder permissions
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'lessonbuilder.read');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'lessonbuilder.upd');

create index clog_post_siteid on clog_post (site_id);
create index clog_comment_post_id on clog_comment (post_id);

