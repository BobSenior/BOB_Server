package com.bob_senior.bob_server.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProfileReqDto {
    String name;
    int age;
}
