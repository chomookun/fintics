package org.chomookun.fintics.core.broker.client.kis;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Kis access token object
 */
@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class KisAccessToken {

    @EqualsAndHashCode.Include
    private String apiUrl;

    @EqualsAndHashCode.Include
    private String appKey;

    @EqualsAndHashCode.Include
    private String appSecret;

    private String accessToken;

    @Setter
    private LocalDateTime expireDateTime;

    /**
     * Checks access toke is expired
     * @return whether token is expired or not
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireDateTime);
    }

}
