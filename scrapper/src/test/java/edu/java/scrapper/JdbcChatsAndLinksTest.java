package edu.java.scrapper;

import edu.java.scrapper.dto.LinkDTO;
import edu.java.scrapper.repository.JdbcChatRepository;
import edu.java.scrapper.repository.JdbcChatsAndLinksRepository;
import edu.java.scrapper.repository.JdbcLinkRepository;
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

@SpringBootTest
public class JdbcChatsAndLinksTest extends IntegrationTest {
    private static final List<List<URI>> LINKS_IDS = List.of(
        List.of(URI.create("https://google.com"), URI.create("https://vk.com"), URI.create("https://example.com"), URI.create("https://github.com")),
        List.of(URI.create("https://chat.openai.com"), URI.create("https://example.com"), URI.create("https://google.com")),
        List.of(URI.create("https://github.com"), URI.create("https://store.steampowered.com")),
        List.of(URI.create("https://pcms.itmo.ru"), URI.create("https://google.com"), URI.create("https://www.kgeorgiy.info"))
    );
    private List<List<LinkDTO>> LINKS_DTOS;
    private Map<URI, Long> linksIds;
    private static final Logger log = LoggerFactory.getLogger(JdbcChatsAndLinksTest.class);
    @Autowired
    private JdbcChatsAndLinksRepository repository;
    @Autowired
    private JdbcChatRepository chatRepository;
    @Autowired
    private JdbcLinkRepository linksRepository;

    private void fillDB() {
        linksIds = new HashMap<>();
        LINKS_DTOS = new ArrayList<>();
        for (long chatId = 0L; chatId < LINKS_IDS.size(); ++chatId) {
            chatRepository.add(chatId);
            List<LinkDTO> links = new ArrayList<>();
            for (URI link : LINKS_IDS.get((int) chatId)) {
                Long linkId = linksRepository.add(link);
                links.add(new LinkDTO(linkId, link));
                linksIds.put(link, linkId);
                this.repository.add(chatId, linkId);
            }
            links.sort(Comparator.comparingLong(LinkDTO::linkId));
            LINKS_DTOS.add(links);
        }
    }

    @Test
    @Transactional
    @Rollback
    void addTest() {
        fillDB();
        URI uri = URI.create("https://pcms.itmo.ru");
        Long linkId = linksIds.get(uri);
        repository.add(2L, linkId);
        List<LinkDTO> result = new ArrayList<>(LINKS_DTOS.get(2));
        result.add(new LinkDTO(linkId, uri));
        assertThat(repository.findAll(0L)).isEqualTo(LINKS_DTOS.get(0));
        assertThat(repository.findAll(1L)).isEqualTo(LINKS_DTOS.get(1));
        assertThat(repository.findAll(2L)).isEqualTo(result);
        assertThat(repository.findAll(3L)).isEqualTo(LINKS_DTOS.get(3));
    }

    @Test
    @Transactional
    @Rollback
    void removeTest() {
        fillDB();
        URI uri = URI.create("https://google.com");
        Long linkId = linksIds.get(uri);
        repository.remove(0L, linkId);
        List<LinkDTO> result = new ArrayList<>();
        LINKS_DTOS.getFirst().forEach(link -> {
            if (!Objects.equals(link.linkId(), linkId)) {
                result.add(link);
            }
        });
        assertThat(repository.findAll(0L)).isEqualTo(result);
        assertThat(repository.findAll(1L)).isEqualTo(LINKS_DTOS.get(1));
        assertThat(repository.findAll(2L)).isEqualTo(LINKS_DTOS.get(2));
        assertThat(repository.findAll(3L)).isEqualTo(LINKS_DTOS.get(3));
    }
}
