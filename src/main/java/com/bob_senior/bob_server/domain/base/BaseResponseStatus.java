package com.bob_senior.bob_server.domain.base;


import lombok.Getter;

@Getter
public enum BaseResponseStatus {
    SUCCESS(true,1000,"요청에 성공하였습니다"),


    /**
     * Error
     */
    INVALID_USER(false,2001,"유효하지 않은 사용자입니다!"),
    ALREADY_PARTICIPATED_IN_ROOM(false,2002,"이미 참가중인 채팅방입니다!"),
    INVALID_CHATROOM_ACCESS(false,2003,"해당 채팅방에 접근할 수 없습니다!"),
    ALREADY_VOTED(false,2004,"이미 참여한 투표입니다");


    private final boolean isSuccess;
    private final int code;
    private final String message;

    BaseResponseStatus(boolean isSuccess, int code, String message) { //BaseResponseStatus 에서 각 해당하는 코드를 생성자로 맵핑
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }

    public static BaseResponseStatus of(final String errorName){
        // valueOf : 이름을 가지고 객체르 가져오는 함수
        return BaseResponseStatus.valueOf(errorName);
    }
}
