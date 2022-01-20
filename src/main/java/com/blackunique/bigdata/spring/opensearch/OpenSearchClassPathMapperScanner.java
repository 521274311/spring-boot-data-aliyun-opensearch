package com.blackunique.bigdata.spring.opensearch;

import club.kingon.sql.builder.spring.annotation.Mapper;
import com.blackunique.bigdata.spring.opensearch.filter.OpenSearchBaseMapperTypeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Arrays;
import java.util.Set;

/**
 * @author dragons
 * @date 2021/12/14 10:05
 */
public class OpenSearchClassPathMapperScanner extends ClassPathBeanDefinitionScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenSearchClassPathMapperScanner.class);

    private Class<? extends OpenSearchMapperFactoryBean> mapperFactoryBeanClass = OpenSearchMapperFactoryBean.class;

    private String dataSourceConfigPrefix;


    public OpenSearchClassPathMapperScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    public void registerFilters() {

        addIncludeFilter(new AnnotationTypeFilter(Mapper.class));
        addIncludeFilter(new OpenSearchBaseMapperTypeFilter());

        // exclude package-info.java
        addExcludeFilter((metadataReader, metadataReaderFactory) -> {
            String className = metadataReader.getClassMetadata().getClassName();
            return className.endsWith("package-info");
        });
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            LOGGER.warn("No MyBatis mapper was found in '" + Arrays.toString(basePackages)
                + "' package. Please check your configuration.");
        } else {
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();
            String beanClassName = definition.getBeanClassName();
            LOGGER.debug("Creating OpenSearchMapperFactoryBean with name '" + holder.getBeanName() + "' and '" + beanClassName
                + "' mapperInterface");

            // the mapper interface is the original class of the bean
            // but, the actual class of the bean is OpenSearchMapperFactoryBean
            definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName); // issue #59
            definition.setBeanClass(this.mapperFactoryBeanClass);

            definition.getPropertyValues().add("dataSourceConfigPrefix", this.dataSourceConfigPrefix);


            LOGGER.debug("Enabling autowire by type for OpenSearchMapperFactoryBean with name '" + holder.getBeanName() + "'.");
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        }
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
        if (super.checkCandidate(beanName, beanDefinition)) {
            return true;
        } else {
            LOGGER.warn("Skipping OpenSearchMapperFactoryBean with name '" + beanName + "' and '"
                + beanDefinition.getBeanClassName() + "' mapperInterface" + ". Bean already defined with the same name!");
            return false;
        }
    }

    public void setDataSourceConfigPrefix(String dataSourceConfigPrefix) {
        this.dataSourceConfigPrefix = dataSourceConfigPrefix;
    }
}
