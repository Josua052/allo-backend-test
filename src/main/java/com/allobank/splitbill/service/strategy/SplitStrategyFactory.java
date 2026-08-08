package com.allobank.splitbill.service.strategy;

import com.allobank.splitbill.model.SplitStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Factory pattern leveraging Spring Boot auto-discovery
@Component
public class SplitStrategyFactory {

    private final Map<SplitStrategy, SplitCalculationStrategy> strategies;

    public SplitStrategyFactory(List<SplitCalculationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(SplitCalculationStrategy::getStrategyType, strategy -> strategy));
    }

    public SplitCalculationStrategy getStrategy(SplitStrategy type) {
        SplitCalculationStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported split strategy: " + type);
        }
        return strategy;
    }
}
