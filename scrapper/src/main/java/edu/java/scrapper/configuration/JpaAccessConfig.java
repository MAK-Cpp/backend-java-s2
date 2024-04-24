package edu.java.scrapper.configuration;

import edu.java.scrapper.service.ChatService;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.service.LinkUpdater;
import edu.java.scrapper.service.jpa.JpaChatService;
import edu.java.scrapper.service.jpa.JpaLinkService;
import edu.java.scrapper.service.jpa.JpaLinkUpdater;
import edu.java.scrapper.validator.LinkValidator;
import jakarta.persistence.EntityManager;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jpa")
public class JpaAccessConfig {
    @Bean
    @Autowired
    public ChatService chatService(EntityManager entityManager) {
        return new JpaChatService(entityManager);
    }

    @Bean
    @Autowired
    public LinkService linkService(EntityManager entityManager, List<LinkValidator> linkValidators) {
        return new JpaLinkService(entityManager, linkValidators);
    }

    @Bean
    @Autowired
    public LinkUpdater linkUpdater(EntityManager entityManager) {
        return new JpaLinkUpdater(entityManager);
    }

    @Bean(name = "entityManagerFactory")
    @Autowired
    public LocalSessionFactoryBean localSessionFactoryBean(DataSource dataSource) {
        final LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan("edu.java.scrapper.repository.jpa");
        return sessionFactory;
    }
}
