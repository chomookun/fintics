package org.chomookun.fintics.web.view.trade;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.basket.model.BasketSearch;
import org.chomookun.fintics.core.broker.model.Broker;
import org.chomookun.fintics.core.broker.model.BrokerSearch;
import org.chomookun.fintics.core.basket.BasketService;
import org.chomookun.fintics.core.broker.BrokerService;
import org.chomookun.fintics.core.strategy.StrategyService;
import org.chomookun.fintics.core.strategy.model.Strategy;
import org.chomookun.fintics.core.strategy.model.StrategySearch;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("trades")
@PreAuthorize("hasAuthority('trades')")
@RequiredArgsConstructor
public class TradesController {

    private final BrokerService brokerService;

    private final BasketService basketService;

    private final StrategyService strategyService;

    @GetMapping
    public ModelAndView getTrades() {
        ModelAndView modelAndView = new ModelAndView("trade/trades.html");

        // brokers
        List<Broker> brokers =  brokerService.getBrokers(BrokerSearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("brokers", brokers);

        // baskets
        List<Basket> baskets = basketService.getBaskets(BasketSearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("baskets", baskets);

        // strategies
        List<Strategy> strategies = strategyService.getStrategies(StrategySearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("strategies", strategies);

        // return
        return modelAndView;
    }

}
