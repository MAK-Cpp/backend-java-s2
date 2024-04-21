package edu.java.scrapper.service.jooq;

import edu.java.dto.exception.DTOException;
import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.response.ChatResponse;
import edu.java.dto.response.LinkAliasResponse;
import edu.java.dto.response.ListChatResponse;
import edu.java.scrapper.domain.jooq.Tables;
import edu.java.scrapper.service.AbstractService;
import edu.java.scrapper.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Primary
public class JooqChatService extends AbstractService implements ChatService {
    private final DSLContext dslContext;

    @Autowired
    public JooqChatService(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public void registerChat(Long chatId) throws DTOException {
        validateId(chatId);
        try {
            dslContext.insertInto(Tables.CHATS, Tables.CHATS.CHAT_ID)
                .values(chatId)
                .execute();
            log.info(String.format(SUCCESS_CHAT_REGISTER_FORMAT, chatId));
        } catch (DataAccessException e) {
            throw new WrongParametersException(CHAT_ALREADY_EXISTS_EXCEPTION, e);
        }

    }

    @Override
    public void deleteChat(Long chatId) throws DTOException {
        validateId(chatId);
        dslContext.deleteFrom(Tables.CHATS_AND_LINKS)
            .where(Tables.CHATS_AND_LINKS.CHAT_ID.eq(chatId))
            .execute();
        dslContext.deleteFrom(Tables.CHATS)
            .where(Tables.CHATS.CHAT_ID.eq(chatId))
            .execute();
        log.info(String.format(SUCCESS_CHAT_DELETED_FORMAT, chatId));
    }

    @Override
    public ListChatResponse getAllChats() throws DTOException {
        final ChatResponse[] responses = dslContext.selectFrom(Tables.CHATS)
            .fetch()
            .stream()
            .map(chatsRecord -> new ChatResponse(chatsRecord.getChatId()))
            .toArray(ChatResponse[]::new);
        return new ListChatResponse(responses, responses.length);
    }

    @Override
    public ListChatResponse getAllChats(Long linkId) throws DTOException {
        validateId(linkId);
        final ChatResponse[] responses = dslContext.selectFrom(Tables.CHATS_AND_LINKS)
            .where(Tables.CHATS_AND_LINKS.LINK_ID.eq(linkId))
            .fetch()
            .stream()
            .map(chatsAndLinksRecord -> new ChatResponse(chatsAndLinksRecord.getChatId()))
            .toArray(ChatResponse[]::new);
        return new ListChatResponse(responses, responses.length);
    }

    @Override
    public ChatResponse getChat(Long chatId) throws DTOException {
        validateId(chatId);
        final ChatResponse[] responses = dslContext.selectFrom(Tables.CHATS)
            .where(Tables.CHATS.CHAT_ID.eq(chatId))
            .fetch()
            .stream()
            .map(chatsRecord -> new ChatResponse(chatsRecord.getChatId()))
            .toArray(ChatResponse[]::new);
        if (responses.length == 0) {
            throw new NonExistentChatException(String.format(NON_EXISTING_CHAT_EXCEPTION_FORMAT, chatId));
        }
        return responses[0];
    }

    @Override
    public LinkAliasResponse getLinkAlias(Long chatId, Long linkId) throws DTOException {
        validateId(chatId);
        validateId(linkId);
        var results = dslContext.select(Tables.CHATS_AND_LINKS.ALIAS)
            .from(Tables.CHATS_AND_LINKS)
            .where(Tables.CHATS_AND_LINKS.CHAT_ID.eq(chatId))
            .and(Tables.CHATS_AND_LINKS.LINK_ID.eq(linkId))
            .fetch();
        if (results.isEmpty()) {
            throw new WrongParametersException(String.format(LINK_IS_NOT_TRACKED_BY_CHAT_FORMAT, linkId, chatId));
        }
        return new LinkAliasResponse(results.getFirst().component1());
    }
}
