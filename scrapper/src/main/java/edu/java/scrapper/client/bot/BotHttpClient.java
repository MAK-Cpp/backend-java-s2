package edu.java.scrapper.client.bot;

import java.util.List;
import java.util.Map;

public interface BotHttpClient {
    void sendUpdates(Long id, String url, String description, List<Map.Entry<Long, String>> chatsAndAliases);
}
