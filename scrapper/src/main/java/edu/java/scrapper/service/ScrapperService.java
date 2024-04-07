package edu.java.scrapper.service;

import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinkResponse;
import edu.java.exception.LinkNotFoundException;
import edu.java.exception.NonExistentChatException;
import edu.java.exception.WrongParametersException;
import edu.java.scrapper.dto.LinkDTO;
import edu.java.scrapper.repository.JdbcChatRepository;
import edu.java.scrapper.repository.JdbcChatsAndLinksRepository;
import edu.java.scrapper.repository.JdbcLinkRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ScrapperService {
    private static final String NEGATE_ID_EXCEPTION_MESSAGE = "id cannot be negate";
    private static final String NON_EXISTENT_CHAT_EXCEPTION_FORMAT = "there is no chat with id=%d";
    private static final String SUCCESS_CHAT_REGISTER_FORMAT = "chat with id=%d registered successfully";
    private static final String SUCCESS_CHAT_DELETED_FORMAT = "chat with id=%d deleted successfully";

    private final JdbcChatRepository chatRepository;
    private final JdbcLinkRepository linkRepository;
    private final JdbcChatsAndLinksRepository chatsAndLinksRepository;

    @Autowired
    public ScrapperService(JdbcChatRepository chatRepository, JdbcLinkRepository linkRepository,
        JdbcChatsAndLinksRepository chatsAndLinksRepository
    ) {
        this.chatRepository = chatRepository;
        this.linkRepository = linkRepository;
        this.chatsAndLinksRepository = chatsAndLinksRepository;
    }

    public void registerChat(Long chatId) throws WrongParametersException {
        if (chatId < 0) {
            throw new WrongParametersException(NEGATE_ID_EXCEPTION_MESSAGE);
        }
        chatRepository.add(chatId);
        log.info(String.format(SUCCESS_CHAT_REGISTER_FORMAT, chatId));
    }

    public void deleteChat(Long chatId) throws WrongParametersException, NonExistentChatException {
        if (chatId < 0) {
            throw new WrongParametersException(NEGATE_ID_EXCEPTION_MESSAGE);
        }
        chatsAndLinksRepository.remove(chatId);
        chatRepository.remove(chatId);
        log.info(String.format(SUCCESS_CHAT_DELETED_FORMAT, chatId));
    }

    public ListLinkResponse getAllLinks(Long chatId) throws NonExistentChatException, WrongParametersException {
        if (chatId < 0) {
            throw new WrongParametersException(NEGATE_ID_EXCEPTION_MESSAGE);
        } else if (!chatRepository.exists(chatId)) {
            throw new NonExistentChatException(String.format(NON_EXISTENT_CHAT_EXCEPTION_FORMAT, chatId));
        }
        LinkResponse[] links = chatsAndLinksRepository
            .findAll(chatId)
            .stream()
            .map(linkDTO -> new LinkResponse(linkDTO.linkId(), linkDTO.uri()))
            .toArray(LinkResponse[]::new);
        return new ListLinkResponse(links, links.length);
    }

    public LinkResponse addLink(Long chatId, String link) throws WrongParametersException, NonExistentChatException {
        if (chatId < 0) {
            throw new WrongParametersException(NEGATE_ID_EXCEPTION_MESSAGE);
        } else if (!chatRepository.exists(chatId)) {
            throw new NonExistentChatException(String.format(NON_EXISTENT_CHAT_EXCEPTION_FORMAT, chatId));
        }
        try {
            final URI uri = new URI(link);
            final Long linkId = linkRepository.add(uri);
            chatsAndLinksRepository.add(chatId, linkId);
            return new LinkResponse(linkId, uri);
        } catch (URISyntaxException e) {
            throw new WrongParametersException(e.getMessage(), e);
        }
    }

    public LinkResponse removeLink(Long chatId, String link)
        throws WrongParametersException, NonExistentChatException, LinkNotFoundException {
        if (chatId < 0) {
            throw new WrongParametersException(NEGATE_ID_EXCEPTION_MESSAGE);
        } else if (!chatRepository.exists(chatId)) {
            throw new NonExistentChatException(String.format(NON_EXISTENT_CHAT_EXCEPTION_FORMAT, chatId));
        }
        try {
            URI uri = new URI(link);
            List<LinkDTO> linkDTO = linkRepository.findAll(uri);
            if (linkDTO.isEmpty()) {
                throw new LinkNotFoundException("there is no link " + link);
            }
            final Long linkId = linkDTO.getFirst().linkId();
            chatsAndLinksRepository.remove(chatId, linkId);
            return new LinkResponse(linkId, uri);
        } catch (URISyntaxException e) {
            throw new WrongParametersException(e.getMessage(), e);
        }
    }
}
