-- Copyright (c)2010 rSmart. Members of The rSmart Network (RSN) may reproduce, 
-- repurpose, and create derivative works from this document at their discretion.
-- They may also share this document and all derivative works with other organizations
-- in The rSmart Network. All other rights reserved.

-- useful indexes & sequences to have as well as some other conditional statements.
-- It's ok if one or more statements fail in this file.

create index SST_EVENTS_SITE_ID_IX on SST_EVENTS (SITE_ID);
create index SST_EVENTS_USER_ID_IX on SST_EVENTS (USER_ID);
create index SST_EVENTS_EVENT_ID_IX on SST_EVENTS (EVENT_ID);
create index SST_EVENTS_DATE_IX on SST_EVENTS (EVENT_DATE);
create index SST_PREFERENCES_SITE_ID_IX on SST_PREFERENCES (SITE_ID);
create index SST_REPORTS_SITE_ID_IX on SST_REPORTS (SITE_ID);
create index SST_RESOURCES_DATE_IX on SST_RESOURCES (RESOURCE_DATE);
create index SST_RESOURCES_RES_ACT_IDX on SST_RESOURCES (RESOURCE_ACTION);
create index SST_RESOURCES_USER_ID_IX on SST_RESOURCES (USER_ID);
create index SST_RESOURCES_SITE_ID_IX on SST_RESOURCES (SITE_ID);
create index SST_SITEACTIVITY_DATE_IX on SST_SITEACTIVITY (ACTIVITY_DATE);
create index SST_SITEACTIVITY_EVENT_ID_IX on SST_SITEACTIVITY (EVENT_ID);
create index SST_SITEACTIVITY_SITE_ID_IX on SST_SITEACTIVITY (SITE_ID);
create index SST_SITEVISITS_SITE_ID_IX on SST_SITEVISITS (SITE_ID);
create index SST_SITEVISITS_DATE_IX on SST_SITEVISITS (VISITS_DATE);
create index SST_EVENTS_SITEEVENTUSER_ID_IX on SST_EVENTS (SITE_ID,EVENT_ID,USER_ID);
create sequence SST_EVENTS_ID;
create sequence SST_JOB_RUN_ID;
create sequence SST_PREFERENCES_ID;
create sequence SST_REPORTS_ID;
create sequence SST_RESOURCES_ID;
create sequence SST_SITEACTIVITY_ID;
create sequence SST_SITEVISITS_ID;

create index PROFILE_FRIENDS_FRIEND_UUID_I on PROFILE_FRIENDS_T (FRIEND_UUID);
create index PROFILE_FRIENDS_USER_UUID_I on PROFILE_FRIENDS_T (USER_UUID);
create index PROFILE_IMAGES_USER_UUID_I on PROFILE_IMAGES_T (USER_UUID);
create index PROFILE_IMAGES_IS_CURRENT_I on PROFILE_IMAGES_T (IS_CURRENT);
create sequence PROFILE_FRIENDS_S;
create sequence PROFILE_IMAGES_S;
create sequence SAKAI_PERSON_META_S;
create index SAKAI_PERSON_META_USER_UUID_I on SAKAI_PERSON_META_T (USER_UUID);
create index SAKAI_PERSON_META_PROPERTY_I on SAKAI_PERSON_META_T (PROPERTY);

create index
 on MFR_AREA_T(CONTEXT_ID);

create index GB_ACTION_RECORD_ID_IDX on GB_ACTION_RECORD_T(ID);
create index GB_ACTION_RECORD_GRADEBOOK_IDX on GB_ACTION_RECORD_T(GRADEBOOK_UID);
create index GB_ACTION_RECORD_PROP_ID_IDX on GB_ACTION_RECORD_PROPERTY_T(ACTION_RECORD_ID);
create unique index GB_USER_DEREF_USER_IDX on GB_USER_DEREFERENCE_T(USER_UID);
create index GB_USER_DEREF_RM_UP_IDX on GB_USER_DEREF_RM_UPDATE_T(REALM_ID);
create index GB_USER_DEREF_SORT_NM_IDX on GB_USER_DEREFERENCE_T(SORT_NAME);
create index GB_USER_DEREF_EMAIL_IDX on GB_USER_DEREFERENCE_T(EMAIL);
create unique index GB_USER_CONFIG_UNIQ_IDX on GB_USER_CONFIG_T(USER_UID, GRADEBOOK_ID, CONFIG_FIELD);

create index SMB_SEARCH on sakai_message_bundle (BASENAME , MODULE_NAME , LOCALE , PROP_VALUE );

-- SAK-15165 new fields for SakaiPerson (should already be there)
alter table SAKAI_PERSON_T add FAVOURITE_BOOKS varchar2(4000); 
alter table SAKAI_PERSON_T add FAVOURITE_TV_SHOWS varchar2(4000); 
alter table SAKAI_PERSON_T add FAVOURITE_MOVIES varchar2(4000); 
alter table SAKAI_PERSON_T add FAVOURITE_QUOTES varchar2(4000); 
alter table SAKAI_PERSON_T add EDUCATION_COURSE varchar2(4000); 
alter table SAKAI_PERSON_T add EDUCATION_SUBJECTS varchar2(4000);

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.guests'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.guests'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.guests'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Organizer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.guests'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewprofile'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewofficialphoto'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewprofile'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewofficialphoto'));

-- SAK-17428
alter table GB_CATEGORY_T
add (
	IS_EQUAL_WEIGHT_ASSNS number(1,0),
	IS_UNWEIGHTED number(1,0),
	CATEGORY_ORDER number(10,0),
	ENFORCE_POINT_WEIGHTING number(1,0)
);
alter table GB_GRADEBOOK_T
add (
	IS_EQUAL_WEIGHT_CATS number(1,0),
	IS_SCALED_EXTRA_CREDIT number(1,0),
	DO_SHOW_MEAN number(1,0),
	DO_SHOW_MEDIAN number(1,0),
	DO_SHOW_MODE number(1,0),
	DO_SHOW_RANK number(1,0),
	DO_SHOW_ITEM_STATS number(1,0)
);
