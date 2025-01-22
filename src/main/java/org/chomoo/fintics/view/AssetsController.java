package org.chomoo.fintics.view;

import lombok.RequiredArgsConstructor;
import org.chomoo.fintics.client.broker.BrokerClientDefinition;
import org.chomoo.fintics.client.broker.BrokerClientDefinitionRegistry;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("assets")
@PreAuthorize("hasAuthority('assets')")
@RequiredArgsConstructor
public class AssetsController {

    private final BrokerClientDefinitionRegistry brokerClientDefinitionRegistry;

    @GetMapping
    public ModelAndView getAssets() {
        ModelAndView modelAndView = new ModelAndView("assets.html");
        // markets
        List<String> markets = brokerClientDefinitionRegistry.getBrokerClientDefinitions().stream()
                .map(BrokerClientDefinition::getMarket)
                .distinct()
                .toList();
        modelAndView.addObject("markets", markets);
        return modelAndView;
    }

}
