package edu.java.scrapper.repository.jdbc;

import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.UserLinkResponse;
import edu.java.scrapper.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"app.database-access-type=jdbc"})
public class JdbcChatsAndLinksTest extends IntegrationTest {
    private static Map.Entry<URI, String> link(String uri, String alias) {
        return Map.entry(URI.create(uri), alias);
    }

    private static final Comparator<UserLinkResponse> COMPARATOR = Comparator.comparingLong(a -> a.getLink().getId());
    private static final List<List<Map.Entry<URI, String>>> LINKS_IDS = List.of(
        List.of(
            link("https://google.com", "google"),
            link("https://vk.com", "vk"),
            link("https://example.com", "example"),
            link("https://github.com", "github")
        ),
        List.of(
            link("https://chat.openai.com", "chat gpt"),
            link("https://example.com", "example"),
            link("https://google.com", "google")
        ),
        List.of(
            link("https://github.com", "github"),
            link("https://store.steampowered.com", "steam")
        ),
        List.of(
            link("https://pcms.itmo.ru", "pcms itmo"),
            link("https://google.com", "google"),
            link("https://www.kgeorgiy.info", "kgeorgiy")
        )
    );
    private List<List<UserLinkResponse>> LINKS_DTOS;
    private Map<URI, Long> linksIds;
    private static final Logger log = LoggerFactory.getLogger(JdbcChatsAndLinksTest.class);
    @Autowired
    private JdbcChatsAndLinksRepository repository;
    @Autowired
    private JdbcChatRepository chatRepository;
    @Autowired
    private JdbcLinkRepository linksRepository;

    private void fillDB() throws Exception {
        linksIds = new HashMap<>();
        LINKS_DTOS = new ArrayList<>();
        for (long chatId = 0L; chatId < LINKS_IDS.size(); ++chatId) {
            chatRepository.add(chatId);
            List<UserLinkResponse> links = new ArrayList<>();
            for (Map.Entry<URI, String> linkAndAlias : LINKS_IDS.get((int) chatId)) {
                LinkResponse response = linksRepository.add(linkAndAlias.getKey());
                UserLinkResponse userResponse = new UserLinkResponse(response, linkAndAlias.getValue());
                links.add(userResponse);
                linksIds.put(linkAndAlias.getKey(), response.getId());
                repository.add(chatId, response.getId(), linkAndAlias.getValue());
            }
            links.sort(COMPARATOR);
            LINKS_DTOS.add(links);
        }
    }

    @Test
    @Transactional
    @Rollback
    void addTest() throws Exception {
        fillDB();
        Map.Entry<URI, String> uri = link("https://pcms.itmo.ru", "pcms itmo");
        LinkResponse response = linksRepository.findAll(uri.getKey()).getFirst();
        repository.add(2L, response.getId(), uri.getValue());
        List<UserLinkResponse> result = new ArrayList<>(LINKS_DTOS.get(2));
        result.add(new UserLinkResponse(response, uri.getValue()));
        assertThat(repository.findAllLinks(0L).stream().sorted(COMPARATOR).toList()).isEqualTo(LINKS_DTOS.get(0));
        assertThat(repository.findAllLinks(1L).stream().sorted(COMPARATOR).toList()).isEqualTo(LINKS_DTOS.get(1));
        assertThat(repository.findAllLinks(2L).stream().sorted(COMPARATOR).toList()).isEqualTo(result);
        assertThat(repository.findAllLinks(3L).stream().sorted(COMPARATOR).toList()).isEqualTo(LINKS_DTOS.get(3));
    }

    @Test
    @Transactional
    @Rollback
    void removeTest() throws Exception {
        fillDB();
        Map.Entry<URI, String> uri = link("https://google.com", "google");
        Long linkId = linksIds.get(uri.getKey());
        repository.remove(0L, uri.getValue());
        List<UserLinkResponse> result = new ArrayList<>();
        LINKS_DTOS.getFirst().forEach(link -> {
            if (!Objects.equals(link.getLink().getId(), linkId)) {
                result.add(link);
            }
        });
        assertThat(repository.findAllLinks(0L).stream().sorted(COMPARATOR).toList()).isEqualTo(result);
        assertThat(repository.findAllLinks(1L).stream().sorted(COMPARATOR).toList()).isEqualTo(LINKS_DTOS.get(1));
        assertThat(repository.findAllLinks(2L).stream().sorted(COMPARATOR).toList()).isEqualTo(LINKS_DTOS.get(2));
        assertThat(repository.findAllLinks(3L).stream().sorted(COMPARATOR).toList()).isEqualTo(LINKS_DTOS.get(3));
    }
}
