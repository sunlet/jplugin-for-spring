package net.jplugin.extension.spring.autoconfigure.tx;

import net.jplugin.common.kits.filter.FilterChain;
import net.jplugin.core.kernel.api.AbstractExtensionInterceptor;
import net.jplugin.core.kernel.api.ExtensionInterceptorContext;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2022/5/19.
 */
public class JpluginTransactionInterceptor extends AbstractExtensionInterceptor {

    @Autowired(required = false)
    private TransactionInterceptor transactionInterceptor;

    /**
     * 当需要考虑扩展当中配置的methodFilter的时候，并且需要拦截整个方法，重载这个方法。
     * 在这个方法当中需要用super.execute(...) 或者 fc.next(ctx)继续后续的处理。
     * @param fc
     * @param ctx
     * @return
     * @throws Throwable
     */
    @Override
    protected Object execute(FilterChain fc, ExtensionInterceptorContext ctx) throws Throwable {
        if(transactionInterceptor == null){
            return super.execute(fc, ctx);
        }
        final Invoke invoke = new Invoke(ctx.getMethod(), ctx.getArgs(), fc, ctx);
        return transactionInterceptor.invoke(invoke);
    }

    private class Invoke implements MethodInvocation{

        private final Method method;
        private final Object[] arguments;
        private final FilterChain fc;
        private final ExtensionInterceptorContext ctx;

        public Invoke(Method method, Object[] arguments, FilterChain fc, ExtensionInterceptorContext ctx) {
            this.method = method;
            this.arguments = arguments;
            this.fc = fc;
            this.ctx = ctx;
        }

        /**
         * Get the method being called.
         * <p>This method is a friendly implementation of the
         * @return the method being called
         */
        @Override
        public Method getMethod() {
            return this.method;
        }

        /**
         * Get the arguments as an array object.
         * It is possible to change element values within this
         * array to change the arguments.
         *
         * @return the argument of the invocation
         */
        @Override
        public Object[] getArguments() {
            return this.arguments;
        }

        /**
         * Proceed to the next interceptor in the chain.
         * <p>The implementation and the semantics of this method depends
         * on the actual joinpoint type (see the children interfaces).
         *
         * @return see the children interfaces' proceed definition
         * @throws Throwable if the joinpoint throws an exception
         */
        @Override
        public Object proceed() throws Throwable {
            return fc.next(ctx);
        }

        /**
         * Return the object that holds the current joinpoint's static part.
         * <p>For instance, the target object for an invocation.
         *
         * @return the object (can be null if the accessible object is static)
         */
        @Override
        public Object getThis() {
            return null;
        }

        /**
         * Return the static part of this joinpoint.
         * <p>The static part is an accessible object on which a chain of
         * interceptors are installed.
         */
        @Override
        public AccessibleObject getStaticPart() {
            return this.method;
        }

    }
}
