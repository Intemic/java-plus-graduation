CREATE TABLE IF NOT EXISTS requests(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created timestamp,
    event_id BIGINT,
    requester_id BIGINT,
    status VARCHAR(20)
);
