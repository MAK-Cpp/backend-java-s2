package edu.java.scrapper.service;

import edu.java.dto.exception.DTOException;

public interface ChatService {
    void registerChat(Long chatId) throws DTOException;

    void deleteChat(Long chatId) throws DTOException;
}
