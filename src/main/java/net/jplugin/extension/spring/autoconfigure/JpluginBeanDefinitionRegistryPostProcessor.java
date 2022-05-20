package net.jplugin.extension.spring.autoconfigure;

import net.jplugin.core.config.api.ConfigFactory;
import net.jplugin.core.kernel.api.IObjectResolver;
import net.jplugin.core.kernel.api.PluginEnvirement;
import net.jplugin.extension.spring.autoconfigure.tx.JpluginAnnotationTransactionAttributeSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Administrator on 2022/5/13.
 */
public class JpluginBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    private static final String JPLUGIN_BEAM_NAME = ClassUtils.getShortName(JPluginInit.class);

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        if(!beanDefinitionRegistry.containsBeanDefinition(JPLUGIN_BEAM_NAME)){
            //register Jplugin Bean
            final AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(SpringBeanResolver.class).getBeanDefinition();
            beanDefinitionRegistry.registerBeanDefinition(ClassUtils.getShortName(SpringBeanResolver.class), beanDefinition);
            final AbstractBeanDefinition definition = BeanDefinitionBuilder.rootBeanDefinition(JPluginInit.class)
                    .setInitMethodName("init").setDestroyMethodName("stop").getBeanDefinition();
            beanDefinitionRegistry.registerBeanDefinition(JPLUGIN_BEAM_NAME, definition);
            final Field definitionNames = ReflectionUtils.findField(DefaultListableBeanFactory.class, "beanDefinitionNames");
            if(definitionNames != null){
                ReflectionUtils.makeAccessible(definitionNames);
                final List<String> beanNames = (List<String>) ReflectionUtils.getField(definitionNames, beanDefinitionRegistry);
                if(beanNames.contains(JPLUGIN_BEAM_NAME)){
                    beanNames.remove(JPLUGIN_BEAM_NAME);
                    //first start up.
                    beanNames.add(0, JPLUGIN_BEAM_NAME);
                }
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        final String[] namesForType = configurableListableBeanFactory.getBeanNamesForType(TransactionInterceptor.class);
        if(namesForType == null || namesForType.length == 0){
            return;
        }

        //修改transactionAttributeSource
        final BeanDefinition beanDefinition = configurableListableBeanFactory.getBeanDefinition(namesForType[0]);
        final MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        final AbstractBeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(JpluginAnnotationTransactionAttributeSource.class).getBeanDefinition();
        propertyValues.add("transactionAttributeSource", definition);
    }

    public static class JPluginInit implements EnvironmentAware{
        @Autowired
        private IObjectResolver springBeanResolver;
        private Environment environment;

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        public void init() {
            //add Jplugin config to spring environment
            if(environment instanceof ConfigurableEnvironment){
                final ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) this.environment;
                final MutablePropertySources propertySources = configurableEnvironment.getPropertySources();
                //add plantform config
                propertySources.addFirst(new JpluginConfigPropertySource("jplugin configs"));
            }
            PluginEnvirement.getInstance().addObjectResolver(springBeanResolver);
            PluginEnvirement.getInstance().startup();
        }

        public void stop(){
            PluginEnvirement.getInstance().stop();
        }
    }

    public static class SpringBeanResolver implements IObjectResolver ,BeanFactoryAware{

        private AutowireCapableBeanFactory beanFactory;
        public static final Set<Object> OBJECTS_RESOLVED = Collections.synchronizedSet(new HashSet<>());

        @Override
        public void resolve(Object obj) {
            //ignore aop proxy
            if(AopUtils.isAopProxy(obj)){
                return;
            }
            OBJECTS_RESOLVED.add(obj);
            this.beanFactory.autowireBean(obj);
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            if(beanFactory instanceof AutowireCapableBeanFactory){
                this.beanFactory = (AutowireCapableBeanFactory) beanFactory;
            }
        }

        /**
         * 是否被jplugin解析过
         * @param object
         * @return
         */
        public static boolean isJpluginResolved(Object object){
            return OBJECTS_RESOLVED.contains(object);
        }
    }

    private static class JpluginConfigPropertySource extends PropertySource<Map<String, Object>>{

        public JpluginConfigPropertySource(String name) {
            super(name, new HashMap());
        }

        @Override
        public boolean containsProperty(String name) {
            return this.getProperty(name) != null;
        }

        @Override
        public Object getProperty(String name) {
            return ConfigFactory.getStringConfig(name);
        }
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
