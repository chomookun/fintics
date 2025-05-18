package org.chomookun.fintics.core.strategy.converter;

import jakarta.persistence.Converter;
import org.chomookun.arch4j.core.common.data.converter.GenericObjectConverter;
import org.chomookun.fintics.core.strategy.runner.StrategyResult;

@Converter
public class StrategyResultConverter extends GenericObjectConverter<StrategyResult> {

    protected StrategyResultConverter() {
        super(StrategyResult.class);
    }

}