package com.bob_senior.bob_server.domain.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmailAuthRequestDTO {
    String email;
    String authToken;
}
