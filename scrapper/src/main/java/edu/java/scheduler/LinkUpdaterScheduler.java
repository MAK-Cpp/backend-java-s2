package edu.java.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@SuppressWarnings("checkstyle:LineLength")
@EnableScheduling
@Component
public class LinkUpdaterScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkUpdaterScheduler.class);

    @Scheduled(fixedDelayString = "#{ beanFactory.getBean(T(edu.java.configuration.ApplicationConfig)).scheduler.interval.getSeconds() * 1000 }")
    void update() {
        LOGGER.debug("scheduled update call");
    }
}
