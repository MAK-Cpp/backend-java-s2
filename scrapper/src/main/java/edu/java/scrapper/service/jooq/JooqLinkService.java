package edu.java.scrapper.service.jooq;

import edu.java.dto.exception.AliasAlreadyTakenException;
import edu.java.dto.exception.DTOException;
import edu.java.dto.exception.LinkAlreadyTrackedException;
import edu.java.dto.exception.LinkNotFoundException;
import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.NonExistentLinkAliasException;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinkResponse;
import edu.java.dto.response.ListUserLinkResponse;
import edu.java.dto.response.UserLinkResponse;
import edu.java.scrapper.domain.jooq.Tables;
import edu.java.scrapper.domain.jooq.tables.records.LinksRecord;
import edu.java.scrapper.service.AbstractService;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.validator.LinkValidator;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Record4;
import org.jooq.RecordMapper;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.springframework.transaction.annotation.Transactional;

public class JooqLinkService extends AbstractService implements LinkService {
    private final DSLContext dslContext;
    private final List<LinkValidator> linkValidators;
    /*package-private*/ static final RecordMapper<? super LinksRecord, LinkResponse> LINK_RECORD_RESPONSE_MAPPER
        = linksRecord -> new LinkResponse(
        linksRecord.getLinkId(),
        URI.create(linksRecord.getUri()),
        localToOffset(linksRecord.getLastUpdate())
    );
    /*package-private*/ static final RecordMapper<? super Record3<Long, String, LocalDateTime>, LinkResponse>
        LINK_RESPONSE_MAPPER =
        record3 -> new LinkResponse(
            record3.get(Tables.LINKS.LINK_ID),
            URI.create(record3.get(Tables.LINKS.URI)),
            localToOffset(record3.get(Tables.LINKS.LAST_UPDATE))
        );
    private static final RecordMapper<? super Record4<Long, String, LocalDateTime, String>, UserLinkResponse>
        USER_LINK_RESPONSE_MAPPER = record4 -> new UserLinkResponse(
        new LinkResponse(
            record4.component1(),
            URI.create(record4.component2()),
            localToOffset(record4.component3())
        ),
        record4.component4()
    );
    private static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();

    private static OffsetDateTime localToOffset(LocalDateTime date) {
        return OffsetDateTime.of(date, ZONE_OFFSET);
    }

    public JooqLinkService(DSLContext dslContext, List<LinkValidator> linkValidators) {
        this.dslContext = dslContext;
        this.linkValidators = linkValidators;
    }

    private void validateChatId(Long chatId) throws DTOException {
        validateId(chatId);
        boolean chatNotExists = dslContext.selectFrom(Tables.CHATS)
            .where(Tables.CHATS.CHAT_ID.eq(chatId))
            .fetch()
            .isEmpty();
        if (chatNotExists) {
            throw new NonExistentChatException(String.format(NON_EXISTING_CHAT_EXCEPTION_FORMAT, chatId));
        }
    }

    @Override
    public ListLinkResponse getAllLinks() throws DTOException {
        final LinkResponse[] responses = dslContext.selectFrom(Tables.LINKS)
            .fetch()
            .map(LINK_RECORD_RESPONSE_MAPPER)
            .toArray(LinkResponse[]::new);
        return new ListLinkResponse(responses, responses.length);
    }

    @Override
    public ListUserLinkResponse getAllLinks(Long chatId) throws DTOException {
        validateChatId(chatId);
        final UserLinkResponse[] responses = dslContext.select(
                Tables.LINKS.LINK_ID,
                Tables.LINKS.URI,
                Tables.LINKS.LAST_UPDATE,
                Tables.CHATS_AND_LINKS.ALIAS
            )
            .from(Tables.CHATS_AND_LINKS)
            .join(Tables.LINKS).on(Tables.LINKS.LINK_ID.eq(Tables.CHATS_AND_LINKS.LINK_ID))
            .where(Tables.CHATS_AND_LINKS.CHAT_ID.eq(chatId))
            .fetch()
            .map(USER_LINK_RESPONSE_MAPPER)
            .toArray(UserLinkResponse[]::new);
        return new ListUserLinkResponse(responses, responses.length);
    }

    @Override
    public UserLinkResponse getLink(Long chatId, String alias) throws DTOException {
        validateChatId(chatId);
        final UserLinkResponse[] responses = dslContext.select(
                Tables.LINKS.LINK_ID,
                Tables.LINKS.URI,
                Tables.LINKS.LAST_UPDATE,
                Tables.CHATS_AND_LINKS.ALIAS
            )
            .from(Tables.CHATS_AND_LINKS)
            .join(Tables.LINKS).on(Tables.LINKS.LINK_ID.eq(Tables.CHATS_AND_LINKS.LINK_ID))
            .where(Tables.CHATS_AND_LINKS.CHAT_ID.eq(chatId))
            .and(Tables.CHATS_AND_LINKS.ALIAS.eq(alias))
            .fetch()
            .map(USER_LINK_RESPONSE_MAPPER)
            .toArray(UserLinkResponse[]::new);
        if (responses.length == 0) {
            throw new
                NonExistentLinkAliasException(String.format(NON_EXISTENT_LINK_ALIAS_EXCEPTION_FORMAT, alias, chatId));
        }
        return responses[0];
    }

    @Override
    @Transactional
    public UserLinkResponse addLink(Long chatId, String link, String alias) throws DTOException {
        validateChatId(chatId);
        final URI uri = validateLink(link, linkValidators);
        final LinkResponse linkResponse = dslContext
            .insertInto(Tables.LINKS, Tables.LINKS.URI)
            .values(uri.toString())
            .onConflict(Tables.LINKS.URI)
            .doUpdate()
            .set(Tables.LINKS.URI, DSL.excluded(Tables.LINKS.URI))
            .returningResult(Tables.LINKS.LINK_ID, Tables.LINKS.URI, Tables.LINKS.LAST_UPDATE)
            .fetch()
            .map(LINK_RESPONSE_MAPPER)
            .getFirst();
        try {
            dslContext.insertInto(Tables.CHATS_AND_LINKS)
                .values(chatId, linkResponse.getId(), alias)
                .execute();
        } catch (DataAccessException e) {
            final String exceptionMessage = e.getMessage();
            if (exceptionMessage.contains("chats_and_links_pkey")) {
                throw new LinkAlreadyTrackedException(
                    String.format(LINK_ALREADY_TRACKED_EXCEPTION_FORMAT, linkResponse.getId(), chatId)
                );
            } else if (exceptionMessage.contains("chat_id_alias_unique")) {
                throw new AliasAlreadyTakenException(
                    String.format(ALIAS_ALREADY_TAKEN_EXCEPTION_FORMAT, alias, chatId)
                );
            } else {
                throw e;
            }
        }
        return new UserLinkResponse(linkResponse, alias);
    }

    @Override
    @Transactional
    public UserLinkResponse removeLink(Long chatId, String alias) throws DTOException {
        validateChatId(chatId);
        final List<Long> linkIds = dslContext
            .deleteFrom(Tables.CHATS_AND_LINKS)
            .where(Tables.CHATS_AND_LINKS.CHAT_ID.eq(chatId))
            .and(Tables.CHATS_AND_LINKS.ALIAS.eq(alias))
            .returningResult(Tables.CHATS_AND_LINKS.LINK_ID)
            .fetch()
            .map(Record1::component1);
        if (linkIds.isEmpty()) {
            throw new LinkNotFoundException(String.format(
                NON_EXISTENT_LINK_ALIAS_EXCEPTION_FORMAT,
                alias,
                chatId
            ));
        }
        final LinkResponse linkResponse =
            dslContext.select(Tables.LINKS.LINK_ID, Tables.LINKS.URI, Tables.LINKS.LAST_UPDATE)
                .from(Tables.LINKS)
                .where(Tables.LINKS.LINK_ID.eq(linkIds.getFirst()))
                .fetch()
                .map(LINK_RESPONSE_MAPPER)
                .getFirst();
        return new UserLinkResponse(linkResponse, alias);
    }
}
