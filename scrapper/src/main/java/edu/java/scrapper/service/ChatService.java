package edu.java.scrapper.service;

import edu.java.dto.exception.DTOException;
import edu.java.dto.response.ListChatResponse;

public interface ChatService {
    void registerChat(Long chatId) throws DTOException;

    void deleteChat(Long chatId) throws DTOException;

    ListChatResponse getAllChats() throws DTOException;

    ListChatResponse getAllChats(Long linkId) throws DTOException;
}
