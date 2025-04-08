package org.chomookun.fintics.web.view.strategy;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.strategy.model.Strategy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("strategy")
@PreAuthorize("hasAuthority('strategy')")
@RequiredArgsConstructor
public class StrategyController {

    @GetMapping
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("strategy/strategy");
        modelAndView.addObject("languages", Strategy.Language.values());
        return modelAndView;
    }

    @GetMapping("detail")
    public ModelAndView detail(@RequestParam(value = "strategyId", required = false) String strategyId) {
        ModelAndView modelAndView = new ModelAndView("strategy/strategy-detail");
        modelAndView.addObject("strategyId", strategyId);
        modelAndView.addObject("languages", Strategy.Language.values());
        return modelAndView;
    }

}
