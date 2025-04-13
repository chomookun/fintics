package org.chomookun.fintics.web.view.asset;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.broker.client.BrokerClientDefinition;
import org.chomookun.fintics.core.broker.client.BrokerClientDefinitionRegistry;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("asset")
@PreAuthorize("hasAuthority('asset')")
@RequiredArgsConstructor
public class AssetController {

    private final BrokerClientDefinitionRegistry brokerClientDefinitionRegistry;

    @GetMapping
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("asset/asset");
        // markets
        List<String> markets = brokerClientDefinitionRegistry.getBrokerClientDefinitions().stream()
                .map(BrokerClientDefinition::getMarket)
                .distinct()
                .toList();
        modelAndView.addObject("markets", markets);
        return modelAndView;
    }

}