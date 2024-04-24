package edu.java.scrapper.service;

import edu.java.dto.response.UserLinkResponse;
import edu.java.scrapper.IntegrationTest;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.LongStream;

@Slf4j
public abstract class ServiceTest extends IntegrationTest {
    protected final LinkUpdater linkUpdater;
    protected final LinkService linkService;
    protected final ChatService chatService;
    protected static final Map<Long, List<LinkRecord>> DATABASE_START_VALUES = Map.of(
        1L, List.of(
            new LinkRecord("https://github.com/MAK-Cpp/backend-java-s2/pulls", "backend_java_s2_pulls"),
            new LinkRecord("https://github.com/MAK-Cpp/backend-java-s2/pull/7", "hw5_pull_request"),
            new LinkRecord("https://github.com/MAK-Cpp/Devtools-LabWork-2/issues", "repeated alias")
        ),
        2L, List.of(
            new LinkRecord("https://github.com/MAK-Cpp/backend-java-s2/pulls", "backend_java_s2_pulls"),
            new LinkRecord(
                "https://stackoverflow.com/questions/218384/what-is-a-nullpointerexception-and-how-do-i-fix-it",
                "repeated alias"
            )
        ),
        3L, List.of()
    );

    public ServiceTest(LinkUpdater linkUpdater, LinkService linkService, ChatService chatService) {
        this.linkUpdater = linkUpdater;
        this.linkService = linkService;
        this.chatService = chatService;
    }

    protected List<Map.Entry<Long, UserLinkResponse>> fillDB() {
        final List<Map.Entry<Long, UserLinkResponse>> addedLinks = new ArrayList<>();
        for (Map.Entry<Long, List<LinkRecord>> entry : DATABASE_START_VALUES.entrySet()) {
            final Long chatId = entry.getKey();
            chatService.registerChat(chatId);
            for (LinkRecord linkRecord : entry.getValue()) {
                addedLinks.add(Map.entry(chatId, linkService.addLink(chatId, linkRecord.link, linkRecord.alias)));
            }
        }
        return addedLinks;
    }

    protected static LongStream chatIdsStream() {
        return LongStream.of(
            3L, 0L, -1L, 5L, 1L, 2L, 8L, -100L
        );
    }

    protected record LinkRecord(String link, String alias) {
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LinkRecord that = (LinkRecord) o;
            return Objects.equals(link, that.link) && Objects.equals(alias, that.alias);
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(link);
            result = 31 * result + Objects.hashCode(alias);
            return result;
        }
    }
}
