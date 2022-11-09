package com.bob_senior.bob_server.domain.appointment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HandleRequestDTO {

    private Integer makerIdx;

    private Integer requesterIdx;

    private boolean accept;
}
