drop procedure if exists DropIndexIfExists;

delimiter //

create procedure DropIndexIfExists(
	IN dbName tinytext,
	IN indexName tinytext,
	IN tableName tinytext)
begin
	IF EXISTS (
		SELECT * FROM information_schema.STATISTICS
		WHERE index_name=indexName
		and table_name=tableName
		and table_schema=dbName
		)
	THEN
		set @ddl=CONCAT('DROP INDEX ', indexName, ' ON ', dbName, '.',tableName);
		prepare stmt from @ddl;
		execute stmt;
	END IF;
end;

//