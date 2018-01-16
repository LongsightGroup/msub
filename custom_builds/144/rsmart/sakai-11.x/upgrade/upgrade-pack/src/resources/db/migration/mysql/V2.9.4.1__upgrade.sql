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

-- CLE-11099
CREATE TABLE if not exists scormcloud_config (
id VARCHAR(128) NOT NULL,
isMasterConfig bit,
context VARCHAR(255),
appId VARCHAR(255),
secretKey VARCHAR(255),
serviceUrl VARCHAR(255),
PRIMARY KEY (id)
);

CREATE TABLE if not exists scormcloud_package (
id VARCHAR(128) NOT NULL,
title VARCHAR(255) NOT NULL,
ownerId VARCHAR(255) NOT NULL,
locationId VARCHAR(255) NOT NULL,
context VARCHAR(255) NOT NULL,
contributesToAssignmentGrade bit NOT NULL,
allowLaunchOutsideAssignment bit NOT NULL,
dateCreated DATETIME,
scormCloudId VARCHAR(255) NOT NULL,
PRIMARY KEY (id)
);

CREATE TABLE if not exists scormcloud_registration (
id VARCHAR(128) NOT NULL,
ownerId VARCHAR(255) NOT NULL,
userName VARCHAR(255),
userDisplayName VARCHAR(255),
locationId VARCHAR(255),
context VARCHAR(255),
assignmentId VARCHAR(255),
assignmentKey VARCHAR(255),
assignmentName VARCHAR(255),
contributesToAssignmentGrade bit NOT NULL,
numberOfContributingResources INT NOT NULL,
dateCreated DATETIME,
scormCloudId VARCHAR(255) NOT NULL,
packageId VARCHAR(255) NOT NULL,
packageTitle VARCHAR(255) NOT NULL,
complete VARCHAR(255),
success VARCHAR(255),
score VARCHAR(255),
totalTime VARCHAR(255),
PRIMARY KEY (id)
);
-- End CLE-11099
