package edu.java.scrapper.configuration;

import edu.java.scrapper.service.ChatService;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.service.LinkUpdater;
import edu.java.scrapper.service.jpa.JpaChatService;
import edu.java.scrapper.service.jpa.JpaLinkService;
import edu.java.scrapper.service.jpa.JpaLinkUpdater;
import edu.java.scrapper.validator.LinkValidator;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jpa")
public class JpaAccessConfig {
    @Bean
    @Autowired
    public ChatService chatService(SessionFactory sessionFactory) {
        return new JpaChatService(sessionFactory);
    }

    @Bean
    @Autowired
    public LinkService linkService(SessionFactory sessionFactory, List<LinkValidator> linkValidators) {
        return new JpaLinkService(sessionFactory, linkValidators);
    }

    @Bean
    @Autowired
    public LinkUpdater linkUpdater(SessionFactory sessionFactory) {
        return new JpaLinkUpdater(sessionFactory);
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
