package com.example.ddadang.domain.glucose.score;

import java.io.IOException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

/**
 * 단위 테스트에서도 운영과 같은 glucose-analysis.yml 값을 사용한다.
 */
final class TestGlucoseProperties {

    static final GlucoseAnalysisProperties PROPERTIES = load();

    private TestGlucoseProperties() {
    }

    private static GlucoseAnalysisProperties load() {
        try {
            StandardEnvironment environment = new StandardEnvironment();
            new YamlPropertySourceLoader()
                .load("glucose-analysis", new ClassPathResource("glucose-analysis.yml"))
                .forEach(environment.getPropertySources()::addLast);
            return new Binder(ConfigurationPropertySources.get(environment))
                .bind("glucose.analysis", GlucoseAnalysisProperties.class)
                .get();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
