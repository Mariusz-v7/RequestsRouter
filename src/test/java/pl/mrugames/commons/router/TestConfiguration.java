package pl.mrugames.commons.router;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.AntPathMatcher;
import pl.mrugames.commons.router.arg_resolvers.PathArgumentResolver;
import pl.mrugames.commons.router.arg_resolvers.RequestPayloadArgumentResolver;
import pl.mrugames.commons.router.arg_resolvers.SessionArgumentResolver;
import pl.mrugames.commons.router.request_handlers.ObjectRequestHandler;

import static org.mockito.Mockito.spy;

@Configuration
@ComponentScan("pl.mrugames.commons")
public class TestConfiguration {

    @Bean
    public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer(ApplicationContext context) {
        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        configurer.setLocations(context.getResource("config.properties"));

        return configurer;
    }

    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }

    @Bean
    @Primary
    public ObjectRequestHandler objectRequestHandlerSpy(ObjectRequestHandler objectRequestHandler) {
        return spy(objectRequestHandler);
    }

    @Bean
    @Primary
    public ObjectMapper mapper(ObjectMapper mapper) {
        return spy(mapper);
    }

    @Bean
    @Primary
    public Router routerSpy(Router router) {
        return spy(router);
    }

    @Bean
    @Primary
    public PathArgumentResolver pathArgumentResolverSpy(PathArgumentResolver pathArgumentResolver) {
        return spy(pathArgumentResolver);
    }

    @Bean
    @Primary
    public RequestPayloadArgumentResolver requestPayloadArgumentResolverSpy(RequestPayloadArgumentResolver requestPayloadArgumentResolver) {
        return spy(requestPayloadArgumentResolver);
    }

    @Bean
    @Primary
    public SessionArgumentResolver sessionArgumentResolverSpy(SessionArgumentResolver sessionArgumentResolver) {
        return spy(sessionArgumentResolver);
    }
}
