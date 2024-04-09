package edu.java.scrapper.client.bot;

public interface BotHttpClient {
    void sendUpdates(Long id, String url, String description, Long... tgChatIds);
}
