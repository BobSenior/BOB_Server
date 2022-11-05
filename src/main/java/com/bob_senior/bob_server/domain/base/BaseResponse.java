package com.bob_senior.bob_server.domain.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.bob_senior.bob_server.domain.base.BaseResponseStatus.SUCCESS;

@Getter
@AllArgsConstructor
public class BaseResponse<T> {

    @JsonProperty("isSuccess")
    private final Boolean isSuccess;
    private final int code;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    public BaseResponse(T result) {
        this.isSuccess = SUCCESS.isSuccess();
        this.code = SUCCESS.getCode();
        this.message = SUCCESS.getMessage();
        this.result = result;
    }

    public BaseResponse(BaseResponseStatus status) {
        this.isSuccess = status.isSuccess();
        this.message = status.getMessage();
        this.code = status.getCode();
    }

    public BaseResponse(BaseResponseStatus status, T result){
        this.isSuccess = status.isSuccess();
        this.message = status.getMessage();
        this.code = status.getCode();
        this.result = result;
    }
}
