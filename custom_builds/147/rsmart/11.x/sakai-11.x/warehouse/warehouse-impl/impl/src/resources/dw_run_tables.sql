CREATE TABLE if not exists `dw_run_info` (
  `run_id` int(11) NOT NULL,
  `start_time` datetime default NULL,
  `end_time` datetime default NULL,
  `status` int(11) default NULL,
  PRIMARY KEY  (`run_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE if not exists `dw_run_child` (
  `run_id` int(11) NOT NULL,
  `table_name` varchar(255) NOT NULL,
  `parent_task_name` varchar(255) default NULL,
  `row_count` int(11) default NULL,
  PRIMARY KEY  (`run_id`,`table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE if not exists  `dw_run_task` (
  `run_id` int(11) NOT NULL,
  `task_name` varchar(255) NOT NULL,
  `start_time` datetime default NULL,
  `end_time` datetime default NULL,
  `status` int(11) default NULL,
  `exception_info` longtext,
  PRIMARY KEY (`run_id`,`task_name`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
