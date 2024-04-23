package edu.java.scrapper.service;

import edu.java.dto.response.LinkResponse;

public interface LinkUpdater {
    LinkResponse updateLink(Long linkId);
}
