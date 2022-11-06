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
    ALREADY_VOTED(false,2004,"이미 참여한 투표입니다"),
    ALREADY_EXIST_VOTE_CONTENT(false,2005,"이미 존재하는 투표입니다"),
    IS_NOT_OWNER_OF_VOTE(false,2006,"해당 투표를 종료할 권한이 없습니다"),
    NO_VOTE_IN_CHATROOM(false,2007,"현재 활성화된 투표가 없습니다"),
    INVALID_VOTE_ACCESS(false, 2008,"유효하지 않은 투표정보입니다.");


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
