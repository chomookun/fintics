package org.chomookun.fintics.trade;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Context;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Slf4j
public class LogAppender extends AppenderBase<ILoggingEvent> {

    private final PatternLayout layout;

    private final SimpMessagingTemplate messagingTemplate;

    private final String destination;

    @Builder
    private LogAppender(Context context, SimpMessagingTemplate messagingTemplate, String destination) {
        this.messagingTemplate = messagingTemplate;
        this.destination = destination;
        layout = new PatternLayout();
        layout.setPattern("%d{yyyy-MM-dd HH:mm:ss} %-5level [%.-12thread] - %msg");
        layout.setContext(context);
        layout.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        String logMessage = layout.doLayout(event);
        messagingTemplate.convertAndSend(destination, logMessage);
    }

}
