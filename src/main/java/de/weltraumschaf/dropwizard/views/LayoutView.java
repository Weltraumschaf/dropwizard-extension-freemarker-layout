package de.weltraumschaf.dropwizard.views;

import de.weltraumschaf.commons.validate.Validate;
import io.dropwizard.views.View;

/**
 * Implementation of a two step layout.
 * <p>
 * A two step layout is a template which has an outer layout template and a separate inner content template.
 * The {@link io.dropwizard.views.ViewRenderer} is responsible to render them in the correct order so that
 * the rendered content of the inner template is placed into the outer layout template.
 * </p>
 * <p>Schematic this looks like:</p>
 * <pre>
 * +---------------------+
 * |     layout.ftl      |
 * |                     |
 * |  +---------------+  |
 * |  |  content.ftl  |  |
 * |  +---------------+  |
 * |                     |
 * +---------------------+
 * </pre>
 * <p>The templates may look like:</p>
 * <pre>{@code
 * <! -- layout.ftl -->
 * <html>
 * <body>
 *     <h1>Layout</h1>
 *
 *     ${content}
 *
 *     <p>Footer</p>
 * </body>
 * </html>
 * }</pre>
 * <pre>{@code
 * <! -- content.ftl -->
 * <p>Hello, World!</p>
 * }</pre>
 */
public class LayoutView extends View {
    private final View contentView;
    private String content = "";

    /**
     * Dedicated constructor.
     *
     * @param layoutTemplateName not {@code null} not empty
     * @param contentView        not {@code null}
     */
    public LayoutView(final String layoutTemplateName, final View contentView) {
        super(Validate.notEmpty(layoutTemplateName, "layoutTemplateName"));
        this.contentView = Validate.notNull(contentView, "contentView");
    }

    /**
     * Get the assigned content of {@link #getContentView() the inner content view}.
     * <p>
     * This value is initially empty until the layout is rendered. The {@link io.dropwizard.views.ViewRenderer} is responsible for rendering the inner content and {@link #setContent(String) set it} on the layout before rendering the outer layout.
     * </p>
     *
     * @return never {@code null}, maybe empty
     */
    public final String getContent() {
        return content;
    }

    /**
     * Set the content from the rendered {@link #getContentView() inner content view}.
     * <p>
     * The {@link io.dropwizard.views.ViewRenderer} is responsible for setting this value.
     * </p>
     *
     * @param content not {@code null}
     */
    public final void setContent(final String content) {
        this.content = Validate.notNull(content, "content");
    }

    /**
     * Get the inner content view of the layout.
     *
     * @return never {@code null}
     */
    public final View getContentView() {
        return contentView;
    }
}
