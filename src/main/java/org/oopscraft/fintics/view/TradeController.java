package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.alarm.Alarm;
import org.oopscraft.arch4j.core.alarm.AlarmSearch;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.oopscraft.fintics.model.Order;
import org.oopscraft.fintics.service.TradeService;
import org.oopscraft.fintics.trade.order.OrderOperatorFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("trade")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TRADE')")
public class TradeController {

    private final TradeService tradeService;

    private final AlarmService alarmService;

    @GetMapping
    public ModelAndView trade() {
        return new ModelAndView("trade.html");
    }

    @GetMapping("trade-detail")
    public ModelAndView tradeDetail(@RequestParam(value= "tradeId", required = false) String tradeId) {
        ModelAndView modelAndView = new ModelAndView("trade-detail.html");
        modelAndView.addObject("tradeId", tradeId);
        modelAndView.addObject("brokers", BrokerClientFactory.getBrokerClientDefinitions());
        modelAndView.addObject("orderOperators", OrderOperatorFactory.getOrderOperatorDefinitions());
        List<Alarm> alarms = alarmService.getAlarms(AlarmSearch.builder().build(), Pageable.unpaged()).getContent();
        modelAndView.addObject("alarms", alarms);
        modelAndView.addObject("orderKinds", Order.Kind.values());
        return modelAndView;
    }

}
