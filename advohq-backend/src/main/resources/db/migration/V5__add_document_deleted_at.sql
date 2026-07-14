-- Soft-delete (Trash): a non-null deleted_at means the document is in the
-- Trash — hidden from the Library but recoverable until permanently deleted.
ALTER TABLE documents ADD COLUMN deleted_at TIMESTAMPTZ;

-- Speeds up the "active documents" and "trash" listings.
CREATE INDEX idx_documents_user_deleted ON documents (user_id, deleted_at);
