--liquibase formatted sql

--changeset scrapper:1
CREATE TABLE links
(
    link_id SERIAL PRIMARY KEY,
    uri     TEXT NOT NULL
);

--changeset scrapper:2
CREATE TABLE chats
(
    chat_id  BIGINT NOT NULL PRIMARY KEY,
    username TEXT NOT NULL
);

--changeset scrapper:3
CREATE TABLE chats_and_links
(
    chat_id BIGINT,
    link_id INTEGER,
    FOREIGN KEY (chat_id) REFERENCES chats (chat_id),
    FOREIGN KEY (link_id) REFERENCES links (link_id)
);
