package edu.java.scrapper.client.bot;

import edu.java.dto.request.LinkUpdateRequest;

public interface BotHttpClient {
    void sendUpdates(LinkUpdateRequest request);
}
