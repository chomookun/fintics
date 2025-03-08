package org.chomookun.fintics.web.api.v1.basket.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class BasketDividerRequest {

    private String basketId;

    private String dividerId;

    private Integer sort;

    private String name;

}
