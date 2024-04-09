package edu.java.scrapper.service;

import edu.java.dto.exception.DTOException;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinkResponse;

public interface LinkService {
    ListLinkResponse getAllLinks() throws DTOException;

    ListLinkResponse getAllLinks(Long chatId) throws DTOException;

    LinkResponse addLink(Long chatId, String link) throws DTOException;

    LinkResponse removeLink(Long chatId, String link) throws DTOException;


}
