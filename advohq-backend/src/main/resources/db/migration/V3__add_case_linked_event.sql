-- Tracks the Schedule-page event (if any) mirroring this case's "next date",
-- so the Library page can update/remove that same event instead of
-- creating duplicates each time the date is changed.
ALTER TABLE cases ADD COLUMN linked_event_id UUID;
