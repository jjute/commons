package commons;

import io.yooksi.commons.git.DiffFilterOption;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("WeakerAccess")
public class GitOptionsTest {

    @Test
    public void createDiffFilterOptionsTest() {

        DiffFilterOption option = DiffFilterOption.create().build();
        Assertions.assertEquals("--diff-filter", option.getDesignation());
        Assertions.assertTrue(option.getValue().isEmpty());

        DiffFilterOption.Type[] types = { DiffFilterOption.Type.ADDED, DiffFilterOption.Type.COPIED };
        option = DiffFilterOption.create().include(types).build();
        Assertions.assertEquals("AC", option.getValue());

        option = DiffFilterOption.create().include(types).exclude(DiffFilterOption.Type.RENAMED).build();
        Assertions.assertEquals("ACr", option.getValue());

        option = DiffFilterOption.create().include(types).setAllOrNone(true).build();
        Assertions.assertEquals("AC*", option.getValue());
    }
}
