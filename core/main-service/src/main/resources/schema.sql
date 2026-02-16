CREATE TABLE IF NOT EXISTS categories(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) UNIQUE
);

CREATE TABLE IF NOT EXISTS compilations(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(50) UNIQUE,
    pinned boolean
);

CREATE TABLE IF NOT EXISTS compilation_events(
    compilation_id BIGINT,
    event_id BIGINT,
    FOREIGN KEY(compilation_id) REFERENCES compilations(id) ON DELETE CASCADE,
    CONSTRAINT unique_keys_compilation_events UNIQUE(compilation_id, event_id)
);

CREATE TABLE IF NOT EXISTS comments(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    author_id BIGINT,
    event_id BIGINT,
    created timestamp,
    text VARCHAR(5000),
    CONSTRAINT unique_keys_author_event UNIQUE(author_id, event_id)
);