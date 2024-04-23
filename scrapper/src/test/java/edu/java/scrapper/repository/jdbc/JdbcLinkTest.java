package edu.java.scrapper.repository.jdbc;

import edu.java.dto.response.LinkResponse;
import edu.java.scrapper.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"app.database-access-type=jdbc"})
public class JdbcLinkTest extends IntegrationTest {
    private static final URI[] LINKS = new URI[] {
        URI.create("https://google.com"),
        URI.create("https://example.com"),
        URI.create("https://github.com"),
        URI.create("https://vk.com")
    };
    @Autowired
    private JdbcLinkRepository linkRepository;
    private List<LinkResponse> linksInRepository;

    private void fillDB() {
        linksInRepository = Arrays.stream(LINKS).map(linkRepository::add).toList();
    }

    @Test
    @Transactional
    @Rollback
    void addTest() {
        fillDB();
        final URI link = URI.create("https://chat.openai.com");
        final List<LinkResponse> result = new ArrayList<>(linksInRepository);
        result.add(linkRepository.add(link));
        assertThat(linkRepository.findAll()).isEqualTo(result);
    }

    @Test
    @Transactional
    @Rollback
    void removeTest() {
        fillDB();
        final List<LinkResponse> ans = new ArrayList<>();
        final URI toRemove = URI.create("https://github.com");
        linksInRepository.forEach(link -> {
            if (Objects.equals(link.getUri(), toRemove)) {
                linkRepository.remove(link.getId());
            } else {
                ans.add(link);
            }
        });
        assertThat(ans.size()).isEqualTo(linksInRepository.size() - 1);
        assertThat(linkRepository.findAll()).isEqualTo(ans);
    }
}
