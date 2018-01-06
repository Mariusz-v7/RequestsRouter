package pl.mrugames.commons.router;

import org.springframework.stereotype.Service;
import pl.mrugames.commons.router.annotations.Translate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

@Service
public class I18nObjectTranslator {
    private final I18nReplacer replacer;

    I18nObjectTranslator(I18nReplacer replacer) {
        this.replacer = replacer;
    }

    public void translate(Object object) throws IllegalAccessException {

        for (Field field : object.getClass().getDeclaredFields()) {
            boolean accessible = field.isAccessible();

            if (!field.getType().isAssignableFrom(String.class)) {
                try {
                    if (!accessible) {
                        field.setAccessible(true);
                    }

                    Object nestedValue = field.get(object);

                    if (field.getType().isAnnotationPresent(Translate.class)) {
                        translate(nestedValue);
                    } else if (nestedValue instanceof Collection) {
                        Collection<?> collection = (Collection<?>) nestedValue;
                        for (Object element : collection) {
                            if (element.getClass().isAnnotationPresent(Translate.class)) {
                                translate(element);
                            }
                        }
                    }
                } finally {
                    if (!accessible) {
                        field.setAccessible(false);
                    }
                }

                continue;
            }

            try {
                if (!accessible) {
                    field.setAccessible(true);
                }

                String str = (String) field.get(object);
                String translated = replacer.replace(str);

                if (!str.equals(translated) && Modifier.isFinal(field.getModifiers())) {
                    throw new IllegalArgumentException("Field: " + object.getClass().getSimpleName() + "#" + field.getName() + " is final. Cannot apply translation.");
                }

                if (!str.equals(translated)) {
                    field.set(object, translated);
                }
            } finally {
                if (!accessible) {
                    field.setAccessible(false);
                }
            }
        }
    }

    public String translateString(String str) {
        return replacer.replace(str);
    }
}
