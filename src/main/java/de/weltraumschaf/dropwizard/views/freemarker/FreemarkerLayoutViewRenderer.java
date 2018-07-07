package de.weltraumschaf.dropwizard.views.freemarker;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import de.weltraumschaf.dropwizard.views.LayoutView;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.dropwizard.views.View;
import io.dropwizard.views.ViewRenderException;
import io.dropwizard.views.ViewRenderer;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This renderer allows to render a two step layout.
 */
public final class FreemarkerLayoutViewRenderer implements ViewRenderer {
    private static final String CONFIGURATION_KEY = "freemarker";
    private static final Pattern FILE_PATTERN = Pattern.compile("\\.ftl[hx]?$");
    private static final int CONCURRENCY_LEVEL = 32;
    private final ConfigurationLoader loader = new ConfigurationLoader();
    private final LoadingCache<Class<?>, Configuration> configurations = CacheBuilder.newBuilder()
        .concurrencyLevel(CONCURRENCY_LEVEL)
        .build(loader);

    @Override
    public boolean isRenderable(final View view) {
        return FILE_PATTERN.matcher(view.getTemplateName()).find();
    }

    @Override
    public String getConfigurationKey() {
        return CONFIGURATION_KEY;
    }

    @Override
    public void render(final View view, final Locale locale, final OutputStream output) throws IOException {
        if (view instanceof LayoutView) {
            renderLayout((LayoutView) view, locale, output);
        } else {
            renderView(view, locale, output);
        }
    }

    @Override
    public void configure(final Map<String, String> options) {
        loader.setBaseConfiguration(options);
    }

    ConfigurationLoader getLoader() {
        return loader;
    }

    private void renderLayout(final LayoutView layout, final Locale locale, final OutputStream output) throws ViewRenderException {
        final View contentView = layout.getContentView();
        final ByteArrayOutputStream content = new ByteArrayOutputStream();
        renderView(contentView, locale, content);

        try {
            layout.setContent(content.toString(determineEncoding(contentView, locale).name()));
        } catch (final UnsupportedEncodingException e) {
            throw new ViewRenderException(e);
        }

        renderView(layout, locale, output);
    }

    private void renderView(final View view, final Locale locale, final OutputStream output) throws ViewRenderException {
        final Configuration configuration = configurations.getUnchecked(view.getClass());
        final Charset charset = determineEncoding(view, locale);

        try {
            final Template template = configuration.getTemplate(view.getTemplateName(), locale, charset.name());
            template.process(view, new OutputStreamWriter(output, template.getEncoding()));
        } catch (final IOException | TemplateException e) {
            throw new ViewRenderException(e);
        }
    }

    private Charset determineEncoding(final View view, final Locale locale) {
        final Configuration configuration = configurations.getUnchecked(view.getClass());
        return view.getCharset()
            .orElseGet(() -> Charset.forName(configuration.getEncoding(locale)));
    }
}
