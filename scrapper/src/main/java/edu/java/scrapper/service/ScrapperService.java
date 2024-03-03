package edu.java.scrapper.service;

import edu.java.scrapper.exception.LinkNotFoundException;
import edu.java.scrapper.exception.NonExistentChatException;
import edu.java.scrapper.exception.WrongRequestParametersException;
import edu.java.scrapper.response.LinkResponse;
import java.net.URI;
import java.net.URISyntaxException;
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

    private static void checkId(int id) {
        if (id < 0) {
            throw new WrongRequestParametersException(NEGATE_ID_EXCEPTION_MESSAGE);
        } else if (id > DELETE_ME) { // TODO: replace to check if chat exist
            throw new NonExistentChatException(String.format(NON_EXISTENT_CHAT_EXCEPTION_FORMAT, id));
        }
    }

    public void registerChat(int id) {
        checkId(id);
        LOGGER.debug(String.format(SUCCESS_CHAT_REGISTER_FORMAT, id));
    }

    public void deleteChat(int id) {
        checkId(id);
        LOGGER.debug(String.format(SUCCESS_CHAT_DELETED_FORMAT, id));
    }

    public LinkResponse[] getAllLinks(int id) {
        checkId(id);
        LinkResponse[] result;
        try {
            result = new LinkResponse[] {
                new LinkResponse(1, new URI("https://github.com/MAK-Cpp/backend-java-s2")),
                new LinkResponse(2, new URI("https://stackoverflow.com/questions/11828270/how-do-i-exit-vim"))
            };
        } catch (URISyntaxException ignore) {
            result = new LinkResponse[] {};
        }
        return result;
    }

    public LinkResponse addLink(int id, String uri) {
        checkId(id);
        try {
            URI parsedUri = new URI(uri);
            // TODO: add link
            return new LinkResponse(1, parsedUri);
        } catch (URISyntaxException e) {
            throw new WrongRequestParametersException(e.getMessage(), e);
        }
    }

    public LinkResponse removeLink(int id, String uri) {
        checkId(id);
        try {
            URI parsedUri = new URI(uri);
            // TODO: check is there a link
            boolean isUriExist = true;
            if (!isUriExist) {
                throw new LinkNotFoundException("there is no link " + uri);
            }
            // TODO: remove link
            return new LinkResponse(1, parsedUri);
        } catch (URISyntaxException e) {
            throw new WrongRequestParametersException(e.getMessage(), e);
        }
    }
}
