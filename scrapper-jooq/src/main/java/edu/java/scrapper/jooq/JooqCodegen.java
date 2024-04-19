package edu.java.scrapper.jooq;

import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;

public final class JooqCodegen {
    private JooqCodegen() {
    }

    @SuppressWarnings("checkstyle:UncommentedMain")
    public static void main(String[] args) throws Exception {
        final Configuration configuration = new Configuration()
            .withJdbc(new Jdbc()
                .withUrl(System.getenv("SCRAPPER_URL"))
                .withUsername(System.getenv("SCRAPPER_USERNAME"))
                .withPassword(System.getenv("SCRAPPER_PASSWORD"))
                .withDriver("org.postgresql.Driver"))
            .withGenerator(new Generator()
                .withDatabase(new Database()
                    .withName("org.jooq.meta.postgres.PostgresDatabase")
                    .withIncludes(".*")
                    .withExcludes("")
                    .withInputSchema("public"))
                .withTarget(new Target()
                    .withPackageName("edu.java.scrapper.domain.jooq")
                    .withDirectory("scrapper/src/main/java")));

        GenerationTool.generate(configuration);
    }
}
