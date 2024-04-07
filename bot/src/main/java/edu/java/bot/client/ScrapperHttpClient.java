package edu.java.bot.client;

import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinkResponse;

public interface ScrapperHttpClient {
    void registerChat(long id);

    void deleteChat(long id);

    ListLinkResponse getAllLinks(long id);

    LinkResponse addLinkToTracking(long id, String uri);

    LinkResponse removeLinkFromTracking(long id, String uri);
}
