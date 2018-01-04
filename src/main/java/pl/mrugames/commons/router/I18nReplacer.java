package pl.mrugames.commons.router;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
class I18nReplacer {
    private final MessageSource messageSource;
    private final Pattern pattern = Pattern.compile("\\$\\{([A-Za-z0-9._]*?)}");

    I18nReplacer(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    String replace(String text) {
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String msg = matcher.group(1);

            msg = messageSource.getMessage(msg, null, null);

            text = text.replace(matcher.group(), msg);
        }

        return text;
    }
}
