package org.chomoo.fintics.client.broker.kis;

import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.util.TimeValue;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLException;
import java.io.EOFException;
import java.io.IOException;

public class KisHttpRequestRetryStrategy extends DefaultHttpRequestRetryStrategy {

    private final static int RETRY_COUNT = 3;

    /**
     * constructor
     */
    public KisHttpRequestRetryStrategy() {
        super(RETRY_COUNT,  TimeValue.ofMilliseconds(100L));
    }

    @Override
    public boolean retryRequest(HttpRequest request, IOException exception, int execCount, org.apache.hc.core5.http.protocol.HttpContext context) {
        // 설정한 retry 횟수를 초과 하면 중단
        if (execCount >  RETRY_COUNT) {
            return false;
        }

        // SSLException 발생 시에도 재시도 수행
        // 한국투자증권 서버가 Legacy 웹서버(JEUSE) 이고
        // java.io.EOFException SSL peer shut down incorrectly
        // 가 자주 발생 함.
        if (exception instanceof SSLException || exception instanceof EOFException) {
            return true;
        }

        // 기본 적인 retry 조건을 유지
        return super.retryRequest(request, exception, execCount, context);
    }

}
