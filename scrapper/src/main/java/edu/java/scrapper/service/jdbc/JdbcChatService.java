package edu.java.scrapper.service.jdbc;

import edu.java.dto.exception.DTOException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.response.ChatResponse;
import edu.java.dto.response.LinkAliasResponse;
import edu.java.dto.response.ListChatResponse;
import edu.java.scrapper.repository.jdbc.JdbcChatRepository;
import edu.java.scrapper.repository.jdbc.JdbcChatsAndLinksRepository;
import edu.java.scrapper.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.SQLException;
import java.util.List;

@Service
@Slf4j
public class JdbcChatService implements ChatService {
    private static final String NEGATE_ID_EXCEPTION_MESSAGE = "id cannot be negate";
    private static final String SUCCESS_CHAT_REGISTER_FORMAT = "chat with id=%d registered successfully";
    private static final String SUCCESS_CHAT_DELETED_FORMAT = "chat with id=%d deleted successfully";
    private static final String LINK_IS_NOT_TRACKED_BY_CHAT_FORMAT = "there is no link with id %d in chat with id %d";

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
        try {
            chatRepository.add(chatId);
            log.info(String.format(SUCCESS_CHAT_REGISTER_FORMAT, chatId));
        } catch (Exception e) {
            throw new WrongParametersException("chat already exists", e);
        }

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

    @Override
    public ListChatResponse getAllChats() throws DTOException {
        final ChatResponse[] response = chatRepository.findAll().toArray(ChatResponse[]::new);
        return new ListChatResponse(response, response.length);
    }

    @Override
    public ListChatResponse getAllChats(Long linkId) throws DTOException {
        final ChatResponse[] response = chatsAndLinksRepository.findAllChats(linkId).toArray(ChatResponse[]::new);
        return new ListChatResponse(response, response.length);
    }

    @Override
    public LinkAliasResponse getLinkAlias(Long chatId, Long linkId) throws DTOException {
        List<String> aliases = chatsAndLinksRepository.getAlias(chatId, linkId);
        if (aliases.isEmpty()) {
            throw new WrongParametersException(String.format(LINK_IS_NOT_TRACKED_BY_CHAT_FORMAT, linkId, chatId));
        }
        return new LinkAliasResponse(aliases.getFirst());
    }
}
