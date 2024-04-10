package org.oopscraft.fintics.view;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.model.Strategy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("strategy")
@PreAuthorize("hasAuthority('STRATEGIES')")
@RequiredArgsConstructor
public class StrategyController {

    @GetMapping
    public ModelAndView getStrategy(@RequestParam(value = "strategyId", required = false) String strategyId) {
        ModelAndView modelAndView = new ModelAndView("strategy.html");
        modelAndView.addObject("strategyId", strategyId);
        modelAndView.addObject("languages", Strategy.Language.values());
        return modelAndView;
    }

}
