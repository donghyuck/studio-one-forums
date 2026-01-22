package studio.one.application.forums.web.etag;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EtagUtilTest {

    @Test
    void buildsWeakEtag() {
        assertEquals("W/\"12\"", EtagUtil.buildWeakEtag(12L));
    }

    @Test
    void parsesIfMatchVersion() {
        assertEquals(12L, EtagUtil.parseIfMatchVersion("W/\"12\""));
        assertEquals(12L, EtagUtil.parseIfMatchVersion("\"12\""));
        assertEquals(12L, EtagUtil.parseIfMatchVersion("12"));
        assertEquals(12L, EtagUtil.parseIfMatchVersion("W/\"12\", \"13\""));
    }
}
