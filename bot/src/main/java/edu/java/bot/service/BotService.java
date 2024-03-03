package edu.java.bot.service;

import edu.java.bot.exception.WrongRequestParametersException;
import edu.java.bot.request.LinkUpdateRequest;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class BotService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BotService.class);

    public void updateLink(LinkUpdateRequest request) {
        if (Objects.equals(request.getUrl(), "")) {
            throw new WrongRequestParametersException("link cannot be empty");
        }
        LOGGER.debug("request processed: " + request);
    }
}
