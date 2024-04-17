package edu.java.scrapper.service.jdbc;

import edu.java.dto.exception.AliasAlreadyTakenException;
import edu.java.dto.exception.DTOException;
import edu.java.dto.exception.InvalidLinkException;
import edu.java.dto.exception.LinkAlreadyTrackedException;
import edu.java.dto.exception.LinkNotFoundException;
import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.NonExistentLinkAliasException;
import edu.java.dto.exception.UnsupportedLinkException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinkResponse;
import edu.java.dto.response.ListUserLinkResponse;
import edu.java.dto.response.UserLinkResponse;
import edu.java.scrapper.repository.jdbc.JdbcChatRepository;
import edu.java.scrapper.repository.jdbc.JdbcChatsAndLinksRepository;
import edu.java.scrapper.repository.jdbc.JdbcLinkRepository;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.validator.LinkValidator;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JdbcLinkService implements LinkService {
    private static final String NEGATE_ID_EXCEPTION_MESSAGE = "id cannot be negate";
    private static final String NON_EXISTENT_CHAT_EXCEPTION_FORMAT = "there is no chat with id=%d";
    private static final String NON_EXISTENT_LINK_ALIAS_EXCEPTION_FORMAT = "there is no link with alias %s in chat %d";
    private static final String INVALID_LINK_EXCEPTION_FORMAT = "string %s is not a valid link";
    private static final String UNSUPPORTED_LINK_EXCEPTION_FORMAT = "Link %s is not supported for tracking";

    private final JdbcChatRepository chatRepository;
    private final JdbcLinkRepository linkRepository;
    private final JdbcChatsAndLinksRepository chatsAndLinksRepository;
    private final List<LinkValidator> linkValidators;

    @Autowired
    public JdbcLinkService(
        JdbcChatRepository chatRepository,
        JdbcLinkRepository linkRepository,
        JdbcChatsAndLinksRepository chatsAndLinksRepository,
        List<LinkValidator> linkValidators
    ) {
        this.chatRepository = chatRepository;
        this.linkRepository = linkRepository;
        this.chatsAndLinksRepository = chatsAndLinksRepository;
        this.linkValidators = linkValidators;
    }

    private void validateChatId(Long chatId) throws DTOException {
        if (chatId < 0) {
            throw new WrongParametersException(NEGATE_ID_EXCEPTION_MESSAGE);
        } else if (!chatRepository.exists(chatId)) {
            throw new NonExistentChatException(String.format(NON_EXISTENT_CHAT_EXCEPTION_FORMAT, chatId));
        }
    }

    private URI validateLink(String link) throws DTOException {
        final URI result;
        try {
            result = new URI(link);
        } catch (URISyntaxException e) {
            throw new InvalidLinkException(String.format(INVALID_LINK_EXCEPTION_FORMAT, link), e);
        }
        for (LinkValidator linkValidator : linkValidators) {
            if (linkValidator.isValid(link)) {
                return result;
            }
        }
        throw new UnsupportedLinkException(String.format(UNSUPPORTED_LINK_EXCEPTION_FORMAT, link));
    }

    @Override
    public ListLinkResponse getAllLinks() throws DTOException {
        final LinkResponse[] links = linkRepository.findAll().toArray(LinkResponse[]::new);
        return new ListLinkResponse(links, links.length);
    }

    @Override
    public ListUserLinkResponse getAllLinks(Long chatId) throws DTOException {
        validateChatId(chatId);
        final UserLinkResponse[] links = chatsAndLinksRepository.findAllLinks(chatId).toArray(UserLinkResponse[]::new);
        return new ListUserLinkResponse(links, links.length);
    }

    @Override
    public UserLinkResponse getLink(Long chatId, String alias) throws DTOException {
        validateChatId(chatId);
        final List<UserLinkResponse> response = chatsAndLinksRepository.getLink(chatId, alias);
        if (response.isEmpty()) {
            throw new
                NonExistentLinkAliasException(String.format(NON_EXISTENT_LINK_ALIAS_EXCEPTION_FORMAT, alias, chatId));
        }
        return response.getFirst();
    }

    @Override
    public UserLinkResponse addLink(Long chatId, String link, String alias) throws DTOException {
        validateChatId(chatId);
        final URI uri = validateLink(link);
        final LinkResponse response = linkRepository.add(uri);
        try {
            chatsAndLinksRepository.add(chatId, response.getId(), alias);
        } catch (DuplicateKeyException e) {
            if (e.getMessage().contains("chats_and_links_pkey")) {
                throw new LinkAlreadyTrackedException(
                    "There is already a link with id " + response.getId() + " in chat " + chatId
                );
            } else if (e.getMessage().contains("chat_id_alias_unique")) {
                throw new AliasAlreadyTakenException(
                    "Alias " + alias + " was already taken in chat " + chatId
                );
            } else {
                throw e;
            }
        }
        return new UserLinkResponse(response, alias);
    }

    @Override
    public UserLinkResponse removeLink(Long chatId, String alias) throws DTOException {
        validateChatId(chatId);
        final List<Long> linkIds = chatsAndLinksRepository.remove(chatId, alias);
        if (linkIds.isEmpty()) {
            throw new LinkNotFoundException(String.format(NON_EXISTENT_LINK_ALIAS_EXCEPTION_FORMAT, alias, chatId));
        }
        final List<LinkResponse> linkResponse = linkRepository.findAll(linkIds.getFirst());
        return new UserLinkResponse(linkResponse.getFirst(), alias);
    }
}
