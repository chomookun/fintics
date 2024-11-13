package org.oopscraft.fintics.client.broker.kis;

import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLException;
import java.io.EOFException;
import java.io.IOException;

public class KisHttpRequestRetryHandler extends StandardHttpRequestRetryHandler {

    public KisHttpRequestRetryHandler(int retryCount) {
        super(retryCount, false);
    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        // 설정한 retry 횟수를 초과 하면 중단
        if (executionCount > this.getRetryCount()) {
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
        return super.retryRequest(exception, executionCount, context);
    }

}
