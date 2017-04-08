package pl.mrugames.commons.router;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.springframework.util.AntPathMatcher;

import java.util.Map;

@RunWith(BlockJUnit4ClassRunner.class)
public class RouterSpec {
    private Map<String, String> routes;

    // app/players/{playerId} -> should get playerId and assign it into playerId variable
    @Test
    public void test() {
        AntPathMatcher matcher = new AntPathMatcher();

        Map<String, String> test = matcher.extractUriTemplateVariables("app/players/{playerId}", "app/players/12");

        test.isEmpty();
    }
}
