package edu.java.scrapper.service.jdbc;

import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinkResponse;
import edu.java.exception.DTOException;
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
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JdbcLinkService implements LinkService {
    private static final String NEGATE_ID_EXCEPTION_MESSAGE = "id cannot be negate";
    private static final String NON_EXISTENT_CHAT_EXCEPTION_FORMAT = "there is no chat with id=%d";
    private static final String NON_EXISTENT_LINK_EXCEPTION_FORMAT = "there is no link %s";
    private static final String INVALID_LINK_EXCEPTION_FORMAT = "string %s is not a valid link";
    private static final Pattern GITHUB_PATTERN
        = Pattern.compile("^(https?://)?(www\\.)?github\\.com/([\\w-]+/?)+", Pattern.CASE_INSENSITIVE);
    private static final Pattern STACKOVERFLOW_PATTERN
        = Pattern.compile("^(https?://)?(www\\.)?stackoverflow\\.com/([\\w-]+/?)+", Pattern.CASE_INSENSITIVE);

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

    private void validateChatId(Long chatId) throws DTOException {
        if (chatId < 0) {
            throw new WrongParametersException(NEGATE_ID_EXCEPTION_MESSAGE);
        } else if (!chatRepository.exists(chatId)) {
            throw new NonExistentChatException(String.format(NON_EXISTENT_CHAT_EXCEPTION_FORMAT, chatId));
        }
    }

    private URI validateLink(String link) throws DTOException {
        final boolean isGithubLink = GITHUB_PATTERN.matcher(link).matches();
        final boolean isStackoverflowLink = STACKOVERFLOW_PATTERN.matcher(link).matches();
        if (!isGithubLink && !isStackoverflowLink) {
            throw new WrongParametersException(String.format(INVALID_LINK_EXCEPTION_FORMAT, link));
        }
        try {
            return new URI(link);
        } catch (URISyntaxException e) {
            throw new WrongParametersException(String.format(INVALID_LINK_EXCEPTION_FORMAT, link), e);
        }
    }

    @Override
    public ListLinkResponse getAllLinks(Long chatId) throws DTOException {
        validateChatId(chatId);
        final LinkResponse[] links = chatsAndLinksRepository.findAll(chatId).toArray(LinkResponse[]::new);
        return new ListLinkResponse(links, links.length);
    }

    @Override
    public LinkResponse addLink(Long chatId, String link) throws DTOException {
        validateChatId(chatId);
        final URI uri = validateLink(link);
        final Long linkId = linkRepository.add(uri);
        chatsAndLinksRepository.add(chatId, linkId);
        return new LinkResponse(linkId, uri);
    }

    @Override
    public LinkResponse removeLink(Long chatId, String link) throws DTOException {
        validateChatId(chatId);
        final URI uri = validateLink(link);
        final List<LinkResponse> linksResponse = linkRepository.findAll(uri);
        if (linksResponse.isEmpty()) {
            throw new LinkNotFoundException(String.format(NON_EXISTENT_LINK_EXCEPTION_FORMAT, link));
        }
        final Long linkId = linksResponse.getFirst().getId();
        chatsAndLinksRepository.remove(chatId, linkId);
        return new LinkResponse(linkId, uri);
    }
}
