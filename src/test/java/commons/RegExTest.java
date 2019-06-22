package commons;

import com.sun.org.apache.xml.internal.serialize.LineSeparator;
import io.yooksi.commons.util.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("WeakerAccess")
public class RegExTest {

    @Test
    public void testEOLNormalization() {

        String windows = StringUtils.normalizeEOL("Sample text" + LineSeparator.Windows);
        String unix = StringUtils.normalizeEOL("Sample text" + LineSeparator.Unix);
        String mac = StringUtils.normalizeEOL("Sample text" + LineSeparator.Macintosh);

        Assertions.assertEquals(windows, unix);
        Assertions.assertEquals(windows, mac);
        Assertions.assertEquals(unix, mac);
    }
}
