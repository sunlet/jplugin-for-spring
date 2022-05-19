package net.jplugin.extension.spring.autoconfigure.tx;

import net.jplugin.core.service.api.UseTransaction;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.TransactionAnnotationParser;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2022/5/19.
 */
public class JpluginTransactionAnnotationParser implements TransactionAnnotationParser, Serializable {

    @Override
    public boolean isCandidateClass(Class<?> targetClass) {
        return AnnotationUtils.isCandidateClass(targetClass, UseTransaction.class);
    }

    @Override
    public TransactionAttribute parseTransactionAnnotation(AnnotatedElement element) {
        AnnotationAttributes attributes = AnnotatedElementUtils.findMergedAnnotationAttributes(
                element, UseTransaction.class, false, false);
        if (attributes == null) {
            return null;
        }
        //parse UseTransaction
        return parseTransactionAnnotation(attributes);
    }

    public TransactionAttribute parseTransactionAnnotation(UseTransaction ann) {
        return parseTransactionAnnotation(AnnotationUtils.getAnnotationAttributes(ann, false, false));
    }

    /**
     * @UseTransaction --> @Transactional
     */
    protected TransactionAttribute parseTransactionAnnotation(AnnotationAttributes attributes) {
        RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();

        Propagation propagation = Propagation.REQUIRED;
        rbta.setPropagationBehavior(propagation.value());
        Isolation isolation = Isolation.DEFAULT;
        rbta.setIsolationLevel(isolation.value());

        rbta.setTimeout(TransactionDefinition.TIMEOUT_DEFAULT);
        rbta.setTimeoutString("");

        rbta.setReadOnly(false);
        rbta.setQualifier("");
        rbta.setLabels(new ArrayList<>());

        List<RollbackRuleAttribute> rollbackRules = new ArrayList<>();
        rbta.setRollbackRules(rollbackRules);
        return rbta;
    }
}
