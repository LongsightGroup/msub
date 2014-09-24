-- Gradebook table changes between Sakai 2.1.* and 2.2.

-- Add grading scale support.
create table GB_PROPERTY_T (
   ID bigint not null identity,
   VERSION integer not null,
   NAME nvarchar(255) not null unique,
   VALUE nvarchar(255),
   primary key (ID)
)
create table GB_GRADING_SCALE_GRADES_T (
   GRADING_SCALE_ID bigint not null,
   LETTER_GRADE nvarchar(255),
   GRADE_IDX integer not null,
   primary key (GRADING_SCALE_ID, GRADE_IDX)
)
create table GB_GRADING_SCALE_T (
   ID bigint not null identity,
   OBJECT_TYPE_ID integer not null,
   VERSION integer not null,
   SCALE_UID nvarchar(255) not null unique,
   NAME nvarchar(255) not null,
   UNAVAILABLE bit,
   primary key (ID)
)
create table GB_GRADING_SCALE_PERCENTS_T (
   GRADING_SCALE_ID bigint not null,
   PERCENT float,
   LETTER_GRADE nvarchar(255) not null,
   primary key (GRADING_SCALE_ID, LETTER_GRADE)
)
alter table GB_GRADE_MAP_T add column (GB_GRADING_SCALE_T bigint);
create index FK5D3F0C955A72817B on GB_GRADING_SCALE_GRADES_T (GRADING_SCALE_ID)
alter table GB_GRADING_SCALE_GRADES_T add constraint FK5D3F0C955A72817B foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T (ID)

create index FKC98BE4675A72817 on GB_GRADING_SCALE_PERCENTS_T (GRADING_SCALE_ID)
alter table GB_GRADING_SCALE_PERCENTS_T add constraint FKC98BE4675A72817B foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T (ID)

-- Add indexes for improved performance and reduced locking.
create index GB_GRADABLE_OBJ_ASN_IDX on GB_GRADABLE_OBJECT_T (OBJECT_TYPE_ID, GRADEBOOK_ID, NAME, REMOVED)
create index GB_GRADE_RECORD_O_T_IDX on GB_GRADE_RECORD_T (OBJECT_TYPE_ID)

-- This may have already been defined via the 2.1.1 upgrade.
create index GB_GRADE_RECORD_STUDENT_ID_IDX on GB_GRADE_RECORD_T (STUDENT_ID)
;
