package edu.java.scrapper.client;

public interface BotHttpClient {
    void sendUpdates(long id, String url, String description, long... tgChatIds);
}
