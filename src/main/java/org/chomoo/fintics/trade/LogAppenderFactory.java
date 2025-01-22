package org.chomoo.fintics.trade;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import ch.qos.logback.core.Context;

@Component
@RequiredArgsConstructor
public class LogAppenderFactory {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * gets object
     * @param context context
     * @param destination stomp messaging destination
     * @return log appender
     */
    public LogAppender getObject(Context context, String destination) {
        return LogAppender.builder()
                .context(context)
                .messagingTemplate(messagingTemplate)
                .destination(destination)
                .build();
    }

}
