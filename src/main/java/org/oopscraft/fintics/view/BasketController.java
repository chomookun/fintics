package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.client.broker.BrokerClientDefinition;
import org.oopscraft.fintics.client.broker.BrokerClientDefinitionRegistry;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.model.Strategy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("basket")
@PreAuthorize("hasAuthority('BASKETS')")
@RequiredArgsConstructor
public class BasketController {

    private final BrokerClientDefinitionRegistry brokerClientDefinitionRegistry;

    @GetMapping
    public ModelAndView getBasket(@RequestParam(value = "basketId", required = false) String basketId) {
        ModelAndView modelAndView = new ModelAndView("basket.html");
        modelAndView.addObject("basketId", basketId);
        // markets
        List<String> markets = brokerClientDefinitionRegistry.getBrokerClientDefinitions().stream()
                .map(BrokerClientDefinition::getMarket)
                .distinct()
                .toList();
        modelAndView.addObject("markets", markets);
        // languages
        modelAndView.addObject("languages", Basket.Language.values());
        // return
        return modelAndView;
    }

}
