package pl.mrugames.commons.router.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.util.AntPathMatcher;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
class RouterConfiguration {
    @Bean
    ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return mapper;
    }

    @Bean
    ExecutableValidator validator() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        return validatorFactory.getValidator().forExecutables();
    }

    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }

}
