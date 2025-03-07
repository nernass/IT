package com.example.examplerest.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggingAspect {

    /*
    * PointCut that matches all repositories, services andWeb REST endpoints
    * */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void springBeanPointcut() {
        //Method is empty as this is just a PointCut, the implementations are in the advices.
    }

    @After("springBeanPointcut()")
    public void afterEndpoints(JoinPoint joinPoint) {
        String kind = joinPoint.getKind();
        System.out.println(kind + "test log");
    }

    @Before("springBeanPointcut()")
    public void beforeEndpoints(JoinPoint joinPoint) {
        System.out.println("test log");
    }
}
