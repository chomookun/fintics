package org.chomookun.fintics.web.view.order;

import lombok.RequiredArgsConstructor;
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

@Controller
@RequestMapping("orders")
@PreAuthorize("hasAuthority('orders')")
@RequiredArgsConstructor
public class OrdersController {

    private final TradeService tradeService;

    @GetMapping
    public ModelAndView getOrders() {
        ModelAndView modelAndView = new ModelAndView("order/orders.html");

        // baskets
        List<Trade> trades = tradeService.getTrades(TradeSearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("trades", trades);

        // return
        return modelAndView;
    }

}
