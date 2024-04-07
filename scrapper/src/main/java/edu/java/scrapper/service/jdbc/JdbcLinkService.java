package edu.java.scrapper.service.jdbc;

import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinkResponse;
import edu.java.exception.LinkNotFoundException;
import edu.java.exception.NonExistentChatException;
import edu.java.exception.WrongParametersException;
import edu.java.scrapper.repository.JdbcChatRepository;
import edu.java.scrapper.repository.JdbcChatsAndLinksRepository;
import edu.java.scrapper.repository.JdbcLinkRepository;
import edu.java.scrapper.service.LinkService;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JdbcLinkService implements LinkService {
    private static final String NEGATE_ID_EXCEPTION_MESSAGE = "id cannot be negate";
    private static final String NON_EXISTENT_CHAT_EXCEPTION_FORMAT = "there is no chat with id=%d";

    private final JdbcChatRepository chatRepository;
    private final JdbcLinkRepository linkRepository;
    private final JdbcChatsAndLinksRepository chatsAndLinksRepository;

    @Autowired
    public JdbcLinkService(
        JdbcChatRepository chatRepository,
        JdbcLinkRepository linkRepository,
        JdbcChatsAndLinksRepository chatsAndLinksRepository
    ) {
        this.chatRepository = chatRepository;
        this.linkRepository = linkRepository;
        this.chatsAndLinksRepository = chatsAndLinksRepository;
    }

    private void validateChatId(Long chatId) throws NonExistentChatException, WrongParametersException {
        if (chatId < 0) {
            throw new WrongParametersException(NEGATE_ID_EXCEPTION_MESSAGE);
        } else if (!chatRepository.exists(chatId)) {
            throw new NonExistentChatException(String.format(NON_EXISTENT_CHAT_EXCEPTION_FORMAT, chatId));
        }
    }

    @Override
    public ListLinkResponse getAllLinks(Long chatId) throws NonExistentChatException, WrongParametersException {
        validateChatId(chatId);
        LinkResponse[] links = chatsAndLinksRepository
            .findAll(chatId)
            .toArray(LinkResponse[]::new);
        return new ListLinkResponse(links, links.length);
    }

    @Override
    public LinkResponse addLink(Long chatId, String link) throws WrongParametersException, NonExistentChatException {
        validateChatId(chatId);
        try {
            final URI uri = new URI(link);
            final Long linkId = linkRepository.add(uri);
            chatsAndLinksRepository.add(chatId, linkId);
            return new LinkResponse(linkId, uri);
        } catch (URISyntaxException e) {
            throw new WrongParametersException(e.getMessage(), e);
        }
    }

    @Override
    public LinkResponse removeLink(Long chatId, String link)
        throws WrongParametersException, NonExistentChatException, LinkNotFoundException {
        validateChatId(chatId);
        try {
            URI uri = new URI(link);
            List<LinkResponse> linkDTO = linkRepository.findAll(uri);
            if (linkDTO.isEmpty()) {
                throw new LinkNotFoundException("there is no link " + link);
            }
            final Long linkId = linkDTO.getFirst().getId();
            chatsAndLinksRepository.remove(chatId, linkId);
            return new LinkResponse(linkId, uri);
        } catch (URISyntaxException e) {
            throw new WrongParametersException(e.getMessage(), e);
        }
    }
}
