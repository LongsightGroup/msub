create table lti_binding (
id NUMBER(11,0) NOT NULL,
tool_id NUMBER(11,0) DEFAULT NULL,
SITE_ID VARCHAR(99) DEFAULT NULL,
settings CLOB,
created_at TIMESTAMP NOT NULL,
updated_at TIMESTAMP NOT NULL,
PRIMARY KEY (id)
);

create table lti_deploy (
id NUMBER(11,0) NOT NULL,
reg_state NUMBER(4,0) DEFAULT '0',
title VARCHAR(255) NULL,
pagetitle VARCHAR(255) NULL,
description CLOB,
status NUMBER(4,0) DEFAULT '0',
visible NUMBER(4,0) DEFAULT '0',
sendname NUMBER(4,0) DEFAULT '0',
sendemailaddr NUMBER(4,0) DEFAULT '0',
allowoutcomes NUMBER(4,0) DEFAULT '0',
allowroster NUMBER(4,0) DEFAULT '0',
allowsettings NUMBER(4,0) DEFAULT '0',
allowlori NUMBER(4,0) DEFAULT '0',
reg_launch CLOB,
reg_key VARCHAR(255) NULL,
reg_password VARCHAR(255) NULL,
consumerkey VARCHAR(255) NULL,
secret VARCHAR(255) NULL,
reg_profile CLOB,
settings CLOB,
created_at TIMESTAMP NOT NULL,
updated_at TIMESTAMP NOT NULL,
PRIMARY KEY (id)
);

alter table lti_content add resource_handler clob;
alter table lti_content add settings_ext clob;

alter table lti_tools add version NUMBER(4,0) default '0';
alter table lti_tools add resource_handler clob;
alter table lti_tools add deployment_id NUMBER(11,0) default null;
alter table lti_tools add settings clob;
alter table lti_tools add parameter clob;
alter table lti_tools add enabled_capability clob;