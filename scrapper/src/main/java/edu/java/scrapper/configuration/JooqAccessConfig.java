package edu.java.scrapper.configuration;

import edu.java.scrapper.service.ChatService;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.service.LinkUpdater;
import edu.java.scrapper.service.jooq.JooqChatService;
import edu.java.scrapper.service.jooq.JooqLinkService;
import edu.java.scrapper.service.jooq.JooqLinkUpdater;
import edu.java.scrapper.validator.LinkValidator;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.boot.autoconfigure.jooq.SpringTransactionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jooq")
public class JooqAccessConfig {
    @Bean
    @Autowired
    public ChatService chatService(DSLContext dsl) {
        return new JooqChatService(dsl);
    }

    @Bean
    @Autowired
    public LinkService linkService(DSLContext dsl, List<LinkValidator> linkValidators) {
        return new JooqLinkService(dsl, linkValidators);
    }

    @Bean
    @Autowired
    public LinkUpdater linkUpdater(DSLContext dsl) {
        return new JooqLinkUpdater(dsl);
    }

    @Bean
    @Autowired
    public DSLContext dslContext(DataSource dataSource, PlatformTransactionManager platformTransactionManager) {
        DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
        jooqConfiguration.set(SQLDialect.POSTGRES);
        jooqConfiguration.set(new DataSourceConnectionProvider(new TransactionAwareDataSourceProxy(dataSource)));
        jooqConfiguration.set(new SpringTransactionProvider(platformTransactionManager));
        jooqConfiguration.setSettings(jooqConfiguration.settings()
            .withRenderNameCase(RenderNameCase.LOWER)
            .withRenderQuotedNames(RenderQuotedNames.NEVER));

        return DSL.using(jooqConfiguration);
    }

/*    @Bean
    public DefaultConfigurationCustomizer jooqDefaultConfigurationCustomizer() {
        return c -> c.settings()
            .withRenderNameCase(RenderNameCase.LOWER)
            .withRenderQuotedNames(RenderQuotedNames.NEVER);
    }*/
}
