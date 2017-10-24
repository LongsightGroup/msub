-- CLE-10689
create index sakai_site_prop_value on sakai_site_property (name, value(40));

-- CLE-10672
create index clog_post_siteid on clog_post (site_id);
create index clog_comment_post_id on clog_comment (post_id);

-- CLE-10648
ALTER TABLE signup_meetings MODIFY COLUMN category VARCHAR(255);

-- CLE-10579
Update SAM_ASSESSMENTGRADING_T Set HASAUTOSUBMISSIONRUN = 0 where HASAUTOSUBMISSIONRUN is null;

-- CLE-10659
update rsn_tool_final_config set prop_value = '/sakai-web-portlet-basiclti' where (tool_id = (select id from rsn_tool where tool_id = 'sakai.web.168') and (prop_key = 'portlet-app-name' or prop_key = 'portlet-context'));
