package com.problemfighter.pfspring.restapi.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class RestSpringContext implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext _applicationContext) throws BeansException {
        applicationContext = _applicationContext;
    }

    public static <T> T getBean(Class<T> klass) {
        return (T)(applicationContext != null ? applicationContext.getBean(klass) : null);
    }

    public static Environment environment() {
        return (Environment)getBean(Environment.class);
    }
}
