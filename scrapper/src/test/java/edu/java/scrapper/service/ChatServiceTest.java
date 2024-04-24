package edu.java.scrapper.service;

import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.response.ChatResponse;
import edu.java.dto.response.LinkAliasResponse;
import edu.java.dto.response.ListChatResponse;
import edu.java.dto.response.UserLinkResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class ChatServiceTest extends ServiceTest {
    public ChatServiceTest(
        LinkUpdater linkUpdater, LinkService linkService, ChatService chatService
    ) {
        super(linkUpdater, linkService, chatService);
    }

    private void assertContains(Long chatId) {
        final ChatResponse chatResponse = chatService.getChat(chatId);
        assertThat(chatResponse.getId()).isEqualTo(chatId);
    }

    private void assertDoesNotContains(Long chatId) {
        Set<Long> chatIds = Arrays.stream(chatService.getAllChats().getChats())
            .map(ChatResponse::getId)
            .collect(Collectors.toSet());
        assertThat(chatIds).doesNotContain(chatId);
    }

    // REGISTER USER

    private void testSuccessRegisterUser(Long chatId) {
        chatService.registerChat(chatId);
        assertContains(chatId);
    }

    private void testFailRegisterUser(
        Long chatId,
        Class<? extends Exception> exceptionClass,
        String exceptionMessage
    ) {
        Exception exception = assertThrows(exceptionClass, () -> chatService.registerChat(chatId));
        assertThat(exception.getMessage()).isEqualTo(exceptionMessage);
    }

    protected void testRegisterUserFunction(Long chatId) {
        fillDB();
        if (chatId < 0) {
            testFailRegisterUser(
                chatId,
                WrongParametersException.class,
                String.format(AbstractService.NEGATE_ID_EXCEPTION_FORMAT, chatId)
            );
        } else if (DATABASE_START_VALUES.containsKey(chatId)) {
            testFailRegisterUser(
                chatId,
                WrongParametersException.class,
                String.format(AbstractService.CHAT_ALREADY_EXISTS_EXCEPTION_FORMAT, chatId)
            );
        } else {
            testSuccessRegisterUser(chatId);
        }
    }

    protected static LongStream chatIdsStream() {
        return LongStream.of(
            3L, 0L, -1L, 5L, 1L, 2L, 8L, -100L
        );
    }

    // DELETE USER

    private void testSuccessDeleteUser(Long chatId) {
        chatService.deleteChat(chatId);
        assertDoesNotContains(chatId);
    }

    private void testFailDeleteUser(Long chatId, Class<? extends Exception> exceptionClass, String exceptionMessage) {
        Exception exception = assertThrows(exceptionClass, () -> chatService.deleteChat(chatId));
        assertThat(exception.getMessage()).isEqualTo(exceptionMessage);
    }

    protected void testDeleteUserFunction(Long chatId) {
        fillDB();
        if (chatId < 0) {
            testFailDeleteUser(
                chatId,
                WrongParametersException.class,
                String.format(AbstractService.NEGATE_ID_EXCEPTION_FORMAT, chatId)
            );
        } else if (!DATABASE_START_VALUES.containsKey(chatId)) {
            testFailDeleteUser(
                chatId,
                NonExistentChatException.class,
                String.format(AbstractService.NON_EXISTING_CHAT_EXCEPTION_FORMAT, chatId)
            );
        } else {
            testSuccessDeleteUser(chatId);
        }
    }

    // GET ALL CHATS

    protected void testGetAllChatsFunction() {
        fillDB();
        final ListChatResponse chats = chatService.getAllChats();
        assertThat(chats.getSize()).isEqualTo(DATABASE_START_VALUES.size());
        for (ChatResponse chat : chats.getChats()) {
            assertThat(DATABASE_START_VALUES).containsKey(chat.getId());
        }
    }

    // GET ALL CHATS BY LINK ID

    protected void testGetAllChatsFunction(Long linkId, String uri) {
        final ListChatResponse chats = chatService.getAllChats(linkId);
        for (ChatResponse chat : chats.getChats()) {
            assertThat(DATABASE_START_VALUES).containsKey(chat.getId());
            assertThat(DATABASE_START_VALUES.get(chat.getId())).map(LinkRecord::link).contains(uri);
        }
    }

    // GET CHAT

    private void testSuccessGetChat(Long chatId) {
        final ChatResponse chat = chatService.getChat(chatId);
        assertThat(chat.getId()).isEqualTo(chatId);
    }

    private void testFailGetChat(Long chatId, Class<? extends Exception> exceptionClass, String exceptionMessage) {
        Exception exception = assertThrows(exceptionClass, () -> chatService.getChat(chatId));
        assertThat(exception.getMessage()).isEqualTo(exceptionMessage);
    }

    protected void testGetChatFunction(Long chatId) {
        fillDB();
        if (chatId < 0) {
            testFailGetChat(
                chatId,
                WrongParametersException.class,
                String.format(AbstractService.NEGATE_ID_EXCEPTION_FORMAT, chatId)
            );
        } else if (!DATABASE_START_VALUES.containsKey(chatId)) {
            testFailGetChat(
                chatId,
                NonExistentChatException.class,
                String.format(AbstractService.NON_EXISTING_CHAT_EXCEPTION_FORMAT, chatId)
            );
        } else {
            testSuccessGetChat(chatId);
        }
    }

    // GET LINK ALIAS

    protected void testGetLinkAliasFunction() {
        final List<Map.Entry<Long, UserLinkResponse>> addedLinks = fillDB();
        for (Map.Entry<Long, UserLinkResponse> addedLink : addedLinks) {
            final Long chatId = addedLink.getKey();
            final Long linkId = addedLink.getValue().getLink().getId();
            final String alias = addedLink.getValue().getAlias();
            final LinkAliasResponse response = chatService.getLinkAlias(chatId, linkId);
            assertThat(response.getAlias()).isEqualTo(alias);
        }
    }
}
