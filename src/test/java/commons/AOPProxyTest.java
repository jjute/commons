package commons;

import io.yooksi.commons.aop.AOPProxy;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class AOPProxyTest {

    @Test
    public void testAOPProxyCreation() {
        assertNotNull(AOPProxy.createValidationProxy(new AOPProxyTest()));
    }
}
