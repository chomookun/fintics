package org.chomookun.fintics.view;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.client.broker.BrokerClientDefinition;
import org.chomookun.fintics.client.broker.BrokerClientDefinitionRegistry;
import org.chomookun.fintics.service.TradeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("data")
@PreAuthorize("hasAuthority('data')")
@RequiredArgsConstructor
public class DataController {

    private final TradeService tradeService;

    private final BrokerClientDefinitionRegistry brokerClientDefinitionRegistry;

    @GetMapping
    public ModelAndView getData() {
        ModelAndView modelAndView = new ModelAndView("data.html");
        // markets
        List<String> markets = brokerClientDefinitionRegistry.getBrokerClientDefinitions().stream()
                .map(BrokerClientDefinition::getMarket)
                .distinct()
                .toList();
        modelAndView.addObject("markets", markets);
        // return
        return modelAndView;
    }

}
