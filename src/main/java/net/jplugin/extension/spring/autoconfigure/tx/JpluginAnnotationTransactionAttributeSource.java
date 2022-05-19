package net.jplugin.extension.spring.autoconfigure.tx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.TransactionAnnotationParser;
import org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2022/5/19.
 * 兼容 Jplugin UseTransaction注解
 * @author jzm
 */
public class JpluginAnnotationTransactionAttributeSource extends AbstractFallbackTransactionAttributeSource implements TransactionAttributeSource {

    private TransactionAnnotationParser jpluginTransactionAnnotationParser = new JpluginTransactionAnnotationParser();

    @Autowired
    private TransactionAttributeSource transactionAttributeSource;

    /**
     * Return the transaction attribute for the given method,
     * or {@code null} if the method is non-transactional.
     *
     * @param method      the method to introspect
     * @param targetClass the target class (may be {@code null},
     *                    in which case the declaring class of the method must be used)
     * @return the matching transaction attribute, or {@code null} if none found
     */
    @Override
    public TransactionAttribute getTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
        final TransactionAttribute attribute = this.transactionAttributeSource.getTransactionAttribute(method, targetClass);
        if(attribute != null){
            return attribute;
        }
        return super.getTransactionAttribute(method, targetClass);
    }


    @Override
    protected TransactionAttribute findTransactionAttribute(Class<?> clazz) {
        return jpluginTransactionAnnotationParser.parseTransactionAnnotation(clazz);
    }

    @Override
    protected TransactionAttribute findTransactionAttribute(Method method) {
        return jpluginTransactionAnnotationParser.parseTransactionAnnotation(method);
    }


    @Override
    public boolean isCandidateClass(Class<?> targetClass) {
        final boolean candidateClass = this.transactionAttributeSource.isCandidateClass(targetClass);
        if(candidateClass){
            return candidateClass;
        }
        return this.jpluginTransactionAnnotationParser.isCandidateClass(targetClass);
    }

}
