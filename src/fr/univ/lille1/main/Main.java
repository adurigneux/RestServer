package fr.univ.lille1.main;

import fr.univ.lille1.config.AppConfig;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.MultipartConfigElement;

/**
 * Main class of the server.
 * We defined the configuration and the original path of the server.
 *
 * @author Durigneux Antoine
 * @author Scouflaire Emmanuel
 */
public class Main {

    public static void main(String[] args) {
        Server server = new Server(8080);
        try {
            // Register and map the dispatcher servlet
            final ServletHolder servletHolder = new ServletHolder(new CXFServlet());
            final ServletContextHandler context = new ServletContextHandler();
            servletHolder.getRegistration()
                    .setMultipartConfig(
                            new MultipartConfigElement("data/tmp", 1048576,
                                    1048576, 262144));
            context.setContextPath("/");
            context.addServlet(servletHolder, "/rest/*");
            context.addEventListener(new ContextLoaderListener());
            context.setInitParameter("contextClass",
                    AnnotationConfigWebApplicationContext.class.getName());
            context.setInitParameter("contextConfigLocation",
                    AppConfig.class.getName());

            server.setHandler(context);

            server.start();
            server.join();
        } catch (Exception ignore) {

        } finally {
            server.destroy();
        }

    }

}
