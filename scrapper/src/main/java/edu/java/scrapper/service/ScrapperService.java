package edu.java.scrapper.service;

import edu.java.exception.LinkNotFoundException;
import edu.java.exception.NonExistentChatException;
import edu.java.exception.WrongParametersException;
import edu.java.dto.response.LinkResponse;
import java.net.URI;
import java.net.URISyntaxException;
import edu.java.dto.response.ListLinkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ScrapperService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScrapperService.class);
    private static final String NEGATE_ID_EXCEPTION_MESSAGE = "id cannot be negate";
    private static final String NON_EXISTENT_CHAT_EXCEPTION_FORMAT = "there is no chat with id=%d";
    private static final String SUCCESS_CHAT_REGISTER_FORMAT = "chat with id=%d registered successfully";
    private static final String SUCCESS_CHAT_DELETED_FORMAT = "chat with id=%d deleted successfully";
    private static final int DELETE_ME = 10;

    private boolean isChatExists(long id) {
        // TODO: replace to check if chat exist
        return id <= DELETE_ME;
    }

    public void registerChat(long id) throws WrongParametersException {
        if (id < 0) {
            throw new WrongParametersException(NEGATE_ID_EXCEPTION_MESSAGE);
        }
        LOGGER.debug(String.format(SUCCESS_CHAT_REGISTER_FORMAT, id));
    }

    public void deleteChat(long id) throws WrongParametersException, NonExistentChatException {
        if (id < 0) {
            throw new WrongParametersException(NEGATE_ID_EXCEPTION_MESSAGE);
        } else if (!isChatExists(id)) {
            throw new NonExistentChatException(String.format(NON_EXISTENT_CHAT_EXCEPTION_FORMAT, id));
        }
        LOGGER.debug(String.format(SUCCESS_CHAT_DELETED_FORMAT, id));
    }

    public ListLinkResponse getAllLinks(long id) throws NonExistentChatException, WrongParametersException {
        if (id < 0) {
            throw new WrongParametersException(NEGATE_ID_EXCEPTION_MESSAGE);
        } else if (!isChatExists(id)) {
            throw new NonExistentChatException(String.format(NON_EXISTENT_CHAT_EXCEPTION_FORMAT, id));
        }
        ListLinkResponse result;
        try {
            result = new ListLinkResponse(new LinkResponse[] {
                new LinkResponse(1, new URI("https://github.com/MAK-Cpp/backend-java-s2")),
                new LinkResponse(2, new URI("https://stackoverflow.com/questions/11828270/how-do-i-exit-vim"))
            }, 2);
        } catch (URISyntaxException ignore) {
            result = new ListLinkResponse(new LinkResponse[] {}, 0);
        }
        return result;
    }

    public LinkResponse addLink(long id, String uri) throws WrongParametersException, NonExistentChatException {
        if (id < 0) {
            throw new WrongParametersException(NEGATE_ID_EXCEPTION_MESSAGE);
        } else if (!isChatExists(id)) {
            throw new NonExistentChatException(String.format(NON_EXISTENT_CHAT_EXCEPTION_FORMAT, id));
        }
        try {
            URI parsedUri = new URI(uri);
            // TODO: add link
            return new LinkResponse(1, parsedUri);
        } catch (URISyntaxException e) {
            throw new WrongParametersException(e.getMessage(), e);
        }
    }

    public LinkResponse removeLink(long id, String uri)
        throws WrongParametersException, NonExistentChatException, LinkNotFoundException {
        if (id < 0) {
            throw new WrongParametersException(NEGATE_ID_EXCEPTION_MESSAGE);
        } else if (!isChatExists(id)) {
            throw new NonExistentChatException(String.format(NON_EXISTENT_CHAT_EXCEPTION_FORMAT, id));
        }
        try {
            boolean isUriExist = !uri.equals("not exists");
            if (!isUriExist) {
                throw new LinkNotFoundException("there is no link " + uri);
            }
            URI parsedUri = new URI(uri);
            // TODO: check is there a link
            // TODO: remove link
            return new LinkResponse(1, parsedUri);
        } catch (URISyntaxException e) {
            throw new WrongParametersException(e.getMessage(), e);
        }
    }
}
