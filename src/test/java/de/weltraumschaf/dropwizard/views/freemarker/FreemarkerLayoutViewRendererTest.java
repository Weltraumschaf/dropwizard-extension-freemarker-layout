package de.weltraumschaf.dropwizard.views.freemarker;

import de.weltraumschaf.dropwizard.views.LayoutView;
import io.dropwizard.views.View;
import io.dropwizard.views.ViewRenderException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link FreemarkerLayoutViewRenderer}.
 */
class FreemarkerLayoutViewRendererTest {

    private final FreemarkerLayoutViewRenderer sut = new FreemarkerLayoutViewRenderer();

    @Test
    void isRenderable() {
        assertThat(sut.isRenderable(new ViewFixture("foo")), is(false));
        assertThat(sut.isRenderable(new ViewFixture("foo.txt")), is(false));
        assertThat(sut.isRenderable(new ViewFixture("foo.ftlfoo")), is(false));
        assertThat(sut.isRenderable(new ViewFixture("foo.ftl.foo")), is(false));

        assertThat(sut.isRenderable(new ViewFixture("foo.ftl")), is(true));
        assertThat(sut.isRenderable(new ViewFixture("foo.ftlh")), is(true));
        assertThat(sut.isRenderable(new ViewFixture("foo.ftlx")), is(true));
    }

    @Test
    void getConfigurationKey() {
        assertThat(sut.getConfigurationKey(), is("freemarker"));
    }


    @Test
    void configure_nullNotAllowed() {
        assertThrows(NullPointerException.class, () -> sut.configure(null));
    }

    @Test
    void configure() {
        final Map<String, String> options = new HashMap<>();
        options.put("fooKey", "fooValue");
        options.put("barKey", "barValue");

        sut.configure(options);

        assertThat(sut.getLoader().getBaseConfiguration(), hasEntry("fooKey", "fooValue"));
        assertThat(sut.getLoader().getBaseConfiguration(), hasEntry("barKey", "barValue"));
    }

    @Test
    void render_templateNotFound() throws IOException {
        assertThrows(
            ViewRenderException.class,
            () -> sut.render(new ViewFixture("foobar.ftl"), Locale.ENGLISH, new ByteArrayOutputStream()));
    }

    @Test
    void render_view() throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        sut.render(new ViewFixture("view.ftl"), Locale.ENGLISH, output);

        assertThat(
            output.toString(StandardCharsets.UTF_8.name()),
            is("<p>Hello, World!</p>"));
    }

    @Test
    void render_layout() throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        sut.render(
            new LayoutView(
                "/de/weltraumschaf/dropwizard/views/freemarker/layout.ftl",
                new ViewFixture("view.ftl")),
            Locale.ENGLISH,
            output);

        assertThat(
            output.toString(StandardCharsets.UTF_8.name()),
            is("<html><body><p>Hello, World!</p></body></html>"));
    }

    public static final class ViewFixture extends View {
        ViewFixture(final String templateName) {
            super(templateName);
        }

        @SuppressWarnings("unused")
        public String getText() {
            return "Hello, World!";
        }
    }
}