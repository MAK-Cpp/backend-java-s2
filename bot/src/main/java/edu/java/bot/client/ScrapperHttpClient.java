package edu.java.bot.client;

import edu.java.dto.exception.ServiceException;
import edu.java.dto.response.ChatResponse;
import edu.java.dto.response.ListUserLinkResponse;
import edu.java.dto.response.UserLinkResponse;

public interface ScrapperHttpClient {
    void registerChat(long id) throws ServiceException;

    void deleteChat(long id) throws ServiceException;

    ChatResponse getChat(long id) throws ServiceException;

    ListUserLinkResponse getAllLinks(long id) throws ServiceException;

    UserLinkResponse getLinkByChatIdAndAlias(long chatId, String alias) throws ServiceException;

    UserLinkResponse addLinkToTracking(long id, String uri, String alias) throws ServiceException;

    UserLinkResponse removeLinkFromTracking(long id, String alias) throws ServiceException;
}
