package commons;

import io.yooksi.commons.aop.AOPProxy;
import io.yooksi.commons.logger.CommonLogger;
import org.apache.logging.log4j.Level;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class AOPProxyTest {

    private static AOPProxyTest test;

    @Test
    public void testAOPProxyCreation() {

        CommonLogger logger = new CommonLogger("test", Level.DEBUG, true);
        test = AOPProxy.createValidationProxy(new AOPProxyTest());

        assertNotNull(test);
    }
}
