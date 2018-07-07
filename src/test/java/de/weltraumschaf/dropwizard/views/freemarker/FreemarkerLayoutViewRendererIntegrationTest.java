package de.weltraumschaf.dropwizard.views.freemarker;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import io.dropwizard.views.View;
import io.dropwizard.views.ViewMessageBodyWriter;
import io.dropwizard.views.ViewRenderExceptionMapper;
import io.dropwizard.views.ViewRenderer;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Integration tests for {@link FreemarkerLayoutViewRenderer}.
 */
public final class FreemarkerLayoutViewRendererIntegrationTest extends JerseyTest {

    @Override
    protected Application configure() {
        return configure(new ExampleResource());
    }

    @BeforeEach
    void startTestContainer() throws Exception {
        super.setUp();
    }

    @AfterEach
    void stopTestContainer() throws Exception {
        super.tearDown();
    }
    
    private Application configure(Object... resources) {
        final ResourceConfig config = new ResourceConfig();
        final ViewRenderer renderer = new FreemarkerLayoutViewRenderer();
        config.register(new ViewMessageBodyWriter(new MetricRegistry(), ImmutableList.of(renderer)));
        config.register(new ViewRenderExceptionMapper());
        Arrays.stream(resources).forEach(config::register);
        return config;
    }

    @Test
    void rendersViewWithAbsoluteTemplatePath() {
        final String response = target("/test/absolute").request().get(String.class);

        assertThat(response, is("Hello, Dude!"));
    }

    @Test
    void rendersViewWithRelativeTemplatePath() {
        final String response = target("/test/relative").request().get(String.class);

        assertThat(response, is("Hello, World!"));
    }

    @Test
    void throwsWebApplicationExceptionIfTemplateNotFound() {
        assertThrows(WebApplicationException.class, () -> target("/test/not-found").request().get(String.class));
    }

    @Test
    void throwsWebApplicationExceptionIfTemplateCantCompile() {
        assertThrows(WebApplicationException.class, () -> target("/test/error").request().get(String.class));
    }

    @Test
    void rendersViewUsingUnsafeInputWithAutoEscapingEnabled() {
        final String unsafe = "<script>alert('hello')</script>";
        final Response response = target("/test/auto-escaping").request().post(Entity.form(new Form("input", unsafe)));

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getHeaderString("content-type"), is(equalToIgnoringCase(MediaType.TEXT_HTML)));
        assertThat(
            response.readEntity(String.class),
            is("<html><body><p>&lt;script&gt;alert(&#39;hello&#39;)&lt;/script&gt;</p></body></html>"));
    }

    @Path("/test/")
    @Produces(MediaType.TEXT_HTML)
    public static class ExampleResource {
        @GET
        @Path("/absolute")
        public AbsoluteView absolute() {
            return new AbsoluteView("Dude");
        }

        @GET
        @Path("/relative")
        public RelativeView relative() {
            return new RelativeView();
        }

        @GET
        @Path("/not-found")
        public NotFoundView bad() {
            return new NotFoundView();
        }

        @GET
        @Path("/error")
        public ErrorView error() {
            return new ErrorView();
        }

        @POST
        @Path("/auto-escaping")
        public AutoEscapingView autoEscaping(@FormParam("input") String input) {
            return new AutoEscapingView(input);
        }
    }

    public static final class AbsoluteView extends View {
        private final String name;

        AbsoluteView(final String name) {
            super("/de/weltraumschaf/dropwizard/views/freemarker/absolute.ftl");
            this.name = name;
        }

        @SuppressWarnings("unused")
        public String getName() {
            return name;
        }
    }

    public static final class RelativeView extends View {
        RelativeView() {
            super("relative.ftl");
        }
    }

    public static final class NotFoundView extends View {
        NotFoundView() {
            super("/does-not-exists.ftl");
        }
    }

    public static final class ErrorView extends View {
        ErrorView() {
            super("/de/weltraumschaf/dropwizard/views/freemarker/error.ftl");
        }
    }

    public static class AutoEscapingView extends View {
        private final String content;

        AutoEscapingView(String content) {
            super("/de/weltraumschaf/dropwizard/views/freemarker/auto-escaping.ftlh");
            this.content = content;
        }

        @SuppressWarnings("unused")
        public String getContent() {
            return content;
        }
    }
}
