package org.chomookun.fintics.web.view.profit;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.broker.model.Broker;
import org.chomookun.fintics.core.broker.model.BrokerSearch;
import org.chomookun.fintics.core.broker.BrokerService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("profits")
@PreAuthorize("hasAuthority('profits')")
@RequiredArgsConstructor
public class ProfitsController {

    private final BrokerService brokerService;

    @GetMapping
    public ModelAndView getProfits() {
        ModelAndView modelAndView = new ModelAndView("profit/profits.html");
        // brokers
        List<Broker> brokers = brokerService.getBrokers(BrokerSearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("brokers", brokers);
        // return
        return modelAndView;
    }

}
