create table GB_GRADABLE_OBJECT_T (
   ID bigint not null identity,
   OBJECT_TYPE_ID integer not null,
   VERSION integer not null,
   GRADEBOOK_ID bigint not null,
   NAME nvarchar(255) not null,
   REMOVED bit,
   POINTS_POSSIBLE float,
   DUE_DATE datetime,
   NOT_COUNTED bit,
   EXTERNALLY_MAINTAINED bit,
   EXTERNAL_STUDENT_LINK nvarchar(255),
   EXTERNAL_INSTRUCTOR_LINK nvarchar(255),
   EXTERNAL_ID nvarchar(255),
   EXTERNAL_APP_NAME nvarchar(255),
   primary key (ID)
)
create table GB_GRADEBOOK_T (
   ID bigint not null identity,
   VERSION integer not null,
   GRADEBOOK_UID nvarchar(255) not null unique,
   NAME nvarchar(255) not null,
   SELECTED_GRADE_MAPPING_ID bigint,
   ASSIGNMENTS_DISPLAYED bit not null,
   COURSE_GRADE_DISPLAYED bit not null,
   ALL_ASSIGNMENTS_ENTERED bit not null,
   LOCKED bit not null,
   primary key (ID)
)
create table GB_GRADE_MAP_T (
   ID bigint not null identity,
   OBJECT_TYPE_ID integer not null,
   VERSION integer not null,
   GRADEBOOK_ID bigint not null,
   GB_GRADING_SCALE_T bigint,
   primary key (ID)
)
create table GB_GRADE_RECORD_T (
   ID bigint not null identity,
   OBJECT_TYPE_ID integer not null,
   VERSION integer not null,
   GRADABLE_OBJECT_ID bigint not null,
   STUDENT_ID nvarchar(255) not null,
   GRADER_ID nvarchar(255) not null,
   DATE_RECORDED datetime not null,
   POINTS_EARNED float,
   ENTERED_GRADE nvarchar(255),
   SORT_GRADE float,
   primary key (ID),
   unique (GRADABLE_OBJECT_ID, STUDENT_ID)
)
create table GB_GRADE_TO_PERCENT_MAPPING_T (
   GRADE_MAP_ID bigint not null,
   [PERCENT] float,
   LETTER_GRADE nvarchar(255) not null,
   primary key (GRADE_MAP_ID, LETTER_GRADE)
)
create table GB_GRADING_EVENT_T (
   ID bigint not null identity,
   GRADABLE_OBJECT_ID bigint not null,
   GRADER_ID nvarchar(255) not null,
   STUDENT_ID nvarchar(255) not null,
   DATE_GRADED datetime not null,
   GRADE nvarchar(255),
   primary key (ID)
)
create table GB_GRADING_SCALE_GRADES_T (
        GRADING_SCALE_ID bigint not null,
        LETTER_GRADE nvarchar(255),
        GRADE_IDX integer not null,
        primary key (GRADING_SCALE_ID, GRADE_IDX)
)
create table GB_GRADING_SCALE_PERCENTS_T (
        GRADING_SCALE_ID bigint not null,
        PERCENT float,
        LETTER_GRADE nvarchar(255) not null,
        primary key (GRADING_SCALE_ID, LETTER_GRADE)
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
create table GB_PROPERTY_T (
        ID bigint not null identity,
        VERSION integer not null,
        NAME nvarchar(255) not null unique,
        VALUE nvarchar(255),
        primary key (ID)
)
create index FK759996A7325D7986 on GB_GRADABLE_OBJECT_T (GRADEBOOK_ID)
alter table GB_GRADABLE_OBJECT_T add constraint FK759996A7325D7986 foreign key (GRADEBOOK_ID) references GB_GRADEBOOK_T (ID)

create index FK7C870191552B7E63 on GB_GRADEBOOK_T (SELECTED_GRADE_MAPPING_ID)
alter table GB_GRADEBOOK_T add constraint FK7C870191552B7E63 foreign key (SELECTED_GRADE_MAPPING_ID) references GB_GRADE_MAP_T (ID)

create index FKADE11225325D7986 on GB_GRADE_MAP_T (GRADEBOOK_ID)
alter table GB_GRADE_MAP_T add constraint FKADE11225325D7986 foreign key (GRADEBOOK_ID) references GB_GRADEBOOK_T (ID)

create index FKADE11225181E947A on GB_GRADE_MAP_T (GB_GRADING_SCALE_T)
alter table GB_GRADE_MAP_T add constraint FKADE11225181E947A foreign key (GB_GRADING_SCALE_T) references GB_GRADING_SCALE_T(ID)

create index GB_GRADE_RECORD_STUDENT_ID_IDX on GB_GRADE_RECORD_T (STUDENT_ID)

create index FK46ACF7526F98CFF on GB_GRADE_RECORD_T (GRADABLE_OBJECT_ID)
alter table GB_GRADE_RECORD_T add constraint FK46ACF7526F98CFF foreign key (GRADABLE_OBJECT_ID) references GB_GRADABLE_OBJECT_T (ID)

create index FKCDEA021162B659F1 on GB_GRADE_TO_PERCENT_MAPPING_T (GRADE_MAP_ID)
alter table GB_GRADE_TO_PERCENT_MAPPING_T add constraint FKCDEA021162B659F1 foreign key (GRADE_MAP_ID) references GB_GRADE_MAP_T (ID)

create index FK4C9D99E06F98CFF on GB_GRADING_EVENT_T (GRADABLE_OBJECT_ID)
alter table GB_GRADING_EVENT_T add constraint FK4C9D99E06F98CFF foreign key (GRADABLE_OBJECT_ID) references GB_GRADABLE_OBJECT_T (ID)

create index FK5D3F0C95605CD0C5 on GB_GRADING_SCALE_GRADES_T (GRADING_SCALE_ID)
alter table GB_GRADING_SCALE_GRADES_T add constraint FK5D3F0C95605CD0C5 foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T (ID)

create index FKC98BE467605CD0C5 on GB_GRADING_SCALE_PERCENTS_T (GRADING_SCALE_ID)
alter table GB_GRADING_SCALE_PERCENTS_T add constraint FKC98BE467605CD0C5 foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T(ID)

create index GB_GRADABLE_OBJ_ASN_IDX on GB_GRADABLE_OBJECT_T (OBJECT_TYPE_ID, GRADEBOOK_ID, NAME, REMOVED)
create index GB_GRADE_RECORD_O_T_IDX on GB_GRADE_RECORD_T (OBJECT_TYPE_ID)
;
