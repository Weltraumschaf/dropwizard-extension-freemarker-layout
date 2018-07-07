package de.weltraumschaf.dropwizard.views.freemarker;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link ConfigurationLoader}.
 */
class ConfigurationLoaderTest {
    private final ConfigurationLoader sut = new ConfigurationLoader();

    @Test
    void setBaseConfiguration_nullNotAllowed() {
        assertThrows(NullPointerException.class, () -> sut.setBaseConfiguration(null));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void load_nullNotAllowed() {
        assertThrows(NullPointerException.class, () -> sut.load(null));
    }

    @Test
    void load_emptyBaseConfiguration() throws Exception {
        final Configuration result = sut.load(Object.class);

        assertThat(result.getDefaultEncoding(), is("UTF-8"));
        assertThat(result.isObjectWrapperSet(), is(true));
        assertThat(result.getIncompatibleImprovements(), is(Configuration.getVersion()));
        TemplateLoader loader = result.getTemplateLoader();
        assertThat(loader, not(nullValue()));
    }

    @Test
    void load_withBaseConfiguration() throws Exception {
        final Map<String, String> baseConfiguration = new HashMap<>();
        baseConfiguration.put("localized_lookup", "yes");
        baseConfiguration.put("datetime_format", "foo");
        sut.setBaseConfiguration(baseConfiguration);

        final Configuration result = sut.load(Object.class);

        assertThat(result.getLocalizedLookup(), is(true));
        assertThat(result.getDateTimeFormat(), is("foo"));
    }
}