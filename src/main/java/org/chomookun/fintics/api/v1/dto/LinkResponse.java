package org.chomookun.fintics.api.v1.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LinkResponse {

    private String name;

    private String url;

    public static LinkResponse of(String name, String url) {
        return LinkResponse.builder()
                .name(name)
                .url(url)
                .build();
    }

}
