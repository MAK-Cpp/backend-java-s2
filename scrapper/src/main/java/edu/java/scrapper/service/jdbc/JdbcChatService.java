package edu.java.scrapper.service.jdbc;

import edu.java.dto.exception.DTOException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.scrapper.repository.JdbcChatRepository;
import edu.java.scrapper.repository.JdbcChatsAndLinksRepository;
import edu.java.scrapper.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JdbcChatService implements ChatService {
    private static final String NEGATE_ID_EXCEPTION_MESSAGE = "id cannot be negate";
    private static final String SUCCESS_CHAT_REGISTER_FORMAT = "chat with id=%d registered successfully";
    private static final String SUCCESS_CHAT_DELETED_FORMAT = "chat with id=%d deleted successfully";

    private final JdbcChatRepository chatRepository;
    private final JdbcChatsAndLinksRepository chatsAndLinksRepository;

    @Autowired
    public JdbcChatService(JdbcChatRepository chatRepository, JdbcChatsAndLinksRepository chatsAndLinksRepository) {
        this.chatRepository = chatRepository;
        this.chatsAndLinksRepository = chatsAndLinksRepository;
    }

    @Override
    public void registerChat(Long chatId) throws DTOException {
        if (chatId < 0) {
            throw new WrongParametersException(NEGATE_ID_EXCEPTION_MESSAGE);
        }
        chatRepository.add(chatId);
        log.info(String.format(SUCCESS_CHAT_REGISTER_FORMAT, chatId));
    }

    @Override
    public void deleteChat(Long chatId) throws DTOException {
        if (chatId < 0) {
            throw new WrongParametersException(NEGATE_ID_EXCEPTION_MESSAGE);
        }
        chatsAndLinksRepository.remove(chatId);
        chatRepository.remove(chatId);
        log.info(String.format(SUCCESS_CHAT_DELETED_FORMAT, chatId));
    }
}
