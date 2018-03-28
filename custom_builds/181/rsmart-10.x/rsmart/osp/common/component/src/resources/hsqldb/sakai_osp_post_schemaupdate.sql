-- Most automatic database initialization will be taken care of automatically
-- by Hibernate's SchemaUpdate tool, triggered by the hibernate.hb2.6.0RC1-SNAPSHOTddl.auto
-- property in vanilla Hibernate applications and by the auto.ddl property
-- in the Sakai framework.
--
-- Not all necessary elements might be created by SchemaUpdate, however.
-- Notably, in versions of Hibernate through at least 3.1.3, no explicit
-- index definitions in the mapping file will be honored except during a
-- full SchemaExport.
--
-- This file creates schema in reverse order of when they were added to
-- out-of-the-box SQL, to increase the chances that the script
-- will have useful results as an upgrader as well as an initializer.

-- Add indexes for improved performance and reduced locking.

create index osp_authz_simple_i on osp_authz_simple (qualifier_id,agent_id,function_name);
