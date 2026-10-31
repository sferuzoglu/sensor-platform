CREATE TABLE sensors (
    id          BIGINT        PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name        TEXT          NOT NULL UNIQUE,
    type        TEXT          NOT NULL,
    latitude    DOUBLE PRECISION   NOT NULL,
    longitude   DOUBLE PRECISION   NOT NULL,
    active      BOOLEAN   NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL
);