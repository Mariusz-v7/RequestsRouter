package pl.mrugames.commons.router.arg_resolvers;

import com.google.common.primitives.Primitives;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import pl.mrugames.commons.router.RouteInfo;
import pl.mrugames.commons.router.exceptions.IncompatibleParameterException;
import pl.mrugames.commons.router.exceptions.PathParameterNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
class PathArgumentResolver {
    private final AntPathMatcher pathMatcher;

    private PathArgumentResolver(AntPathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    Map<String, Object> resolve(String path, String pattern, List<RouteInfo.Parameter> parameters) {

        Map<String, String> mappedValues;
        try {
            mappedValues = pathMatcher.extractUriTemplateVariables(pattern, path);
        } catch (IllegalStateException e) {
            throw new PathParameterNotFoundException(path, pattern);
        }

        return parameters.stream()
                .filter(p -> p.getParameterType() == RouteInfo.ParameterType.PATH_VAR)
                .map(p -> map(mappedValues, p))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map.Entry<String, Object> map(Map<String, String> mappedValues, RouteInfo.Parameter parameter) {
        try {
            String strValue = mappedValues.get(parameter.getName());

            Class<?> type = parameter.getType();

            if (type.isPrimitive()) {
                type = Primitives.wrap(type);
            }

            Object converted = type.getConstructor(String.class).newInstance(strValue);

            return new AbstractMap.SimpleEntry<>(parameter.getName(), converted);
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new IncompatibleParameterException(parameter.getName(), parameter.getType());
        }
    }
}
