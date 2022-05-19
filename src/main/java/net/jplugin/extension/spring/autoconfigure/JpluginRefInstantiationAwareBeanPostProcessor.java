package net.jplugin.extension.spring.autoconfigure;

import net.jplugin.core.kernel.api.PluginEnvirement;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2022/5/12.
 * @author jzm
 * 注入JPlugin bean
 */
public class JpluginRefInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor {

    private static final Set<String> FRAME_WORK_BEANNAME_PREFIX = new HashSet<>();

    static {
        FRAME_WORK_BEANNAME_PREFIX.add("org.springframework");
        FRAME_WORK_BEANNAME_PREFIX.add("javax");
        //FRAME_WORK_BEANNAME_PREFIX.add("java");
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        if(!skipInject(bean) && !AopUtils.isAopProxy(bean) && !JpluginBeanDefinitionRegistryPostProcessor.SpringBeanResolver.isJpluginResolved(bean)){
            PluginEnvirement.getInstance().resolveRefAnnotation(bean);
        }
        return pvs;
    }

    private boolean skipInject(Object bean){
        final String name = bean.getClass().getName();
        return FRAME_WORK_BEANNAME_PREFIX.stream().anyMatch(e -> name.startsWith(e));
    }

}
