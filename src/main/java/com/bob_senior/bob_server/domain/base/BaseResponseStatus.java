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
    INVALID_VOTE_ACCESS(false, 2008,"유효하지 않은 투표정보입니다."),
    NON_EXIST_POSTIDX(false,2009,"존재하지 않는 게시글입니다"),
    UNABLE_TO_MAKE_REQUEST_IN_POST(false,2010,"더이상 해당 글에 참여할 수 없습니다"),
    INVALID_ACCESS_TO_APPOINTMENT(false,2011,"해당 약속에 대한 권한이 없습니다"),
    NON_EXIST_POST_PARTICIPATION(false, 2012,"존재하지 않는 참가요청입니다"),
    UNABLE_TO_PARTICIPATE_IN_POST(false, 2013, "해당 약속에는 더이상 참여할 수 없습니다"),
    CAN_NOT_REQUEST_FRIENDSHIP(false,2014,"해당 유저에게 친구요청을 보낼 수 없습니다"),
    ALREADY_HAS_FRIENDSHIP(false, 2015, "이미 친구인 유저입니다"),
    INVALID_USER_TO_ACCEPT(false,2016,"존재하지 않는 친구요청입니다"),
    TAG_DOES_NOT_EXIST(false, 2017, "존재하지 않는 태그입니다"),
    ALREADY_BLOCKED_USER(false,2018,"이미 차단된 유저입니다"),
    DATE_TIME_ERROR(false, 2019,"날짜 선택이 잘못되었습니다"),
    ALREADY_EXIST_ONGOING_VOTE(false, 2020, "이미 진행중인 투표가 존재합니다."),
    VOTE_RESULT_IS_NOT_UNANIMITY(false, 2021,"해당 투표는 만장일치이어야 합니다"),
    IS_NOT_PARTICIPANT_OF_APPOINTMENT(false, 2022, "해당 약속에 참여하고 있지 않습니다"),
    INVALID_UUID_FOR_USER(false, 2023,"존재하지 않는 uuid입니다"),
    IS_NOT_OWNER_OF_APPOINTMENT(false, 2024, "게시글의 작성자가 아닙니다");


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
