package org.chomookun.fintics.view;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.client.broker.BrokerClientDefinition;
import org.chomookun.fintics.client.broker.BrokerClientDefinitionRegistry;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("brokers")
@PreAuthorize("hasAuthority('brokers')")
@RequiredArgsConstructor
public class BrokersController {

    private final BrokerClientDefinitionRegistry brokerClientDefinitionRegistry;

    @GetMapping
    public ModelAndView getStrategies() {
        ModelAndView modelAndView = new ModelAndView("brokers.html");
        List<BrokerClientDefinition> brokerClientDefinitions = brokerClientDefinitionRegistry.getBrokerClientDefinitions();
        modelAndView.addObject("brokerClientDefinitions", brokerClientDefinitions);
        return modelAndView;
    }

}
