package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.BrokerService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("realized-profits")
@PreAuthorize("hasAuthority('realized-profits')")
@RequiredArgsConstructor
public class RealizedProfitsController {

    private final BrokerService brokerService;

    @GetMapping
    public ModelAndView getStatistics() {
        ModelAndView modelAndView = new ModelAndView("realized-profits.html");
        // brokers
        List<Broker> brokers = brokerService.getBrokers(BrokerSearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("brokers", brokers);
        // return
        return modelAndView;
    }

}
