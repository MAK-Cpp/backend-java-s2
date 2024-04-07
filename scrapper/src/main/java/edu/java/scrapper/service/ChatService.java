package edu.java.scrapper.service;

import edu.java.exception.NonExistentChatException;
import edu.java.exception.WrongParametersException;

public interface ChatService {
    void registerChat(Long chatId) throws WrongParametersException;

    void deleteChat(Long chatId) throws WrongParametersException, NonExistentChatException;
}
