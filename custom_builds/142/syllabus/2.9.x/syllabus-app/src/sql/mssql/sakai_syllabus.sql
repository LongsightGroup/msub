create table SAKAI_SYLLABUS_DATA (
   id bigint not null identity,
   lockId integer not null,
   asset nvarchar(max),
   position integer not null,
   title nvarchar(max),
   xview varchar(16),
   status varchar(64),
   emailNotification varchar(128),
   surrogateKey bigint,
   primary key (id)
)
create table SAKAI_SYLLABUS_ITEM (
   id bigint not null identity,
   lockId integer not null,
   userId varchar(36) not null,
   contextId varchar(36) not null,
   redirectURL nvarchar(max),
   primary key (id),
   unique (userId, contextId)
)
create table SAKAI_SYLLABUS_ATTACH (
   syllabusAttachId bigint not null identity,
   lockId integer not null,
   attachmentId text not null,
   syllabusAttachName text not null,
   syllabusAttachSize nvarchar(max),
   syllabusAttachType nvarchar(max),
   createdBy nvarchar(max),
   syllabusAttachUrl text not null,
   lastModifiedBy nvarchar(max),
   syllabusId bigint,
   primary key (syllabusAttachId)
)
create index syllabus_position on SAKAI_SYLLABUS_DATA (position)
create index FK3BC123AA4FDCE067 on SAKAI_SYLLABUS_DATA (surrogateKey)
alter table SAKAI_SYLLABUS_DATA add constraint FK3BC123AA4FDCE067 foreign key (surrogateKey) references SAKAI_SYLLABUS_ITEM (id)
create index syllabus_userId on SAKAI_SYLLABUS_ITEM (userId)
create index syllabus_contextId on SAKAI_SYLLABUS_ITEM (contextId)
create index FK4BF41E45A09831E0 on SAKAI_SYLLABUS_ATTACH (syllabusId)
alter table SAKAI_SYLLABUS_ATTACH add constraint FK4BF41E45A09831E0 foreign key (syllabusId) references SAKAI_SYLLABUS_DATA (id)
;