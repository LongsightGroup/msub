-- SAK-5742 create SAKAI_PERSON_T indexes  
create index SAKAI_PERSON_SURNAME_I on SAKAI_PERSON_T (SURNAME);
create index SAKAI_PERSON_ferpaEnabled_I on SAKAI_PERSON_T (ferpaEnabled);
create index SAKAI_PERSON_GIVEN_NAME_I on SAKAI_PERSON_T (GIVEN_NAME);
create index SAKAI_PERSON_UID_I on SAKAI_PERSON_T (UID_C);
