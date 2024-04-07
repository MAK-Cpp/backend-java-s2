package edu.java.scrapper.service;

import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinkResponse;
import edu.java.exception.LinkNotFoundException;
import edu.java.exception.NonExistentChatException;
import edu.java.exception.WrongParametersException;

public interface LinkService {
    ListLinkResponse getAllLinks(Long chatId) throws NonExistentChatException, WrongParametersException;

    LinkResponse addLink(Long chatId, String link) throws WrongParametersException, NonExistentChatException;

    LinkResponse removeLink(Long chatId, String link)
        throws WrongParametersException, NonExistentChatException, LinkNotFoundException;
}
