package pl.mrugames.commons.router;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.AntPathMatcher;
import pl.mrugames.commons.router.request_handlers.ObjectRequestHandler;

import static org.mockito.Mockito.spy;

@Configuration
@ComponentScan("pl.mrugames.commons")
public class TestConfiguration {

    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }

    @Bean
    @Primary
    public ObjectRequestHandler objectRequestHandler() {
        return spy(new ObjectRequestHandler());
    }

    @Bean
    @Primary
    public ObjectMapper mapper(ObjectMapper mapper) {
        return spy(mapper);
    }
}
