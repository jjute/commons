package commons;

import io.yooksi.commons.aop.AOPProxy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("WeakerAccess")
public class AOPProxyTest {

    @Test
    public void testAOPProxyCreation() {
        Assertions.assertNotNull(AOPProxy.createValidationProxy(new AOPProxyTest()));
    }
}
