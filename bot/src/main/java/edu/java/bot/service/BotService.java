package edu.java.bot.service;

import java.util.Objects;
import edu.java.dto.request.LinkUpdateRequest;
import edu.java.exception.WrongParametersException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class BotService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BotService.class);

    public void updateLink(LinkUpdateRequest request) {
        if (Objects.equals(request.getUrl(), "")) {
            throw new WrongParametersException("link cannot be empty");
        } else if (request.getId() < 0) {
            throw new WrongParametersException("id cannot be negate");
        }
        LOGGER.debug("request processed: " + request);
    }
}
