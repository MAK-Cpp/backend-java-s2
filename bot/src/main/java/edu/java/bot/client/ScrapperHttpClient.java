package edu.java.bot.client;

import edu.java.dto.exception.DTOException;
import edu.java.dto.response.ChatResponse;
import edu.java.dto.response.ListUserLinkResponse;
import edu.java.dto.response.UserLinkResponse;

public interface ScrapperHttpClient {
    void registerChat(long id) throws DTOException;

    void deleteChat(long id) throws DTOException;

    ChatResponse getChat(long id) throws DTOException;

    ListUserLinkResponse getAllLinks(long id) throws DTOException;

    UserLinkResponse getLinkByChatIdAndAlias(long chatId, String alias) throws DTOException;

    UserLinkResponse addLinkToTracking(long id, String uri, String alias) throws DTOException;

    UserLinkResponse removeLinkFromTracking(long id, String alias) throws DTOException;
}
