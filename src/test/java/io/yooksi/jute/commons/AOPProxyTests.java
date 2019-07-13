package io.yooksi.jute.commons;

import io.yooksi.jute.commons.aop.AOPProxy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("WeakerAccess")
public class AOPProxyTests {

    @Test
    public void testAOPProxyCreation() {
        Assertions.assertNotNull(AOPProxy.createValidationProxy(new AOPProxyTests()));
    }
}
