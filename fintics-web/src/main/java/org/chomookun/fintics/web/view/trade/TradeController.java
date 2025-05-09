package org.chomookun.fintics.web.view.trade;

import lombok.RequiredArgsConstructor;
import org.chomookun.arch4j.core.notification.NotifierService;
import org.chomookun.arch4j.core.notification.model.Notifier;
import org.chomookun.arch4j.core.notification.model.NotifierSearch;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.basket.model.BasketSearch;
import org.chomookun.fintics.core.broker.client.BrokerClientDefinition;
import org.chomookun.fintics.core.broker.client.BrokerClientDefinitionRegistry;
import org.chomookun.fintics.core.broker.model.Broker;
import org.chomookun.fintics.core.broker.model.BrokerSearch;
import org.chomookun.fintics.core.basket.BasketService;
import org.chomookun.fintics.core.broker.BrokerService;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.strategy.StrategyService;
import org.chomookun.fintics.core.strategy.model.Strategy;
import org.chomookun.fintics.core.strategy.model.StrategySearch;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("trade")
@PreAuthorize("hasAuthority('trade')")
@RequiredArgsConstructor
public class TradeController {

    private final BrokerService brokerService;

    private final BasketService basketService;

    private final StrategyService strategyService;

    private final NotifierService notifierService;

    private final BrokerClientDefinitionRegistry brokerClientDefinitionRegistry;

    @GetMapping
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("trade/trade");
        List<Broker> brokers =  brokerService.getBrokers(BrokerSearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("brokers", brokers);
        List<Basket> baskets = basketService.getBaskets(BasketSearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("baskets", baskets);
        List<Strategy> strategies = strategyService.getStrategies(StrategySearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("strategies", strategies);
        return modelAndView;
    }

    @GetMapping("detail")
    public ModelAndView detail(@RequestParam(value="tradeId", required = false) String tradeId) {
        ModelAndView modelAndView = new ModelAndView("trade/trade-detail");
        modelAndView.addObject("tradeId", tradeId);
        List<Notifier> notifiers = notifierService.getNotifiers(NotifierSearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("notifiers", notifiers);
        List<Broker> brokers =  brokerService.getBrokers(BrokerSearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("brokers", brokers);
        List<BrokerClientDefinition> brokerClientDefinitions = brokerClientDefinitionRegistry.getBrokerClientDefinitions();
        modelAndView.addObject("brokerClientDefinitions", brokerClientDefinitions);
        List<Basket> baskets = basketService.getBaskets(BasketSearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("baskets", baskets);
        List<Strategy> strategies = strategyService.getStrategies(StrategySearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("strategies", strategies);
        modelAndView.addObject("orderKinds", Order.Kind.values());
        // markets
        List<String> markets = brokerClientDefinitionRegistry.getBrokerClientDefinitions().stream()
                .map(BrokerClientDefinition::getMarket)
                .distinct()
                .toList();
        modelAndView.addObject("markets", markets);
        return modelAndView;
    }

}
