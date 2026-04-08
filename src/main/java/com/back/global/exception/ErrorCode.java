package com.back.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ───────────────────────────────────────
    // Order
    // ───────────────────────────────────────
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "해당 발주를 찾을 수 없습니다."),
    ORDER_ALREADY_RECEIVED(HttpStatus.BAD_REQUEST, "O002", "이미 입고 처리된 발주입니다."),
    ORDER_INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "O003", "유효하지 않은 상태 변경입니다."),

    // ───────────────────────────────────────
    // Vendor
    // ───────────────────────────────────────
    VENDOR_NOT_FOUND(HttpStatus.NOT_FOUND, "V001", "해당 협력사를 찾을 수 없습니다."),
    VENDOR_DUPLICATE_NAME(HttpStatus.BAD_REQUEST, "V002", "이미 등록된 협력사 이름입니다."),

    // ───────────────────────────────────────
    // Report
    // ───────────────────────────────────────
    REPORT_GENERATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "R001", "리포트 생성 중 오류가 발생했습니다."),

    // ───────────────────────────────────────
    // Notification
    // ───────────────────────────────────────
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "해당 알림을 찾을 수 없습니다."),

    // ───────────────────────────────────────
    // Excel
    // ───────────────────────────────────────
    EXCEL_EMPTY_FILE(HttpStatus.BAD_REQUEST, "E001", "업로드된 파일이 비어 있습니다."),
    EXCEL_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "E002", "엑셀 파일 형식이 올바르지 않습니다. (.xls, .xlsx 만 허용)"),
    EXCEL_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E003", "엑셀 파일 파싱 중 오류가 발생했습니다."),
    EXCEL_INVALID_ROW(HttpStatus.BAD_REQUEST, "E004", "엑셀 데이터에 필수값이 누락된 행이 있습니다."),

    // ───────────────────────────────────────
    // Common
    // ───────────────────────────────────────
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
