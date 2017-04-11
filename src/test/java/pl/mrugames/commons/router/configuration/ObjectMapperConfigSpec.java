package pl.mrugames.commons.router.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.TestConfiguration;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class ObjectMapperConfigSpec {
    @Autowired
    private ObjectMapper mapper;

    @Test
    public void shouldProperlyReadDateOfAGivenFormat() throws IOException {
        LocalDateTime time = LocalDateTime.of(2000, 1, 2, 3, 4, 5);

        LocalDateTime mapped = mapper.readValue("\"2000-01-02T03:04:05\"", LocalDateTime.class);

        assertThat(mapped).isEqualTo(time);
    }

    @Test
    public void shouldProperlyMapPrimitiveTypes() throws IOException {
        int a = 1;
        short b = 2;
        long c = 3;
        float d = 4.1f;
        double e = 1.1;

        assertThat(mapper.readValue("\"1\"", int.class)).isEqualTo(a);
        assertThat(mapper.readValue("\"2\"", short.class)).isEqualTo(b);
        assertThat(mapper.readValue("\"3\"", long.class)).isEqualTo(c);
        assertThat(mapper.readValue("\"4.1\"", float.class)).isEqualTo(d);
        assertThat(mapper.readValue("\"1.1\"", double.class)).isEqualTo(e);
    }
}
