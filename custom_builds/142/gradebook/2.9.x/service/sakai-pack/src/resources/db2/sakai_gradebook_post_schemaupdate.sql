-- Most automatic database initialization will be taken care of automatically
-- by Hibernate's SchemaUpdate tool, triggered by the hibernate.hbm2ddl.auto
-- property in vanilla Hibernate applications and by the auto.ddl property
-- in the Sakai framework.
--
-- Not all necessary elements might be created by SchemaUpdate, however.
-- Notably, in versions of Hibernate through at least 3.1.3, no explicit
-- index definitions in the mapping file will be honored except during a
-- full SchemaExport.
--
-- This file creates schema in reverse order of when they were added to
-- Gradebook out-of-the-box SQL, to increase the chances that the script
-- will have useful results as an upgrader as well as an initializer.

-- Add indexes for improved performance and reduced locking.
create index GRADEBOOK_ID on GB_GRADABLE_OBJECT_T (GRADEBOOK_ID) ALLOW REVERSE SCANS;
create index GB_GRADE_MAP_GB on GB_GRADE_MAP_T (GRADEBOOK_ID) ALLOW REVERSE SCANS;
create index GB_GRADABLE_OBJASN on GB_GRADABLE_OBJECT_T (OBJECT_TYPE_ID, GRADEBOOK_ID, NAME, REMOVED) ALLOW REVERSE SCANS;
create index GB_GRADE_REC_O_T on GB_GRADE_RECORD_T (OBJECT_TYPE_ID) ALLOW REVERSE SCANS;

-- These two may have already been defined via the 2.1.1 upgrade.
create index GB_GRADE_REC_G_O on GB_GRADE_RECORD_T (GRADABLE_OBJECT_ID) ALLOW REVERSE SCANS;
create index GB_GRADE_REC_STU on GB_GRADE_RECORD_T (STUDENT_ID) ALLOW REVERSE SCANS;
