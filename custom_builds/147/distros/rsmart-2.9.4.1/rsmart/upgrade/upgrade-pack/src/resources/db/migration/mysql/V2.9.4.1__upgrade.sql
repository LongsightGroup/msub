-- CLE-11069
call AddIndexUnlessExists(DATABASE(), 'sakora_operation', 'rdt_sts', 'received_date, status');

-- END CLE-11069
create table if not exists lti_binding (
id int(11) NOT NULL AUTO_INCREMENT,
tool_id int(11) DEFAULT NULL,
SITE_ID varchar(99) DEFAULT NULL,
settings text,
created_at datetime NOT NULL,
updated_at datetime NOT NULL,
PRIMARY KEY (id)
);

create table if not exists lti_deploy (
id int(11) NOT NULL AUTO_INCREMENT,
reg_state tinyint(4) DEFAULT '0',
title varchar(255) DEFAULT NULL,
pagetitle varchar(255) DEFAULT NULL,
description text,
status tinyint(4) DEFAULT '0',
visible tinyint(4) DEFAULT '0',
sendname tinyint(4) DEFAULT '0',
sendemailaddr tinyint(4) DEFAULT '0',
allowoutcomes tinyint(4) DEFAULT '0',
allowroster tinyint(4) DEFAULT '0',
allowsettings tinyint(4) DEFAULT '0',
allowlori tinyint(4) DEFAULT '0',
reg_launch text,
reg_key varchar(255) DEFAULT NULL,
reg_password varchar(255) DEFAULT NULL,
consumerkey varchar(255) DEFAULT NULL,
secret varchar(255) DEFAULT NULL,
reg_profile text,
settings text,
created_at datetime NOT NULL,
updated_at datetime NOT NULL,
PRIMARY KEY (id)
);

call AddColumnUnlessExists(DATABASE(), 'lti_content', 'resource_handler', 'text');
call AddColumnUnlessExists(DATABASE(), 'lti_content', 'settings_ext', 'text');

call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'version', 'tinyint(4) default 0');
call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'resource_handler', 'text');
call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'deployment_id', 'int(11) default null');
call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'settings', 'text');
call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'parameter', 'text');
call AddColumnUnlessExists(DATABASE(), 'lti_tools', 'enabled_capability', 'text');
-- CLE-11079