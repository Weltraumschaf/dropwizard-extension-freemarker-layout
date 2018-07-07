package de.weltraumschaf.dropwizard.views.freemarker;

import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableMap;
import de.weltraumschaf.commons.validate.Validate;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Version;
import lombok.SneakyThrows;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Cache loader implementation to create the Freemarker configurations for the view classes.
 */
final class ConfigurationLoader extends CacheLoader<Class<?>, Configuration> {
    private static final Version VERSION = Configuration.getVersion();
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private Map<String, String> baseConfiguration = ImmutableMap.of();

    @Override
    public Configuration load(final Class<?> key) throws Exception {
        Validate.notNull(key, "key");
        final Configuration configuration = new Configuration(VERSION);
        configuration.setObjectWrapper(new DefaultObjectWrapperBuilder(VERSION).build());
        configuration.loadBuiltInEncodingMap();
        configuration.setDefaultEncoding(DEFAULT_CHARSET.name());
        configuration.setClassForTemplateLoading(key, "/");
        baseConfiguration.entrySet()
            .forEach(entry -> addSetting(configuration, entry));

        return configuration;
    }

    @SneakyThrows
    private void addSetting(final Configuration configuration, Map.Entry<String, String> entry) {
        configuration.setSetting(entry.getKey(), entry.getValue());
    }

    void setBaseConfiguration(final Map<String, String> baseConfiguration) {
        Validate.notNull(baseConfiguration, "baseConfiguration");
        this.baseConfiguration = ImmutableMap.copyOf(baseConfiguration);
    }

    Map<String, String> getBaseConfiguration() {
        return baseConfiguration;
    }
}
