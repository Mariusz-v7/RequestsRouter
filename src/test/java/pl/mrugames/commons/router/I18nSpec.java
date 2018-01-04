package pl.mrugames.commons.router;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.controllers.ModelToTranslate;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class I18nSpec {
    @Autowired
    private Router router;


    @Test
    public void givenRouterReturnsStringPlaceholder_thenReplaceItWithTranslation() throws IllegalAccessException {
        RouteInfo routeInfo = router.findRoute("i18n/return-string", RequestMethod.GET);
        String result = (String) router.navigate(routeInfo, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());

        assertThat(result).isEqualTo("Prosty ciąg znaków");
    }

    @Test
    public void givenRouterReturnsSimpleModel_thenReplaceString() throws IllegalAccessException {
        RouteInfo routeInfo = router.findRoute("i18n/model", RequestMethod.GET);
        ModelToTranslate result = (ModelToTranslate) router.navigate(routeInfo, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());

        assertThat(result.getValue()).isEqualTo("raz");
    }

    @Test
    public void translateNestedObjectsSpec() throws IllegalAccessException {
        RouteInfo routeInfo = router.findRoute("i18n/model", RequestMethod.GET);
        ModelToTranslate result = (ModelToTranslate) router.navigate(routeInfo, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());

        assertThat(result.getNestedModelToTranslate().getValue()).isEqualTo("dwa");
    }

}
