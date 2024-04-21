package edu.java.scrapper.service.jooq;

import edu.java.scrapper.domain.jooq.Tables;
import edu.java.scrapper.service.AbstractService;
import edu.java.scrapper.service.LinkUpdater;
import java.time.OffsetDateTime;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class JooqLinkUpdater extends AbstractService implements LinkUpdater {
    private final DSLContext dslContext;

    @Autowired
    public JooqLinkUpdater(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public void updateLink(Long linkId) {
        dslContext.update(Tables.LINKS)
            .set(Tables.LINKS.LAST_UPDATE, OffsetDateTime.now().toLocalDateTime())
            .where(Tables.LINKS.LINK_ID.eq(linkId))
            .execute();
    }
}
