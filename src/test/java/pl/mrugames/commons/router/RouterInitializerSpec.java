package pl.mrugames.commons.router;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.annotations.ArgDefaultValue;
import pl.mrugames.commons.router.controllers.TestController;
import pl.mrugames.commons.router.permissions.AccessType;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class RouterInitializerSpec {
    @Autowired
    private RouterInitializer initializer;

    @Autowired
    private TestController testController;

    @Test
    public void givenApplicationStart_thenInitializerIsNotNull() {
        assertThat(initializer).isNotNull();
    }

    @Test
    public void givenApplicationStart_thenTestControllerIsNotNull() {
        assertThat(testController).isNotNull();
    }

    @Test
    public void givenApplicationStart_thenControllersContainsTestController() {
        assertThat(initializer.getControllers()).contains("app/test");
    }

    @Test
    public void givenApplicationStart_thenMapShouldContainRouteToTestController() {
        assertThat(initializer.getRoutes()).containsKeys("GET:app/test/route1");
    }

    @Test
    public void givenApplicationStart_thenMapShouldContainRouteToTestControllerWithPOST() {
        assertThat(initializer.getRoutes()).containsKeys("POST:app/test/route1");
    }

    @Test
    public void givenApplicationStart_whenInvokeFirstRoute_thenItShouldReturnValueFromController() throws InvocationTargetException, IllegalAccessException {
        RouteInfo routeInfo = initializer.getRoutes().get("GET:app/test/route1");
        assertThat(routeInfo.getMethod().invoke(routeInfo.getControllerInstance())).isEqualTo("route1");
    }

    @Test
    public void givenMethodHasArgumentsWithoutAnnotations_thenAllOfThemShouldBeInserted() {
        RouteInfo routeInfo = initializer.getRoutes().get("GET:app/test/sum");
        List<RouteParameter> parameters = routeInfo.getParameters();
        assertThat(parameters).hasSize(2);
    }

    @Test
    public void givenMethodHasArgumentsWithoutAnnotations_thenParameterNameShouldBeNull() {
        RouteInfo routeInfo = initializer.getRoutes().get("GET:app/test/sum");
        List<RouteParameter> parameters = routeInfo.getParameters();

        for (RouteParameter parameter : parameters) {
            assertThat(parameter.getType().getName()).isEqualTo("int");
            assertThat(parameter.getName()).isNull();
            assertThat(parameter.getDefaultValue()).isNull();
            assertThat(parameter.getParameterType()).isEqualTo(RouteParameter.ParameterType.NONE);
        }
    }

    @Test
    public void givenAnnotatedParameters_thenAllOfThemAreInserted() {
        RouteInfo routeInfo = initializer.getRoutes().get("GET:app/test/concat");
        List<RouteParameter> parameters = routeInfo.getParameters();
        assertThat(parameters).hasSize(4);
    }

    @Test
    public void givenParameterHasAnnotationWithName_thenFirstParamIsRecognized() {
        RouteInfo routeInfo = initializer.getRoutes().get("GET:app/test/concat");
        List<RouteParameter> parameters = routeInfo.getParameters();
        assertThat(parameters.get(0).getType().getSimpleName()).isEqualTo("int");
        assertThat(parameters.get(0).getName()).isEqualTo("a");
        assertThat(parameters.get(0).getDefaultValue()).isEqualTo(ArgDefaultValue.ARG_NULL_DEFAULT_VALUE);
        assertThat(parameters.get(0).getParameterType()).isEqualTo(RouteParameter.ParameterType.ARG);
    }

    @Test
    public void givenParameterHasAnnotationWithName_thenSecondParamIsRecognized() {
        RouteInfo routeInfo = initializer.getRoutes().get("GET:app/test/concat");
        List<RouteParameter> parameters = routeInfo.getParameters();
        assertThat(parameters.get(1).getType().getSimpleName()).isEqualTo("String");
        assertThat(parameters.get(1).getName()).isEqualTo("b");
        assertThat(parameters.get(1).getDefaultValue()).isEqualTo(ArgDefaultValue.ARG_NULL_DEFAULT_VALUE);
        assertThat(parameters.get(1).getParameterType()).isEqualTo(RouteParameter.ParameterType.ARG);
    }

    @Test
    public void givenParameterHasAnnotationWithName_thenThirdParamIsRecognized() {
        RouteInfo routeInfo = initializer.getRoutes().get("GET:app/test/concat");
        List<RouteParameter> parameters = routeInfo.getParameters();
        assertThat(parameters.get(2).getType().getSimpleName()).isEqualTo("Double");
        assertThat(parameters.get(2).getName()).isEqualTo("c");
        assertThat(parameters.get(2).getDefaultValue()).isEqualTo(ArgDefaultValue.ARG_NULL_DEFAULT_VALUE);
        assertThat(parameters.get(2).getParameterType()).isEqualTo(RouteParameter.ParameterType.ARG);
    }

    @Test
    public void givenParameterHasAnnotationWithName_thenFourthParamIsRecognized() {
        RouteInfo routeInfo = initializer.getRoutes().get("GET:app/test/concat");
        List<RouteParameter> parameters = routeInfo.getParameters();
        assertThat(parameters.get(3).getType().getSimpleName()).isEqualTo("String");
        assertThat(parameters.get(3).getName()).isEqualTo("d");
        assertThat(parameters.get(3).getDefaultValue()).isEqualTo("last");
        assertThat(parameters.get(3).getParameterType()).isEqualTo(RouteParameter.ParameterType.ARG);
    }

    @Test
    public void givenParameterHasAnnotationWithPathVar_thenParamTypeIsSet() {
        RouteInfo routeInfo = initializer.getRoutes().get("GET:app/test/player/{playerId}");
        List<RouteParameter> parameters = routeInfo.getParameters();
        assertThat(parameters.get(0).getParameterType()).isEqualTo(RouteParameter.ParameterType.PATH_VAR);
    }

    @Test
    public void givenNoPermissionRelatedAnnotation_thenRouteHasAccessTypeOfOnlyLogged() {
        RouteInfo routeInfo = initializer.getRoutes().get("GET:app/test/only-logged");
        assertThat(routeInfo.getAccessType()).isEqualTo(AccessType.ONLY_LOGGED_IN);
        assertThat(routeInfo.getAllowedRoles()).isEmpty();
    }

    @Test
    public void givenOnlyNotLoggedAnnotation_thenRouteHasAccessTypeOfOnlyNotLogged() {
        RouteInfo routeInfo = initializer.getRoutes().get("GET:app/test/only-not-logged");
        assertThat(routeInfo.getAccessType()).isEqualTo(AccessType.ONLY_NOT_LOGGED_IN);
        assertThat(routeInfo.getAllowedRoles()).isEmpty();
    }

    @Test
    public void givenAllAllowedAnnotation_thenRouteHasAccessTypeOfAllAllowed() {
        RouteInfo routeInfo = initializer.getRoutes().get("GET:app/test/all-allowed");
        assertThat(routeInfo.getAccessType()).isEqualTo(AccessType.ALL_ALLOWED);
        assertThat(routeInfo.getAllowedRoles()).isEmpty();
    }

    @Test
    public void givenAllowedRolesAnnotation_thenRouteHasAccessOfTypeSpecificRoles_andContainsListOfRoles() {
        RouteInfo routeInfo = initializer.getRoutes().get("GET:app/test/admin");
        assertThat(routeInfo.getAccessType()).isEqualTo(AccessType.ONLY_WITH_SPECIFIC_ROLES);
        assertThat(routeInfo.getAllowedRoles()).contains("admin", "superuser");
    }

    @Test
    public void givenMixedPermissionAnnotations_thenAllowedRolesHasBiggestPriority() {
        RouteInfo routeInfo = initializer.getRoutes().get("GET:app/test/bad-perms");
        assertThat(routeInfo.getAccessType()).isEqualTo(AccessType.ONLY_WITH_SPECIFIC_ROLES);
        assertThat(routeInfo.getAllowedRoles()).contains("admin");
    }
}
