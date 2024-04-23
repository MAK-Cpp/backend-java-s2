package edu.java.scrapper.service.jooq;

import edu.java.dto.response.LinkResponse;
import edu.java.scrapper.domain.jooq.Tables;
import edu.java.scrapper.service.AbstractService;
import edu.java.scrapper.service.LinkUpdater;
import java.time.OffsetDateTime;
import org.jooq.DSLContext;

public class JooqLinkUpdater extends AbstractService implements LinkUpdater {
    private final DSLContext dslContext;

    public JooqLinkUpdater(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public LinkResponse updateLink(Long linkId) {
        return dslContext.update(Tables.LINKS)
            .set(Tables.LINKS.LAST_UPDATE, OffsetDateTime.now().toLocalDateTime())
            .where(Tables.LINKS.LINK_ID.eq(linkId))
            .returning(Tables.LINKS.LINK_ID, Tables.LINKS.URI, Tables.LINKS.LAST_UPDATE)
            .fetch()
            .map(JooqLinkService.LINK_RECORD_RESPONSE_MAPPER)
            .getFirst();
    }
}
