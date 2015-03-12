package fr.univ.lille1.config;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.ws.rs.ext.RuntimeDelegate;
import java.util.Arrays;

/**
 * This implementations contains all the configuration that are used by the server.
 * it binds or services and offer json provider
 *
 * @author Durigneux Antoine
 * @author Scouflaire Emmanuel
 */

@Configuration
public class AppConfig {
    @Bean(destroyMethod = "shutdown")
    public SpringBus cxf() {
        return new SpringBus();
    }

    @Bean
    @DependsOn("cxf")
    public Server jaxRsServer() {
        JAXRSServerFactoryBean factory = RuntimeDelegate.getInstance()
                .createEndpoint(jaxRsApiApplication(),
                        JAXRSServerFactoryBean.class);

        factory.setAddress("/" + factory.getAddress());
        factory.setProviders(Arrays.<Object>asList(jsonProvider()));
        return factory.create();
    }

    @Bean
    public JaxRsApiApplication jaxRsApiApplication() {
        return new JaxRsApiApplication();
    }

    @Bean
    public JacksonJsonProvider jsonProvider() {
        return new JacksonJsonProvider();
    }
}
