CREATE TABLE device_objects (
    id SERIAL PRIMARY KEY,
    device_id UUID NOT NULL,
    category VARCHAR(64) NOT NULL,
    object_type VARCHAR(32) NOT NULL,
    filename VARCHAR(256) NOT NULL,
    object_url VARCHAR(1024) NOT NULL,
    size BIGINT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    -- additional auditing/fields as needed
    CONSTRAINT fk_device
        FOREIGN KEY(device_id)
            REFERENCES devices(device_id)
);
