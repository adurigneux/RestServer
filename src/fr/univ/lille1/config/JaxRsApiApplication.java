package fr.univ.lille1.config;

import fr.univ.lille1.service.FtpService;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * This implementations is only used to create the main point of or services.
 * We add here the different point of connexion that the client can use
 * Our server will matches every url and calls only via the different services setted in the configuration
 *
 * @author Durigneux Antoine
 * @author Scouflaire Emmanuel
 */
public class JaxRsApiApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        // Add all services
        // For non-JAX-RS aware web container environments
        // Application sub-class needs to be created which returns sets of
        // JAX-RS root resources and providers
        classes.add(FtpService.class);
        return classes;
    }
}
