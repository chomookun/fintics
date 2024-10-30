package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.model.BasketSearch;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeSearch;
import org.oopscraft.fintics.service.BasketService;
import org.oopscraft.fintics.service.TradeService;
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
public class MonitorsController {

    private final TradeService tradeService;

    private final BasketService basketService;

    @GetMapping
    public ModelAndView getMonitor() {
        ModelAndView modelAndView = new ModelAndView("monitors.html");

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
