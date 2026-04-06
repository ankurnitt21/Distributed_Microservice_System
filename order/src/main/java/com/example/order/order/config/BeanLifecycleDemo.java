package com.example.order.order.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Comprehensive demonstration of Spring Bean Lifecycle
 * 
 * Bean Lifecycle Sequence:
 * 1. Bean Instantiation (Constructor)
 * 2. Dependencies Injection (Setter/Constructor)
 * 3. BeanNameAware.setBeanName()
 * 4. BeanFactoryAware.setBeanFactory()
 * 5. ApplicationContextAware.setApplicationContext()
 * 6. BeanPostProcessor.postProcessBeforeInitialization()
 * 7. @PostConstruct / InitializingBean.afterPropertiesSet()
 * 8. BeanPostProcessor.postProcessAfterInitialization()
 * 9. [Bean ready for use]
 * 10. @PreDestroy / DisposableBean.destroy() [on shutdown]
 */

// ==================== LIFECYCLE BEAN ====================
/**
 * Sample bean demonstrating all lifecycle methods
 */
@Component
class OrderServiceLifecycleBean implements BeanNameAware, BeanFactoryAware, InitializingBean, DisposableBean {
    
    private static final Logger log = LoggerFactory.getLogger(OrderServiceLifecycleBean.class);
    
    private String beanName;
    private BeanFactory beanFactory;
    private ApplicationContext applicationContext;
    private String serviceId;
    
    // ===== 1. Constructor Injection =====
    public OrderServiceLifecycleBean() {
        log.info("➊ [CONSTRUCTOR] OrderServiceLifecycleBean instance created");
    }
    
    // ===== 2. Setter Injection / Properties Set =====
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
        log.info("➋ [SETTER] setServiceId called: {}", serviceId);
    }
    
    // ===== 3. BeanNameAware.setBeanName() =====
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        log.info("➌ [BeanNameAware] Bean name set: {}", name);
    }
    
    // ===== 4. BeanFactoryAware.setBeanFactory() =====
    @Override
    public void setBeanFactory(BeanFactory factory) throws BeansException {
        this.beanFactory = factory;
        log.info("➍ [BeanFactoryAware] BeanFactory injected");
        
        // Access other beans from BeanFactory
        if (factory.containsBean("appConfig")) {
            log.info("   └─ Found AppConfig bean in factory");
        }
    }
    
    // ===== 6 & 8. BeanPostProcessor hooks (handled by framework) =====
    // Handled by @Bean method below
    
    // ===== 7. @PostConstruct / InitializingBean.afterPropertiesSet() =====
    @PostConstruct
    public void postConstruct() {
        log.info("➏ [@PostConstruct] Initialization started");
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("➐ [InitializingBean] afterPropertiesSet - Expensive operations here");
        
        // Simulate expensive initialization (DB connection, API call, etc.)
        Thread.sleep(500);  // Simulated work
        
        log.info("   └─ Service initialized with ID: {}", serviceId);
        log.info("➇ [READY] Bean fully initialized and ready for use!");
    }
    
    // ===== 10. @PreDestroy / DisposableBean.destroy() =====
    @PreDestroy
    public void preDestroy() {
        log.info("⓾ [@PreDestroy] Starting cleanup");
    }
    
    @Override
    public void destroy() throws Exception {
        log.info("⓫ [DisposableBean] destroy() - Cleanup resources");
        log.warn("   └─ Bean {} is being destroyed", beanName);
    }
    
    // ===== Demonstration methods =====
    public void demonstrateBeanFactory() {
        if (beanFactory != null) {
            log.info("\n=== BeanFactory Usage ===");
            log.info("BeanFactory type: {}", beanFactory.getClass().getSimpleName());
            
            // Get bean by name
            Object appConfig = beanFactory.getBean("appConfig");
            log.info("Retrieved bean 'appConfig': {}", appConfig.getClass().getSimpleName());
        }
    }
    
    public void demonstrateApplicationContext() {
        if (applicationContext != null) {
            log.info("\n=== ApplicationContext Usage ===");
            log.info("ApplicationContext type: {}", applicationContext.getClass().getSimpleName());
            
            // List all bean names
            String[] beanNames = applicationContext.getBeanDefinitionNames();
            log.info("Total beans: {}", beanNames.length);
            log.info("First 5 beans: {}", java.util.Arrays.toString(
                java.util.Arrays.copyOf(beanNames, Math.min(5, beanNames.length))
            ));
            
            // Get beans by type
            java.util.Map<String, OrderServiceLifecycleBean> beansOfType = 
                applicationContext.getBeansOfType(OrderServiceLifecycleBean.class);
            log.info("Beans of type OrderServiceLifecycleBean: {}", beansOfType.size());
        }
    }
}

// ==================== CUSTOM BEANFACTORYPOSTPROCESSOR ====================
/**
 * Custom BeanFactoryPostProcessor for modifying bean definitions
 * Executes BEFORE any beans are instantiated
 */
@Configuration
class CustomBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(CustomBeanFactoryPostProcessor.class);
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) 
            throws BeansException {
        
        log.info("\n╔════════════════════════════════════════════╗");
        log.info("║ BeanFactoryPostProcessor Execution          ║");
        log.info("║ (Before any bean instantiation)             ║");
        log.info("╚════════════════════════════════════════════╝");
        
        // Get all bean definitions
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        log.info("Total bean definitions found: {}", beanNames.length);
        
        // Iterate through bean definitions and modify them
        int modifiedCount = 0;
        for (String beanName : beanNames) {
            if (beanName.equals("orderServiceLifecycleBean")) {
                log.info("\n🔧 Modifying bean definition: {}", beanName);
                log.info("   └─ This happens BEFORE the bean is instantiated");
                modifiedCount++;
            }
        }
        
        log.info("\n✅ BeanFactoryPostProcessor completed");
        log.info("   └─ Total beans processed: {}", beanNames.length);
        log.info("   └─ Beans modified: {}", modifiedCount);
    }
}

// ==================== CUSTOM BEAN POST PROCESSOR ====================
/**
 * Custom BeanPostProcessor for intercepting bean initialization
 * Executes AFTER bean instantiation but within lifecycle
 */
@Component
class CustomBeanPostProcessor implements org.springframework.beans.factory.config.BeanPostProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(CustomBeanPostProcessor.class);
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) 
            throws BeansException {
        if (beanName.equals("orderServiceLifecycleBean")) {
            log.info("➅ [BeanPostProcessor.preInitialization] Before @PostConstruct");
        }
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) 
            throws BeansException {
        if (beanName.equals("orderServiceLifecycleBean")) {
            log.info("➈ [BeanPostProcessor.postInitialization] After InitializingBean");
        }
        return bean;
    }
}

// ==================== BEAN LIFECYCLE VIEWER ====================
/**
 * Component that demonstrates how to interact with ApplicationContext and BeanFactory
 */
@Component
class BeanLifecycleViewer {
    
    private static final Logger log = LoggerFactory.getLogger(BeanLifecycleViewer.class);
    
    private final ApplicationContext applicationContext;
    private final OrderServiceLifecycleBean lifecycleBean;
    
    public BeanLifecycleViewer(ApplicationContext applicationContext, 
                              OrderServiceLifecycleBean lifecycleBean) {
        this.applicationContext = applicationContext;
        this.lifecycleBean = lifecycleBean;
        
        // Demonstrate bean access
        demonstrateFullLifecycle();
    }
    
    private void demonstrateFullLifecycle() {
        log.info("\n" + "═".repeat(60));
        log.info("              BEAN LIFECYCLE DEMONSTRATION");
        log.info("═".repeat(60));
        
        // ApplicationContext demonstration
        log.info("\n[ApplicationContext] - High-level context interface");
        log.info("├─ getBean(name): Get bean by name");
        log.info("├─ getBeansOfType(type): Get all beans of a type");
        log.info("├─ getBeanDefinitionNames(): Get all bean names");
        log.info("├─ getEnvironment(): Access properties");
        log.info("└─ publishEvent(event): Publish events");
        
        // BeanFactory demonstration
        log.info("\n[BeanFactory] - Low-level bean container");
        log.info("├─ getBean(name): Get bean by name");
        log.info("├─ containsBean(name): Check if bean exists");
        log.info("├─ getType(name): Get bean class type");
        log.info("└─ isSingleton(name): Check if singleton");
        
        // Call lifecycle bean methods
        lifecycleBean.demonstrateBeanFactory();
        lifecycleBean.demonstrateApplicationContext();
        
        log.info("\n" + "═".repeat(60));
        log.info("           LIFECYCLE SEQUENCE COMPLETED");
        log.info("═".repeat(60) + "\n");
    }
}
