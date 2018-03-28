drop procedure if exists ExecuteQueryIfNotExists;
drop procedure if exists ChangeColumnIfExists;
drop procedure if exists DropColumnIfExists;
drop procedure if exists ModifyColumnIfExists;
drop procedure if exists AddConstraintUnlessExists;
drop procedure if exists RemoveToolInfo;

delimiter //

create procedure ExecuteQueryIfNotExists(
  IN dbName tinytext,
  IN tableName tinytext,
  IN whereClause text,
  IN updateStmt text)
begin
  DECLARE num INT;
  SET @num=0;
  SET @query = concat('select count(*) into @num from ', dbName,'.',tableName, ' where ', whereClause);
  prepare query from @query;
  execute query;
	DEALLOCATE PREPARE query;

  IF (@num=0) THEN
    set @ddl=updateStmt;
    prepare stmt from @ddl;
    execute stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
end;

//

create procedure ChangeColumnIfExists(
	IN dbName tinytext,
	IN tableName tinytext,
	IN fieldName tinytext,
	IN fieldType text)
begin
	IF EXISTS (
		SELECT * FROM information_schema.COLUMNS
		WHERE column_name=fieldName
		and table_name=tableName
		and table_schema=dbName
		)
	THEN
		set @ddl=CONCAT('ALTER TABLE ',dbName,'.',tableName,
			' CHANGE ',fieldName,' ', fieldName, ' ', fieldType);
		prepare stmt from @ddl;
		execute stmt;
	END IF;
end;

//

create procedure DropColumnIfExists(
	IN dbName tinytext,
	IN tableName tinytext,
	IN fieldName tinytext)
begin
	IF EXISTS (
		SELECT * FROM information_schema.COLUMNS
		WHERE column_name=fieldName
		and table_name=tableName
		and table_schema=dbName
		)
	THEN
		set @ddl=CONCAT('ALTER TABLE ',dbName,'.',tableName,
			' DROP COLUMN ',fieldName);
		prepare stmt from @ddl;
		execute stmt;
	END IF;
end;

//

create procedure ModifyColumnIfExists(
	IN dbName tinytext,
	IN tableName tinytext,
	IN fieldName tinytext,
	IN fieldDef text)
begin
	IF EXISTS (
		SELECT * FROM information_schema.COLUMNS
		WHERE column_name=fieldName
		and table_name=tableName
		and table_schema=dbName
		)
	THEN
		set @ddl=CONCAT('ALTER TABLE ',dbName,'.',tableName,
			' MODIFY ',fieldName,' ',fieldDef);
		prepare stmt from @ddl;
		execute stmt;
	END IF;
end;

//

create procedure AddConstraintUnlessExists(
	IN dbName tinytext,
	IN tableName tinytext,
	IN refTable tinytext,
	IN constraintDef text)
begin
	IF NOT EXISTS (
		SELECT * FROM information_schema.REFERENTIAL_CONSTRAINTS
		WHERE constraint_schema=dbName
		and table_name=tableName
		and referenced_table_name=refTable
		)
	THEN
		set @ddl=CONCAT('ALTER TABLE ',dbName,'.',tableName,
			' ADD CONSTRAINT ',constraintDef);
		prepare stmt from @ddl;
		execute stmt;
	END IF;
end;

//

create procedure RemoveToolInfo(
	IN toolReg tinytext
)
BEGIN
	DROP TABLE IF EXISTS temp_page_ids;
	CREATE TABLE temp_page_ids AS SELECT page_id FROM sakai_site_tool WHERE registration = toolReg;
	DELETE FROM sakai_site_tool_property WHERE tool_id IN (SELECT tool_id FROM sakai_site_tool WHERE registration = toolReg);
	DELETE FROM sakai_site_tool WHERE registration = toolReg;
	DELETE FROM sakai_site_page_property WHERE page_id IN (SELECT page_id FROM temp_page_ids);
	DELETE FROM sakai_site_page WHERE page_id IN (SELECT page_id FROM temp_page_ids);
	DROP TABLE temp_page_ids;
end;

//

delimiter //
