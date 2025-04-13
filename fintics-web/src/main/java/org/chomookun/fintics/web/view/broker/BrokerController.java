package org.chomookun.fintics.web.view.broker;

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
@RequestMapping("broker")
@PreAuthorize("hasAuthority('broker')")
@RequiredArgsConstructor
public class BrokerController {

    private final BrokerClientDefinitionRegistry brokerClientDefinitionRegistry;

    @GetMapping
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("broker/broker");
        List<BrokerClientDefinition> brokerClientDefinitions = brokerClientDefinitionRegistry.getBrokerClientDefinitions();
        modelAndView.addObject("brokerClientDefinitions", brokerClientDefinitions);
        return modelAndView;
    }

}
