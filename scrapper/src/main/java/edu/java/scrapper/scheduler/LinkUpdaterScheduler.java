package edu.java.scrapper.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
public class LinkUpdaterScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkUpdaterScheduler.class);

    @Scheduled(fixedDelayString = "${app.scheduler.interval}")
    void update() {
        LOGGER.debug("scheduled update call");
    }
}
