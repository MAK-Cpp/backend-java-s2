package edu.java.scrapper.service;

import edu.java.dto.response.LinkResponse;
import java.time.OffsetDateTime;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class LinkUpdaterTest extends ServiceTest {
    public LinkUpdaterTest(LinkUpdater linkUpdater, LinkService linkService, ChatService chatService) {
        super(linkUpdater, linkService, chatService);
    }

    protected void testUpdateFunction(Long linkId) {
        final OffsetDateTime start = OffsetDateTime.now();
        final LinkResponse updatedLink = linkUpdater.updateLink(linkId);
        final OffsetDateTime end = OffsetDateTime.now();
        assertThat(updatedLink.getId()).isEqualTo(linkId);
        assertThat(updatedLink.getLastUpdate()).isAfter(start);
        assertThat(updatedLink.getLastUpdate()).isBefore(end);
    }
}
