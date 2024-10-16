package com.dev5ops.healthtart.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum StatusEnum {

    WRONG_ENTRY_POINT(400, HttpStatus.BAD_REQUEST, "잘못된 접근입니다"),
    MISSING_REQUEST_PARAMETER(400, HttpStatus.BAD_REQUEST, "필수 요청 파라미터가 누락되었습니다."),
    DATA_INTEGRITY_VIOLATION(400, HttpStatus.BAD_REQUEST, "데이터 무결성 위반입니다. 필수 값이 누락되었거나 유효하지 않습니다."),
    INVALID_PARAMETER_FORMAT(400, HttpStatus.BAD_REQUEST, "요청에 유효하지 않은 인자 형식입니다."),
    EMAIL_DUPLICATE(400, HttpStatus.BAD_REQUEST, "이미 등록된 이메일입니다."),
    GYM_DUPLICATE(400, HttpStatus.BAD_REQUEST, "이미 등록된 헬스장입니다."),
    EQUIPMENT_DUPLICATE(400, HttpStatus.BAD_REQUEST, "이미 등록된 운동기구입니다."),
    DAY_OF_INBODY_DUPLICATE(400, HttpStatus.BAD_REQUEST, "이미 등록한 인바디 정보입니다."),
    ROUTINES_CREATED_ERROR(400, HttpStatus.BAD_REQUEST, "운동 루틴 생성 중 오류가 발생했습니다."),

    RESTRICTED(403, HttpStatus.UNAUTHORIZED, "권한이 없습니다."),

    USER_NOT_FOUND(404, HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    GYM_NOT_FOUND(404, HttpStatus.NOT_FOUND, "헬스장이 존재하지 않습니다."),
    ROUTINE_NOT_FOUND(404, HttpStatus.NOT_FOUND, "해당 운동루틴이 존재하지 않습니다."),
    EQUIPMENT_NOT_FOUND(404, HttpStatus.NOT_FOUND, "DB에 해당 운동기구 데이터가 존재하지 않습니다."),
    DAY_NOT_FOUND(404, HttpStatus.NOT_FOUND,"DB에 해당 날짜에 운동한 기록이 존재하지 않습니다." ),
    EQUIPMENT_PER_GYM_NOT_FOUND(404, HttpStatus.NOT_FOUND, "DB에 해당 헬스장 별 운동기구 데이터가 존재하지 않습니다."),
    INBODY_NOT_FOUND(404, HttpStatus.NOT_FOUND, "인바디 데이터가 존재하지 않습니다."),
    RECORD_NOT_FOUND(404, HttpStatus.NOT_FOUND, "DB에 해당 운동기록이 존재하지 않습니다."),

    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다");
    private final int statusCode;
    private final HttpStatus httpStatus;
    private final String message;

    StatusEnum(int statusCode, HttpStatus httpStatus, String message) {
        this.statusCode = statusCode;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}

