--liquibase formatted sql

--changeset scrapper:1
CREATE TABLE links
(
    link_id     BIGSERIAL PRIMARY KEY,
    uri         TEXT UNIQUE NOT NULL,
    last_update TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

--changeset scrapper:2
CREATE TABLE chats
(
    chat_id BIGINT PRIMARY KEY
);

--changeset scrapper:3
CREATE TABLE chats_and_links
(
    chat_id BIGINT,
    link_id BIGINT,
    PRIMARY KEY (chat_id, link_id),
    alias   TEXT NOT NULL,
    FOREIGN KEY (chat_id) REFERENCES chats (chat_id),
    FOREIGN KEY (link_id) REFERENCES links (link_id)
);

--changeset scrapper:4
ALTER TABLE chats_and_links
    ADD CONSTRAINT chat_id_alias_unique UNIQUE (chat_id, alias);


