
package com.uade.tpo.Scrims.model.patterns.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MatchmakingStrategyFactory {

    @Autowired
    private ApplicationContext context;

    /**
     * Retorna "MANUAL" como estrategia por defecto si el nombre no existe.
     */
    public MatchmakingStrategy getStrategy(String strategyName) {
        if (strategyName == null || !context.containsBean(strategyName)) {
            return (MatchmakingStrategy) context.getBean("MANUAL");
        }
        return (MatchmakingStrategy) context.getBean(strategyName);
    }
}