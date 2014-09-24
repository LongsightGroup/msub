-- procdures that conditionally add columns to tables and indexes

drop procedure if exists AddColumnUnlessExists;
drop procedure if exists AddIndexUnlessExists;
drop procedure if exists UpdateColumnDataIfExists;

delimiter //

create procedure AddColumnUnlessExists(
	IN dbName tinytext,
	IN tableName tinytext,
	IN fieldName tinytext,
	IN fieldDef text)
begin
	IF NOT EXISTS (
		SELECT * FROM information_schema.COLUMNS
		WHERE column_name=fieldName
		and table_name=tableName
		and table_schema=dbName
		)
	THEN
		set @ddl=CONCAT('ALTER TABLE ',dbName,'.',tableName,
			' ADD COLUMN ',fieldName,' ',fieldDef);
		prepare stmt from @ddl;
		execute stmt;
	END IF;
end;
//

create procedure AddIndexUnlessExists(
	IN dbName tinytext,
	IN tableName tinytext,
	IN indexName tinytext,
	IN indexDef text)
begin
	IF NOT EXISTS (
		SELECT * FROM information_schema.STATISTICS
		WHERE index_name=indexName
		and table_name=tableName
		and table_schema=dbName
		)
	THEN
		set @ddl=CONCAT('ALTER TABLE ',dbName,'.',tableName,
			' ADD INDEX ',indexName,'(',indexDef,')');
		prepare stmt from @ddl;
		execute stmt;
	END IF;
end;
//

delimiter //

create procedure UpdateColumnDataIfExists(
  IN dbName tinytext,
  IN tableName tinytext,
  IN columnName tinytext,
  IN updateStmt text)
begin
  IF EXISTS (
    SELECT * FROM information_schema.COLUMNS
    WHERE column_name = columnName
    and table_name = tableName
    and table_schema = dbName
  )
  THEN
   set @ddl=updateStmt;
   prepare stmt from @ddl;
   execute stmt;
  END IF;
end;

-- end of procedure