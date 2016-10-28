-----------------------------------------------------------------------------
-- ASSIGNMENT_ASSIGNMENT
-----------------------------------------------------------------------------
CREATE TABLE ASSIGNMENT_ASSIGNMENT
(
    ASSIGNMENT_ID NVARCHAR (99) NOT NULL,
	CONTEXT NVARCHAR (99),
    XML NVARCHAR(MAX)
)
;
sp_tableoption 'ASSIGNMENT_ASSIGNMENT', 'large value types out of row', 'true'
;

CREATE UNIQUE INDEX ASSIGNMENT_ASSIGNMENT_INDEX ON ASSIGNMENT_ASSIGNMENT
(
	ASSIGNMENT_ID
)

CREATE INDEX ASSIGNMENT_ASSIGNMENT_CONTEXT ON ASSIGNMENT_ASSIGNMENT
(
	CONTEXT
)

-----------------------------------------------------------------------------
-- ASSIGNMENT_CONTENT
-----------------------------------------------------------------------------

CREATE TABLE ASSIGNMENT_CONTENT
(
    CONTENT_ID NVARCHAR (99) NOT NULL,
	CONTEXT NVARCHAR (99),
    XML NVARCHAR(MAX)
)
;
sp_tableoption 'ASSIGNMENT_CONTENT', 'large value types out of row', 'true'
;
CREATE UNIQUE INDEX ASSIGNMENT_CONTENT_INDEX ON ASSIGNMENT_CONTENT
(
	CONTENT_ID
)

CREATE INDEX ASSIGNMENT_CONTENT_CONTEXT ON ASSIGNMENT_CONTENT
(
	CONTEXT
)

-----------------------------------------------------------------------------
-- ASSIGNMENT_SUBMISSION
-----------------------------------------------------------------------------

CREATE TABLE ASSIGNMENT_SUBMISSION
(
    SUBMISSION_ID NVARCHAR (99) NOT NULL,
	CONTEXT NVARCHAR (99),
    XML NVARCHAR(MAX)
)
;
sp_tableoption 'ASSIGNMENT_SUBMISSION', 'large value types out of row', 'true'
;
CREATE UNIQUE INDEX ASSIGNMENT_SUBMISSION_INDEX ON ASSIGNMENT_SUBMISSION
(
	SUBMISSION_ID
)

CREATE INDEX ASSIGNMENT_SUBMISSION_CONTEXT ON ASSIGNMENT_SUBMISSION
(
	CONTEXT
)
;