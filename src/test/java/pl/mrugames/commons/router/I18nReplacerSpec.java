package pl.mrugames.commons.router;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class I18nReplacerSpec {
    @Autowired
    private I18nReplacer replacer;

    @Test
    public void oneMessageReplaceTest() {
        assertThat(replacer.replace("${i18n.simple_string}")).isEqualTo("Prosty ciąg znaków");
        assertThat(replacer.replace("1. ${i18n.simple_string}.")).isEqualTo("1. Prosty ciąg znaków.");
    }

    @Test
    public void twoMessagesReplaceTest() {
        assertThat(replacer.replace("${i18n.one}, ${i18n.two}")).isEqualTo("raz, dwa");
    }
}
