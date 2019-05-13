package io.yooksi.commons.aop;

import io.yooksi.commons.logger.CommonLogger;
import io.yooksi.commons.validator.BeanValidator;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;

public class AOPProxy {

    // TODO: Document this method
    public static <T> T createFor(Object T) {

        ProxyFactory pf = new ProxyFactory(T);
        pf.addAdvice((MethodInterceptor) mi -> {

            CommonLogger.getLogger().debug("Method was intercepted!");
            CommonLogger.getLogger().debug("Invocation target : " + mi.getThis());
            CommonLogger.getLogger().debug("Method name : " + mi.getMethod().getName());
            CommonLogger.getLogger().debug("Method arguments : " + java.util.Arrays.toString(mi.getArguments()));

            return BeanValidator.validateMethod(mi);
        });
        return (T) pf.getProxy();
    }
}
