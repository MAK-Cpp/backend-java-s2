package edu.java.bot.client;

import edu.java.dto.response.ListUserLinkResponse;
import edu.java.dto.response.UserLinkResponse;

public interface ScrapperHttpClient {
    void registerChat(long id);

    void deleteChat(long id);

    ListUserLinkResponse getAllLinks(long id);

    UserLinkResponse addLinkToTracking(long id, String uri, String alias);

    UserLinkResponse removeLinkFromTracking(long id, String alias);
}
