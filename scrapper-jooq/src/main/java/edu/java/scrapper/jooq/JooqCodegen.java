package edu.java.scrapper.jooq;

import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Property;
import org.jooq.meta.jaxb.Target;

public final class JooqCodegen {
    private JooqCodegen() {
    }

    @SuppressWarnings("checkstyle:UncommentedMain")
    public static void main(String[] args) throws Exception {
        final Configuration configuration = new org.jooq.meta.jaxb.Configuration()
            .withGenerator(new Generator()
                .withDatabase(new Database()
                    .withName("org.jooq.meta.extensions.liquibase.LiquibaseDatabase")
                    .withProperties(
                        new Property()
                            .withKey("scripts")
                            .withValue("master.xml"),
                        new Property()
                            .withKey("rootPath")
                            .withValue("scrapper/src/main/resources/migrations")
                    )
                )
                .withGenerate(new Generate()
                    .withGeneratedAnnotation(true)
                    .withGeneratedAnnotationDate(false)
                    .withNullableAnnotation(true)
                    .withNullableAnnotationType("org.jetbrains.annotations.Nullable")
                    .withNonnullAnnotation(true)
                    .withNonnullAnnotationType("org.jetbrains.annotations.NotNull")
                    .withJpaAnnotations(false)
                    .withValidationAnnotations(true)
                    .withSpringAnnotations(true)
                    .withConstructorPropertiesAnnotation(true)
                    .withConstructorPropertiesAnnotationOnPojos(true)
                    .withConstructorPropertiesAnnotationOnRecords(true)
                    .withFluentSetters(false)
                    .withDaos(false)
                    .withPojos(true)
                )
                .withTarget(new Target()
                    .withPackageName("edu.java.scrapper.domain.jooq")
                    .withDirectory("scrapper/src/main/java")
                )
            );

        GenerationTool.generate(configuration);
    }
}
