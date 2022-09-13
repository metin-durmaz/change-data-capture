\connect mydb
CREATE TABLE ddl_history (
  	id BIGSERIAL PRIMARY KEY NOT NULL,
	event_type VARCHAR(155) NOT NULL,
	table_name VARCHAR(255),
	event_owner VARCHAR(155)
);

CREATE OR REPLACE FUNCTION log_ddl()
	RETURNS event_trigger AS $$
DECLARE
	audit_query TEXT;
	r RECORD;
BEGIN
	IF tg_tag NOT LIKE 'DROP%' AND tg_tag NOT LIKE '%TABLE' AND tg_tag <> 'COMMENT' THEN
		r := pg_event_trigger_ddl_commands();
		insert into ddl_history(event_type,table_name,event_owner) values (tg_tag, r.object_identity, current_user);
	END IF;

	IF tg_tag <> 'DROP TABLE' THEN
		
		FOR r IN SELECT * FROM pg_event_trigger_ddl_commands()
			LOOP
				IF r.object_type = 'table' THEN
					insert into ddl_history(event_type,table_name,event_owner) values (tg_tag, r.object_identity, current_user);
					exit;
			END IF;
		END LOOP;
	END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION log_ddl_drop()
	RETURNS event_trigger AS $$
DECLARE
	audit_query TEXT;
	r RECORD;
BEGIN
	IF tg_tag LIKE 'DROP%' AND tg_tag NOT LIKE '%TABLE' THEN
		FOR r IN SELECT * FROM pg_event_trigger_dropped_objects()
		LOOP
			insert into ddl_history(event_type,table_name,event_owner) values (tg_tag, r.object_identity, current_user);
			exit;
		END LOOP;
	END IF;

	IF tg_tag = 'DROP TABLE' THEN
		FOR r IN SELECT * FROM pg_event_trigger_dropped_objects()
		LOOP
			IF r.object_type='table' THEN
				insert into ddl_history(event_type,table_name,event_owner) values (tg_tag, r.object_identity, current_user);
				exit;
			END IF;
		END LOOP;
	END IF;
END;
$$ LANGUAGE plpgsql;

CREATE EVENT TRIGGER log_ddl_info ON ddl_command_end EXECUTE PROCEDURE log_ddl();

CREATE EVENT TRIGGER log_ddl_drop_info ON sql_drop EXECUTE PROCEDURE log_ddl_drop();






------------------------------------------------------------------------------------------------*

-- alter table tableName replica identity FULL;
	