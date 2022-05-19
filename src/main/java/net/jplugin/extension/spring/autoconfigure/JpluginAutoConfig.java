package net.jplugin.extension.spring.autoconfigure;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Administrator on 2022/5/13.
 */
@Configuration(proxyBeanMethods = false)
public class JpluginAutoConfig {

    @Bean
    public BeanPostProcessor jpluginRefInstantiationAwareBeanPostProcessor(){
        return new JpluginRefInstantiationAwareBeanPostProcessor();
    }

    @Bean
    public BeanDefinitionRegistryPostProcessor jpluginBeanDefinitionRegistryPostProcessor(){
        return new JpluginBeanDefinitionRegistryPostProcessor();
    }
}
