delete from rsn_tool_categories where tool_id in (select id from rsn_tool where tool_id in ('sakai.rsn.osp.iframe','sakai.iframe.rsn'));
delete from rsn_tool_final_config where tool_id in (select id from rsn_tool where tool_id in ('sakai.rsn.osp.iframe','sakai.iframe.rsn'));
delete from rsn_tool_keywords where tool_id in (select id from rsn_tool where tool_id in ('sakai.rsn.osp.iframe','sakai.iframe.rsn'));
delete from rsn_tool_mutable_config where tool_id in (select id from rsn_tool where tool_id in ('sakai.rsn.osp.iframe','sakai.iframe.rsn'));
delete from rsn_tool where tool_id in ('sakai.rsn.osp.iframe','sakai.iframe.rsn');
delete from rsn_to_tool where tool_id in ('sakai.rsn.osp.iframe','sakai.iframe.rsn');

call ExecuteQueryIfNotExists(DATABASE(), 'SAKAI_SITE_PAGE', 'page_id = "!admin-1250"', 'INSERT INTO SAKAI_SITE_PAGE VALUES("!admin-1250", "!admin", "Message Bundle Manager", "0", 17, "0")');
call ExecuteQueryIfNotExists(DATABASE(), 'SAKAI_SITE_TOOL', 'tool_id = "!admin-1250"', 'INSERT INTO SAKAI_SITE_TOOL VALUES("!admin-1250", "!admin-1250", "!admin", "sakai.message.bundle.manager", 1, "Message Bundle Manager", NULL )');
