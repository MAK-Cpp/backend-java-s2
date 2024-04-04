package edu.java.scrapper;

import edu.java.scrapper.dto.LinkDTO;
import edu.java.scrapper.repository.JdbcLinkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class JdbcLinkTest extends IntegrationTest {
    private static final URI[] LINKS = new URI[] {
        URI.create("https://google.com"),
        URI.create("https://example.com"),
        URI.create("https://github.com"),
        URI.create("https://vk.com")
    };
    @Autowired
    private JdbcLinkRepository linkRepository;
    private List<LinkDTO> linksInRepository;

    private void fillDB() {
        linksInRepository = Arrays.stream(LINKS).map(link -> new LinkDTO(this.linkRepository.add(link), link)).toList();
    }

    @Test
    @Transactional
    @Rollback
    void addTest() {
        fillDB();
        final URI link = URI.create("https://chat.openai.com");
        final List<LinkDTO> result = new ArrayList<>(linksInRepository);
        result.add(new LinkDTO(linkRepository.add(link), link));
        assertThat(linkRepository.findAll()).isEqualTo(result);
    }

    @Test
    @Transactional
    @Rollback
    void removeTest() {
        fillDB();
        final List<LinkDTO> ans = new ArrayList<>();
        final URI toRemove = URI.create("https://github.com");
        linksInRepository.forEach(link -> {
            if (Objects.equals(link.uri(), toRemove)) {
                linkRepository.remove(link.linkId());
            } else {
                ans.add(link);
            }
        });
        assertThat(ans.size()).isEqualTo(linksInRepository.size() - 1);
        assertThat(linkRepository.findAll()).isEqualTo(ans);
    }
}
