package edu.java.scrapper.service.jdbc;

import edu.java.dto.exception.DTOException;
import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.response.ChatResponse;
import edu.java.dto.response.LinkAliasResponse;
import edu.java.dto.response.ListChatResponse;
import edu.java.scrapper.repository.jdbc.JdbcChatRepository;
import edu.java.scrapper.repository.jdbc.JdbcChatsAndLinksRepository;
import edu.java.scrapper.service.AbstractService;
import edu.java.scrapper.service.ChatService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcChatService extends AbstractService implements ChatService {
    private final JdbcChatRepository chatRepository;
    private final JdbcChatsAndLinksRepository chatsAndLinksRepository;

    public JdbcChatService(JdbcChatRepository chatRepository, JdbcChatsAndLinksRepository chatsAndLinksRepository) {
        this.chatRepository = chatRepository;
        this.chatsAndLinksRepository = chatsAndLinksRepository;
    }

    @Override
    public void registerChat(Long chatId) throws DTOException {
        validateId(chatId);
        try {
            chatRepository.add(chatId);
            log.info(String.format(SUCCESS_CHAT_REGISTER_FORMAT, chatId));
        } catch (Exception e) {
            throw new WrongParametersException(CHAT_ALREADY_EXISTS_EXCEPTION, e);
        }
    }

    @Override
    public void deleteChat(Long chatId) throws DTOException {
        validateId(chatId);
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
        validateId(linkId);
        final ChatResponse[] response = chatsAndLinksRepository.findAllChats(linkId).toArray(ChatResponse[]::new);
        return new ListChatResponse(response, response.length);
    }

    @Override
    public ChatResponse getChat(Long chatId) throws DTOException {
        validateId(chatId);
        List<ChatResponse> response = chatRepository.findAll(chatId);
        if (response.isEmpty()) {
            throw new NonExistentChatException(String.format(NON_EXISTING_CHAT_EXCEPTION_FORMAT, chatId));
        }
        return response.getFirst();
    }

    @Override
    public LinkAliasResponse getLinkAlias(Long chatId, Long linkId) throws DTOException {
        validateId(chatId);
        validateId(linkId);
        List<String> aliases = chatsAndLinksRepository.getAlias(chatId, linkId);
        if (aliases.isEmpty()) {
            throw new WrongParametersException(String.format(LINK_IS_NOT_TRACKED_BY_CHAT_FORMAT, linkId, chatId));
        }
        return new LinkAliasResponse(aliases.getFirst());
    }
}
