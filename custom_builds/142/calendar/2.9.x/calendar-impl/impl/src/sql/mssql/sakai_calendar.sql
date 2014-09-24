-----------------------------------------------------------------------------
-- CALENDAR_CALENDAR
-----------------------------------------------------------------------------

CREATE TABLE CALENDAR_CALENDAR
(
    CALENDAR_ID NVARCHAR (99) NOT NULL,
	NEXT_ID INT,
    XML NVARCHAR(MAX)
)
;
sp_tableoption 'CALENDAR_CALENDAR', 'large value types out of row', 'true'
;

CREATE UNIQUE INDEX CALENDAR_CALENDAR_INDEX ON CALENDAR_CALENDAR
(
	CALENDAR_ID
)

-----------------------------------------------------------------------------
-- CALENDAR_EVENT
-----------------------------------------------------------------------------

CREATE TABLE CALENDAR_EVENT
(
    CALENDAR_ID NVARCHAR (99) NOT NULL,
	EVENT_ID NVARCHAR (36) NOT NULL,
	EVENT_START DATETIME NOT NULL,
	EVENT_END DATETIME NOT NULL,
    XML NVARCHAR(MAX)
)
;
sp_tableoption 'CALENDAR_EVENT', 'large value types out of row', 'true'
;
CREATE INDEX CALENDAR_EVENT_INDEX ON CALENDAR_EVENT
(
	CALENDAR_ID
)
;