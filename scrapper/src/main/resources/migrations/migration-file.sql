--liquibase formatted sql

--changeset scrapper:1
CREATE TABLE links
(
    link_id BIGSERIAL PRIMARY KEY,
    uri     TEXT NOT NULL
);

--changeset scrapper:2
CREATE TABLE chats
(
    chat_id  BIGINT PRIMARY KEY
);

--changeset scrapper:3
CREATE TABLE chats_and_links
(
    chain_id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT,
    link_id BIGINT,
    FOREIGN KEY (chat_id) REFERENCES chats (chat_id),
    FOREIGN KEY (link_id) REFERENCES links (link_id)
);
