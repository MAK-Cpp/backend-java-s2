package edu.java.scrapper.service;

import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinkResponse;
import edu.java.exception.DTOException;

public interface LinkService {
    ListLinkResponse getAllLinks(Long chatId) throws DTOException;

    LinkResponse addLink(Long chatId, String link) throws DTOException;

    LinkResponse removeLink(Long chatId, String link) throws DTOException;
}
