package com.back.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ───────────────────────────────────────
    // 1. 커스텀 예외
    // ───────────────────────────────────────
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.warn("[CustomException] code={}, message={}", e.getErrorCode().getCode(), e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode(), e.getMessage()));
    }

    // ───────────────────────────────────────
    // 2. 요청 파라미터 누락
    // ───────────────────────────────────────
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("[MissingParam] {}", e.getMessage());
        String detail = "필수 파라미터 '" + e.getParameterName() + "'가 누락되었습니다.";
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, detail));
    }

    // ───────────────────────────────────────
    // 3. 파일 업로드 관련
    // ───────────────────────────────────────
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("[MaxUploadSize] {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.EXCEL_INVALID_FORMAT.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.EXCEL_INVALID_FORMAT, "업로드 파일 크기가 허용 한도를 초과했습니다."));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipart(MultipartException e) {
        log.warn("[MultipartException] {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.EXCEL_EMPTY_FILE.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.EXCEL_EMPTY_FILE));
    }

    // ───────────────────────────────────────
    // 4. 404
    // ───────────────────────────────────────
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException e) {
        log.warn("[NotFound] {}", e.getRequestURL());
        return ResponseEntity
                .status(ErrorCode.ORDER_NOT_FOUND.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.ORDER_NOT_FOUND, "요청하신 페이지를 찾을 수 없습니다. [" + e.getRequestURL() + "]"));
    }

    // ───────────────────────────────────────
    // 5. 그 외 예상치 못한 예외 (500)
    // ───────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[UnhandledException] {}", e.getMessage(), e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
