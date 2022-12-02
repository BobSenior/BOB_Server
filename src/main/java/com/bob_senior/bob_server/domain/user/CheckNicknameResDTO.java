package com.bob_senior.bob_server.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CheckNicknameResDTO {
    private Boolean isDuplicated;
    private String resultMessage;
}
