package io.yooksi.jute.commons.aop;

import io.yooksi.jute.commons.define.MethodsNotNull;
import io.yooksi.jute.commons.logger.LibraryLogger;
import io.yooksi.jute.commons.validator.BeanValidator;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;

import java.lang.reflect.Method;


/**
 * This classes handles proxy based frameworks such as Spring AOP,
 * to intercept methods and either validate values such as method
 * parameters or manipulate method execution and outcome.
 */
@MethodsNotNull
@SuppressWarnings({"unused", "unchecked"})
public class AOPProxy {

    /**
     * <p>Create a Spring AOP proxy that works for all interfaces that the given target implements.</p>
     * <p>The proxy will intercepts any method calls on an interface on its way to the target, validate
     * the method parameters and return value as well as the state of the object it belongs to.</p>
     * If the object doesn't implement any interfaces the proxy will intercept all class methods.
     *
     * @param target object to create proxy for
     * @return newly created proxy cast to target object class
     */
    public static <T> T createValidationProxy(T target) {

        LibraryLogger.debug("Creating new AOP validation proxy for object %s", target);
        ProxyFactory pf = new ProxyFactory(target);
        pf.addAdvice((MethodInterceptor) mi -> {

            Method method = mi.getMethod();         /* the method being intercepted */
            Object[] params = mi.getArguments();    /* list of method arguments     */
            Object targetObj = mi.getThis();        /* target object being proxied  */

            LibraryLogger.debug("Method %s (args: %s) was intercepted while on it's " +
                    "way to target %s", method.getName(), java.util.Arrays.toString(params), targetObj);

            return BeanValidator.validateMethod(mi);
        });
        return (T) pf.getProxy();
    }
}
