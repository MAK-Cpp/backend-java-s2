package edu.java.scrapper.service;

import edu.java.dto.exception.AliasAlreadyTakenException;
import edu.java.dto.exception.LinkAlreadyTrackedException;
import edu.java.dto.exception.LinkNotFoundException;
import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.NonExistentLinkAliasException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinkResponse;
import edu.java.dto.response.ListUserLinkResponse;
import edu.java.dto.response.UserLinkResponse;
import org.junit.jupiter.params.provider.Arguments;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class LinkServiceTest extends ServiceTest {
    public LinkServiceTest(LinkUpdater linkUpdater, LinkService linkService, ChatService chatService) {
        super(linkUpdater, linkService, chatService);
    }

    // GET ALL LINKS

    protected void testGetAllLinksFunction() {
        final Set<LinkResponse> allLinks =
            fillDB().stream().map(x -> x.getValue().getLink()).collect(Collectors.toSet());
        final ListLinkResponse listLinkResponse = linkService.getAllLinks();
        assertThat(listLinkResponse.getSize()).isEqualTo(allLinks.size());
        for (LinkResponse link : listLinkResponse.getLinks()) {
            assertThat(allLinks).contains(link);
        }
    }

    // GET ALL LINKS BY CHAT ID

    private void testSuccessGetAllLinks(Long chatId) {
        final ListUserLinkResponse userLinkResponse = linkService.getAllLinks(chatId);
        assertThat(userLinkResponse.getSize()).isEqualTo(DATABASE_START_VALUES.get(chatId).size());
        for (UserLinkResponse userLink : userLinkResponse.getLinks()) {
            assertThat(DATABASE_START_VALUES.get(chatId))
                .contains(new LinkRecord(userLink.getLink().getUri().toString(), userLink.getAlias()));
        }
    }

    private void testFailGetAllLinks(Long chatId, Class<? extends Exception> exceptionClass, String exceptionMessage) {
        Exception exception = assertThrows(exceptionClass, () -> linkService.getAllLinks(chatId));
        assertThat(exception.getMessage()).isEqualTo(exceptionMessage);
    }

    protected void testGetAllLinksFunction(Long chatId) {
        fillDB();
        if (chatId < 0) {
            testFailGetAllLinks(
                chatId,
                WrongParametersException.class,
                String.format(AbstractService.NEGATE_ID_EXCEPTION_FORMAT, chatId)
            );
        } else if (!DATABASE_START_VALUES.containsKey(chatId)) {
            testFailGetAllLinks(
                chatId,
                NonExistentChatException.class,
                String.format(AbstractService.NON_EXISTING_CHAT_EXCEPTION_FORMAT, chatId)
            );
        } else {
            testSuccessGetAllLinks(chatId);
        }
    }

    // GET LINK

    private static Arguments userAndLink(Long chatId, String uri, String alias) {
        return Arguments.arguments(chatId, new LinkRecord(uri, alias));
    }

    protected static Stream<Arguments> usersAndLinks() {
        return Stream.of(
            userAndLink(1L, "https://github.com/MAK-Cpp/backend-java-s2/pull/7", "hw5_pull_request"),
            userAndLink(
                2L,
                "https://stackoverflow.com/questions/218384/what-is-a-nullpointerexception-and-how-do-i-fix-it",
                "repeated alias"
            ),
            userAndLink(-3L, "https://github.com/MAK-Cpp/backend-java-s2/pull/7", "hw5_pull_request"),
            userAndLink(
                3L,
                "https://stackoverflow.com/questions/218384/what-is-a-nullpointerexception-and-how-do-i-fix-it",
                "repeated alias"
            ),
            userAndLink(
                100L,
                "https://stackoverflow.com/questions/218384/what-is-a-nullpointerexception-and-how-do-i-fix-it",
                "repeated alias"
            )
        );
    }

    private void testSuccessGetLink(Long chatId, String uri, String alias) {
        final UserLinkResponse userLink = linkService.getLink(chatId, alias);
        assertThat(userLink.getLink().getUri().toString()).isEqualTo(uri);
        assertThat(userLink.getAlias()).isEqualTo(alias);
    }

    private void testFailGetLink(
        Long chatId,
        String alias,
        Class<? extends Exception> exceptionClass,
        String exceptionMessage
    ) {
        Exception exception = assertThrows(exceptionClass, () -> linkService.getLink(chatId, alias));
        assertThat(exception.getMessage()).isEqualTo(exceptionMessage);
    }

    protected void testGetLinkFunction(Long chatId, LinkRecord linkRecord) {
        fillDB();
        final String link = linkRecord.link();
        final String alias = linkRecord.alias();
        if (chatId < 0) {
            testFailGetLink(
                chatId,
                alias,
                WrongParametersException.class,
                String.format(AbstractService.NEGATE_ID_EXCEPTION_FORMAT, chatId)
            );
        } else if (!DATABASE_START_VALUES.containsKey(chatId)) {
            testFailGetLink(
                chatId,
                alias,
                NonExistentChatException.class,
                String.format(AbstractService.NON_EXISTING_CHAT_EXCEPTION_FORMAT, chatId)
            );
        } else if (DATABASE_START_VALUES.get(chatId).stream().noneMatch(x -> x.alias().equals(alias))) {
            testFailGetLink(
                chatId,
                alias,
                NonExistentLinkAliasException.class,
                String.format(AbstractService.NON_EXISTENT_LINK_ALIAS_EXCEPTION_FORMAT, alias, chatId)
            );
        } else {
            testSuccessGetLink(chatId, link, alias);
        }
    }

    // ADD LINK

    private void testSuccessAddLink(Long chatId, String uri, String alias) {
        final UserLinkResponse userLink = linkService.addLink(chatId, uri, alias);
        assertThat(userLink.getLink().getUri().toString()).isEqualTo(uri);
        assertThat(userLink.getAlias()).isEqualTo(alias);
        testSuccessGetLink(chatId, uri, alias);
    }

    private void testFailAddLink(
        Long chatId,
        String uri,
        String alias,
        Class<? extends Exception> exceptionClass,
        String exceptionMessage
    ) {
        Exception exception = assertThrows(exceptionClass, () -> linkService.addLink(chatId, uri, alias));
        assertThat(exception.getMessage()).isEqualTo(exceptionMessage);
    }

    protected void testAddLinkFunction(Long chatId, LinkRecord linkRecord) {
        final List<Map.Entry<Long, UserLinkResponse>> responses = fillDB();
        final String link = linkRecord.link();
        final String alias = linkRecord.alias();
        if (chatId < 0) {
            testFailAddLink(
                chatId,
                link,
                alias,
                WrongParametersException.class,
                String.format(AbstractService.NEGATE_ID_EXCEPTION_FORMAT, chatId)
            );
        } else if (!DATABASE_START_VALUES.containsKey(chatId)) {
            testFailAddLink(
                chatId,
                link,
                alias,
                NonExistentChatException.class,
                String.format(AbstractService.NON_EXISTING_CHAT_EXCEPTION_FORMAT, chatId)
            );
        } else if (DATABASE_START_VALUES.get(chatId).stream().anyMatch(x -> x.link().equals(link))) {
            final Long linkId = responses.stream()
                .filter(x -> x.getKey().equals(chatId) && x.getValue().getAlias().equals(alias))
                .toList()
                .getFirst()
                .getValue()
                .getLink().getId();
            testFailAddLink(
                chatId,
                link,
                alias,
                LinkAlreadyTrackedException.class,
                String.format(AbstractService.LINK_ALREADY_TRACKED_EXCEPTION_FORMAT, linkId, chatId)
            );
        } else if (DATABASE_START_VALUES.get(chatId).stream().anyMatch(x -> x.alias().equals(alias))) {
            testFailAddLink(
                chatId,
                link,
                alias,
                AliasAlreadyTakenException.class,
                String.format(AbstractService.ALIAS_ALREADY_TAKEN_EXCEPTION_FORMAT, alias, chatId)
            );
        } else {
            testSuccessAddLink(chatId, link, alias);
        }
    }

    // REMOVE LINK

    private void testSuccessRemoveLink(Long chatId, String uri, String alias) {
        final UserLinkResponse userLink = linkService.removeLink(chatId, alias);
        assertThat(userLink.getLink().getUri().toString()).isEqualTo(uri);
        assertThat(userLink.getAlias()).isEqualTo(alias);
        assertThat(Arrays.stream(linkService.getAllLinks(chatId).getLinks())).noneMatch(x ->
            x.getAlias().equals(alias) && x.getLink().getUri().toString().equals(uri));
    }

    private void testFailRemoveLink(
        Long chatId,
        String alias,
        Class<? extends Exception> exceptionClass,
        String exceptionMessage
    ) {
        Exception exception = assertThrows(exceptionClass, () -> linkService.removeLink(chatId, alias));
        assertThat(exception.getMessage()).isEqualTo(exceptionMessage);
    }

    protected void testRemoveLinkFunction(Long chatId, LinkRecord linkRecord) {
        fillDB();
        final String link = linkRecord.link();
        final String alias = linkRecord.alias();
        if (chatId < 0) {
            testFailRemoveLink(
                chatId,
                alias,
                WrongParametersException.class,
                String.format(AbstractService.NEGATE_ID_EXCEPTION_FORMAT, chatId)
            );
        } else if (!DATABASE_START_VALUES.containsKey(chatId)) {
            testFailRemoveLink(
                chatId,
                alias,
                NonExistentChatException.class,
                String.format(AbstractService.NON_EXISTING_CHAT_EXCEPTION_FORMAT, chatId)
            );
        } else if (DATABASE_START_VALUES.get(chatId).stream().noneMatch(x -> x.alias().equals(alias))) {
            testFailRemoveLink(
                chatId,
                alias,
                LinkNotFoundException.class,
                String.format(AbstractService.NON_EXISTENT_LINK_ALIAS_EXCEPTION_FORMAT, alias, chatId)
            );
        } else {
            testSuccessRemoveLink(chatId, link, alias);
        }
    }
}
