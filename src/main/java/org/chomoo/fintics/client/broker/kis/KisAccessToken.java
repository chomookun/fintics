package org.chomoo.fintics.client.broker.kis;

import lombok.*;

import java.time.LocalDateTime;

/**
 * access token object
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

    private LocalDateTime expireDateTime;

    /**
     * checks access toke is expired
     * @return whether token is expired or noot
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireDateTime);
    }

}
