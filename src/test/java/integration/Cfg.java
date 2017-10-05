package integration;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"pl.mrugames.commons", "integration"})
public class Cfg {

    @Bean("integrationSubject")
    Subject<String> subject() {
        return PublishSubject.create();
    }
}
