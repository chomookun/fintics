package org.chomookun.fintics.web.view.monitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.core.trade.model.TradeSearch;
import org.chomookun.fintics.core.trade.TradeService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("monitor")
@PreAuthorize("hasAuthority('monitor')")
@RequiredArgsConstructor
@Slf4j
public class MonitorController {

    private final TradeService tradeService;

    @GetMapping
    public ModelAndView getMonitors() {
        ModelAndView modelAndView = new ModelAndView("monitor/monitor");
        // trades
        List<Trade> trades = tradeService.getTrades(TradeSearch.builder().build(), Pageable.unpaged())
                .getContent()
                .stream()
                .filter(Trade::isEnabled)
                .collect(Collectors.toList());
        modelAndView.addObject("trades", trades);
        // return
        return modelAndView;
    }

}
