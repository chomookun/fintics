package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.alarm.Alarm;
import org.oopscraft.arch4j.core.alarm.AlarmSearch;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.model.Order;
import org.oopscraft.fintics.model.Simulate;
import org.oopscraft.fintics.model.broker.BrokerClientDefinitionRegistry;
import org.oopscraft.fintics.service.IndiceService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("trades")
@PreAuthorize("hasAuthority('TRADES')")
@RequiredArgsConstructor
public class TradesController {

    private final AlarmService alarmService;

    private final IndiceService indiceService;

    @GetMapping
    public ModelAndView getTrades() {
        return new ModelAndView("trades.html");
    }

}
