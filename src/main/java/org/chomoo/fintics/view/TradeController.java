package org.chomoo.fintics.view;

import lombok.RequiredArgsConstructor;
import org.chomoo.arch4j.core.alarm.model.Alarm;
import org.chomoo.arch4j.core.alarm.model.AlarmSearch;
import org.chomoo.arch4j.core.alarm.service.AlarmService;
import org.chomoo.fintics.client.broker.BrokerClientDefinition;
import org.chomoo.fintics.client.broker.BrokerClientDefinitionRegistry;
import org.chomoo.fintics.model.*;
import org.chomoo.fintics.service.BasketService;
import org.chomoo.fintics.service.BrokerService;
import org.chomoo.fintics.service.StrategyService;
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

    private final AlarmService alarmService;

    private final BrokerService brokerService;

    private final BasketService basketService;

    private final StrategyService strategyService;

    private final BrokerClientDefinitionRegistry brokerClientDefinitionRegistry;

    @GetMapping
    public ModelAndView getTrade(@RequestParam(value="tradeId", required = false) String tradeId) {
        ModelAndView modelAndView = new ModelAndView("trade.html");
        modelAndView.addObject("tradeId", tradeId);
        List<Alarm> alarms = alarmService.getAlarms(AlarmSearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("alarms", alarms);
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
