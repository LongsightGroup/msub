-----------------------------------------------------------------------------
-- ANNOUNCEMENT_CHANNEL
-----------------------------------------------------------------------------

CREATE TABLE ANNOUNCEMENT_CHANNEL
(
    CHANNEL_ID NVARCHAR (99) NOT NULL,
	NEXT_ID INT,
    XML NVARCHAR(MAX)
)
;
sp_tableoption 'ANNOUNCEMENT_CHANNEL', 'large value types out of row', 'true'
;
CREATE UNIQUE INDEX ANNOUNCEMENT_CHANNEL_INDEX ON ANNOUNCEMENT_CHANNEL
(
	CHANNEL_ID
)

-----------------------------------------------------------------------------
-- ANNOUNCEMENT_MESSAGE
-----------------------------------------------------------------------------

CREATE TABLE ANNOUNCEMENT_MESSAGE
(
       CHANNEL_ID           NVARCHAR(99) NOT NULL,
       MESSAGE_ID           NVARCHAR(36) NOT NULL,
       DRAFT                CHAR(1) NULL
                                   CHECK (DRAFT IN (1, 0)),
       PUBVIEW              CHAR(1) NULL
                                   CHECK (PUBVIEW IN (1, 0)),
       OWNER                NVARCHAR (99) NULL,
       MESSAGE_DATE         DATETIME NOT NULL,
       XML                  NVARCHAR(MAX) NULL
)
;
sp_tableoption 'ANNOUNCEMENT_MESSAGE', 'large value types out of row', 'true'
;

ALTER TABLE ANNOUNCEMENT_MESSAGE
       ADD  CONSTRAINT ANNOUNCEMENT_MESSAGE_PK PRIMARY KEY (CHANNEL_ID, MESSAGE_ID)

CREATE INDEX IE_ANNC_MSG_CHANNEL ON ANNOUNCEMENT_MESSAGE
(
       CHANNEL_ID                     ASC
)

CREATE INDEX IE_ANNC_MSG_ATTRIB ON ANNOUNCEMENT_MESSAGE
(
       DRAFT                          ASC,
       PUBVIEW                        ASC,
       OWNER                          ASC
)

CREATE INDEX IE_ANNC_MSG_DATE ON ANNOUNCEMENT_MESSAGE
(
       MESSAGE_DATE                   ASC
)

CREATE INDEX IE_ANNC_MSG_DATE_DESC ON ANNOUNCEMENT_MESSAGE
(
       MESSAGE_DATE                   DESC
)

CREATE INDEX ANNOUNCEMENT_MESSAGE_CDD ON ANNOUNCEMENT_MESSAGE
(
	CHANNEL_ID,
	MESSAGE_DATE,
	DRAFT
)
;