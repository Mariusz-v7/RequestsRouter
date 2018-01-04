package pl.mrugames.commons.router;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
class I18nReplacer {
    private final MessageSource messageSource;
    private final Pattern simple = Pattern.compile("\\$\\{([A-Za-z0-9._]*?)}");
    private final Pattern argsPattern = Pattern.compile("'(.*?)'");
    private final Pattern withParams = Pattern.compile("\\$\\{([A-Za-z0-9._]*?)\\(\\s*('.*?')*\\s*\\)}");


    I18nReplacer(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    String replace(String text) {
        Matcher matcher = simple.matcher(text);

        while (matcher.find()) {
            String msg = matcher.group(1);

            msg = messageSource.getMessage(msg, null, null);

            text = text.replace(matcher.group(), msg);
        }

        // resolve params

        matcher = withParams.matcher(text);
        while (matcher.find()) {
            String msg = matcher.group(1);
            String params = matcher.group(2);

            List<String> paramList = new LinkedList<>();

            if (params != null) {
                Matcher paramMatcher = argsPattern.matcher(params);
                while (paramMatcher.find()) {
                    paramList.add(paramMatcher.group(1));
                }
            }

            msg = messageSource.getMessage(msg, paramList.toArray(), null);

            text = text.replace(matcher.group(), msg);
        }

        return text;
    }
}
