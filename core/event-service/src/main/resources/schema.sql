CREATE TABLE IF NOT EXISTS events(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(120),
    annotation VARCHAR(2000),
    description VARCHAR(7000),
    category_id BIGINT,
    event_date timestamp,
    initiator_id BIGINT,
    paid boolean,
    participant_limit integer,
    request_moderation boolean,
    created_on timestamp,
    published_on timestamp,
    location_lat float,
    location_lon float,
    state VARCHAR(20)
);
