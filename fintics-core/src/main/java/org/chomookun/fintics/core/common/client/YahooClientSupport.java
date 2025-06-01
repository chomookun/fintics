package org.chomookun.fintics.core.common.client;

import org.springframework.http.HttpHeaders;

public interface YahooClientSupport extends ClientSupport {

    /**
     * Creates yahoo finance http headers
     * @return http headers
     */
    default HttpHeaders createYahooHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36");
        return headers;
    }

}
