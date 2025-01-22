package org.chomoo.fintics.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "Request payload for creating or updating a broker")
public class BrokerRequest {

    @Schema(description = "broker id", example = "test")
    private String brokerId;

    @Schema(description = "name", example = "test broker")
    private String name;

    @Schema(description = "broker client id", example = "test")
    private String brokerClientId;

    @Schema(description = "broker client properties", example = "name=value")
    private String brokerClientProperties;

}
