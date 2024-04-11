package edu.java.scrapper.service;

import edu.java.dto.exception.DTOException;
import edu.java.dto.response.ListLinkResponse;
import edu.java.dto.response.ListUserLinkResponse;
import edu.java.dto.response.UserLinkResponse;

public interface LinkService {
    ListLinkResponse getAllLinks() throws DTOException;

    ListUserLinkResponse getAllLinks(Long chatId) throws DTOException;

    UserLinkResponse addLink(Long chatId, String link, String alias) throws DTOException;

    UserLinkResponse removeLink(Long chatId, String alias) throws DTOException;
}
