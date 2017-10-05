package integration;

import io.reactivex.Observable;
import io.reactivex.subjects.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import pl.mrugames.commons.router.Mono;
import pl.mrugames.commons.router.ResponseStatus;
import pl.mrugames.commons.router.annotations.Arg;
import pl.mrugames.commons.router.annotations.Controller;
import pl.mrugames.commons.router.annotations.Route;

@Controller("integration")
public class IntegController {

    @Autowired
    @Qualifier("integrationSubject")
    Subject<String> subject;

    @Route
    public boolean basicTest(@Arg("a") int a) {
        return a < 10;
    }

    @Route("void")
    public void aVoid() {

    }

    @Route("null")
    public Object getNull() {
        return null;
    }

    @Route("mono-void")
    public Mono<Void> monoVoid() {
        return Mono.OK;
    }

    @Route("mono-error")
    public Mono<Void> monoError() {
        return Mono.of(ResponseStatus.ERROR);
    }

    @Route("exception")
    public void exception() {
        throw new IllegalArgumentException("an exception!");
    }

    @Route("mono-error-custom")
    public Mono<Void> monoErrorCustom() {
        return Mono.error(ResponseStatus.BAD_PARAMETERS, "bad params!");
    }

    @Route("observable")
    public Observable<String> observable() {
        return Observable.just("hello");
    }

    @Route("observable-multi")
    public Observable<String> observableMulti() {
        return Observable.just("hello", "observable");
    }

    @Route("observable-error")
    public Observable<String> observableError() {
        return Observable.error(new IllegalArgumentException("what?"));
    }

    @Route("observable-mono-void")
    public Observable<Mono<Void>> observableMonoVoid() {
        return Observable.just(Mono.OK);
    }

    @Route("observable-mono-error")
    public Observable<Mono<Void>> observableMonoError() {
        return Observable.just(Mono.error(ResponseStatus.ERROR, "error"));
    }

    @Route("subj-observable")
    public Observable<String> subjToObs() {
        return subject.hide();
    }
}
