package edu.java.scrapper.service;

import edu.java.dto.exception.DTOException;
import edu.java.dto.exception.InvalidLinkException;
import edu.java.dto.exception.UnsupportedLinkException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.scrapper.validator.LinkValidator;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public abstract class AbstractService {
    protected static final String NEGATE_ID_EXCEPTION_FORMAT = "wrong id %d: id cannot be negate";
    protected static final String CHAT_ALREADY_EXISTS_EXCEPTION = "chat already exists";
    protected static final String SUCCESS_CHAT_REGISTER_FORMAT = "chat with id=%d registered successfully";
    protected static final String SUCCESS_CHAT_DELETED_FORMAT = "chat with id=%d deleted successfully";
    protected static final String LINK_IS_NOT_TRACKED_BY_CHAT_FORMAT = "there is no link with id %d in chat with id %d";
    protected static final String NON_EXISTING_CHAT_EXCEPTION_FORMAT = "chat with id=%d does not exist";
    protected static final String NON_EXISTENT_LINK_ALIAS_EXCEPTION_FORMAT =
        "there is no link with alias %s in chat %d";
    protected static final String INVALID_LINK_EXCEPTION_FORMAT = "string %s is not a valid link";
    protected static final String UNSUPPORTED_LINK_EXCEPTION_FORMAT = "Link %s is not supported for tracking";
    protected static final String LINK_ALREADY_TRACKED_EXCEPTION_FORMAT =
        "There is already a link with id {%d} in chat {%d}";
    protected static final String ALIAS_ALREADY_TAKEN_EXCEPTION_FORMAT = "Alias \"%s\" was already taken in chat {%d}";

    public AbstractService() {
    }

    protected static void validateId(Long id) throws DTOException {
        if (id < 0) {
            throw new WrongParametersException(String.format(NEGATE_ID_EXCEPTION_FORMAT, id));
        }
    }

    @NotNull
    protected static URI validateLink(String link, List<LinkValidator> linkValidators) {
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
}
