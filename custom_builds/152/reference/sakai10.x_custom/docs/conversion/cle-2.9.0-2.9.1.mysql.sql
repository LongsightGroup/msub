-- CLE-10579
ALTER TABLE sam_assessmentgrading_t ADD COLUMN HASAUTOSUBMISSIONRUN bit(1) NOT NULL;

Update SAM_ASSESSMENTGRADING_T set HASAUTOSUBMISSIONRUN = 0 where HASAUTOSUBMISSIONRUN is null;

-- SAK-21332 LessonBuilder permissions
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'lessonbuilder.read');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'lessonbuilder.upd');