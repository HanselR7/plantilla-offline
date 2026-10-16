-- Función reutilizable: asigna versión de sincronización y fecha de modificación.
-- Para sincronizar una tabla de negocio, añade este trigger en su migración:
--
--   CREATE TRIGGER trg_<tabla>_sync
--   BEFORE INSERT OR UPDATE ON <tabla>
--   FOR EACH ROW EXECUTE FUNCTION sync_touch();
--
CREATE OR REPLACE FUNCTION sync_touch() RETURNS trigger AS $$
BEGIN
    NEW.sync_version := nextval('sync_version_seq');
    NEW.updated_at   := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;