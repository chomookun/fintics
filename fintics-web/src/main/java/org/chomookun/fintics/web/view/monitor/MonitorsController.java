package org.chomookun.fintics.web.view.monitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.core.trade.model.TradeSearch;
import org.chomookun.fintics.core.basket.BasketService;
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
@RequestMapping("monitors")
@PreAuthorize("hasAuthority('monitors')")
@RequiredArgsConstructor
@Slf4j
public class MonitorsController {

    private final TradeService tradeService;

    private final BasketService basketService;

    @GetMapping
    public ModelAndView getMonitor() {
        ModelAndView modelAndView = new ModelAndView("monitor/monitors.html");

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
