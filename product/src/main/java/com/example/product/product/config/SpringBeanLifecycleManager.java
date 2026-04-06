package com.example.product.product.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * COMPLETE SPRING BEAN LIFECYCLE DEMO IN ONE FILE
 * 
 * BEAN LIFECYCLE ORDER:
 * 1. Constructor (Instantiation)
 * 2. @PostConstruct (dependencies injected)
 * 3. InitializingBean.afterPropertiesSet()
 * 4. Custom init-method
 * 5. BEAN IN USE
 * 6. @PreDestroy (shutdown)
 * 7. DisposableBean.destroy()
 * 8. Custom destroy-method
 */
@Component
public class SpringBeanLifecycleManager implements InitializingBean, DisposableBean, BeanFactoryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SpringBeanLifecycleManager.class);
    private String status = "CREATED";

    // ============================================
    // BEAN LIFECYCLE METHODS (Init Phase)
    // ============================================

    /**
     * 1. Constructor - Bean Instantiation
     */
    public SpringBeanLifecycleManager() {
        logger.info("\n[1] Constructor - Bean instantiated");
        this.status = "INSTANTIATED";
    }

    /**
     * 2. @PostConstruct - After Dependencies Injected
     */
    @PostConstruct
    public void postConstruct() {
        logger.info("[2] @PostConstruct - Dependencies injected ✓");
        this.status = "POST_CONSTRUCT";
    }

    /**
     * 3. InitializingBean.afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("[3] InitializingBean.afterPropertiesSet()");
        this.status = "AFTER_PROPERTIES";
    }

    /**
     * 4. Custom init-method (declared via @Bean(initMethod="customInit"))
     */
    public void customInit() {
        logger.info("[4] Custom init-method");
        this.status = "READY";
        logger.info("✅ BEAN READY FOR USE\n");
    }

    /**
     * 5. Bean in Use - Application calls methods
     */
    public String getStatus() {
        logger.info("[5] Bean in use - Status: {}", status);
        return status;
    }

    // ============================================
    // BEAN LIFECYCLE METHODS (Destroy Phase)
    // ============================================

    /**
     * 6. @PreDestroy - Before Shutdown
     */
    @PreDestroy
    public void preDestroy() {
        logger.warn("[6] @PreDestroy - Shutdown initiated");
        this.status = "PRE_DESTROY";
    }

    /**
     * 7. DisposableBean.destroy()
     */
    @Override
    public void destroy() throws Exception {
        logger.warn("[7] DisposableBean.destroy()");
        this.status = "DESTROYING";
    }

    /**
     * 8. Custom destroy-method
     */
    public void customDestroy() {
        logger.warn("[8] Custom destroy-method");
        this.status = "DESTROYED";
        logger.warn("✅ BEAN DESTROYED\n");
    }

    // ============================================
    // BEANFACTORYPOSTPROCESSOR
    // ============================================

    /**
     * Runs BEFORE beans are instantiated
     * Modify bean definitions at startup
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        logger.info("\n╔════════════════════════════════════════╗");
        logger.info("║   BeanFactoryPostProcessor Execution   ║");
        logger.info("╚════════════════════════════════════════╝");
        logger.info("Runs BEFORE any bean is instantiated\n");
        logger.info("Total beans defined: {}", beanFactory.getBeanDefinitionCount());
        logger.info("You can modify bean definitions here\n");
    }

    // ============================================
    // APPLICATIONCONTEXT & BEANFACTORY DEMO
    // ============================================

    public void demonstrateApplicationContext(ApplicationContext context) {
        logger.info("╔════════════════════════════════════════╗");
        logger.info("║       ApplicationContext Features       ║");
        logger.info("╚════════════════════════════════════════╝");
        logger.info("Type: {}", context.getClass().getSimpleName());
        logger.info("Total beans: {}", context.getBeanDefinitionCount());
        logger.info("Display name: {}", context.getDisplayName());
        logger.info("✓ Supports: AOP, Events, i18n, Resources\n");
    }

    public void demonstrateBeanFactory(BeanFactory factory) {
        logger.info("╔════════════════════════════════════════╗");
        logger.info("║       BeanFactory Features             ║");
        logger.info("╚════════════════════════════════════════╝");
        logger.info("Type: {}", factory.getClass().getSimpleName());
        logger.info("✓ Get beans by name or type");
        logger.info("✓ Check if singleton/prototype");
        logger.info("✓ Check bean existence\n");
    }

    // ============================================
    // LIFECYCLE SUMMARY
    // ============================================

    public void displayLifecycleFlow() {
        logger.info("\n");
        logger.info("╔════════════════════════════════════════╗");
        logger.info("║      SPRING BEAN LIFECYCLE FLOW        ║");
        logger.info("╠════════════════════════════════════════╣");
        logger.info("║                                        ║");
        logger.info("║  INITIALIZATION PHASE:                 ║");
        logger.info("║  1. BeanFactoryPostProcessor runs      ║");
        logger.info("║  2. Constructor called                 ║");
        logger.info("║  3. @PostConstruct invoked             ║");
        logger.info("║  4. InitializingBean.afterProperties   ║");
        logger.info("║  5. Custom init-method                 ║");
        logger.info("║  6. ✅ BEAN READY FOR USE              ║");
        logger.info("║                                        ║");
        logger.info("║  APPLICATION RUNNING...                ║");
        logger.info("║  (Your code uses the bean)             ║");
        logger.info("║                                        ║");
        logger.info("║  DESTRUCTION PHASE:                    ║");
        logger.info("║  7. @PreDestroy invoked                ║");
        logger.info("║  8. DisposableBean.destroy()           ║");
        logger.info("║  9. Custom destroy-method              ║");
        logger.info("║  10. ✅ BEAN DESTROYED                 ║");
        logger.info("║                                        ║");
        logger.info("╚════════════════════════════════════════╝");
        logger.info("\n");
    }
}
