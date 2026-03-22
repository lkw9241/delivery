package com.back.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // ───────────────────────────────────────
    // 1. 커스텀 예외
    // ───────────────────────────────────────
    @ExceptionHandler(CustomException.class)
    public Object handleCustomException(CustomException e, HttpServletRequest request) {
        log.warn("[CustomException] code={}, message={}", e.getErrorCode().getCode(), e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode(), e.getMessage());

        if (isBrowserRequest(request)) {
            return errorModelAndView(errorResponse);
        }
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(errorResponse);
    }

    // ───────────────────────────────────────
    // 2. 요청 파라미터 누락
    // ───────────────────────────────────────
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object handleMissingParam(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("[MissingParam] {}", e.getMessage());
        String detail = "필수 파라미터 '" + e.getParameterName() + "'가 누락되었습니다.";
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT, detail);

        if (isBrowserRequest(request)) {
            return errorModelAndView(errorResponse);
        }
        return ResponseEntity.status(ErrorCode.INVALID_INPUT.getHttpStatus()).body(errorResponse);
    }

    // ───────────────────────────────────────
    // 3. 파일 업로드 관련
    // ───────────────────────────────────────
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleMaxUploadSize(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("[MaxUploadSize] {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.EXCEL_INVALID_FORMAT, "업로드 파일 크기가 허용 한도를 초과했습니다.");

        if (isBrowserRequest(request)) {
            return errorModelAndView(errorResponse);
        }
        return ResponseEntity.status(ErrorCode.EXCEL_INVALID_FORMAT.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(MultipartException.class)
    public Object handleMultipart(MultipartException e, HttpServletRequest request) {
        log.warn("[MultipartException] {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.EXCEL_EMPTY_FILE);

        if (isBrowserRequest(request)) {
            return errorModelAndView(errorResponse);
        }
        return ResponseEntity.status(ErrorCode.EXCEL_EMPTY_FILE.getHttpStatus()).body(errorResponse);
    }

    // ───────────────────────────────────────
    // 4. 404
    // ───────────────────────────────────────
    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNotFound(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("[NotFound] {}", e.getRequestURL());
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.ORDER_NOT_FOUND,
                "요청하신 페이지를 찾을 수 없습니다. [" + e.getRequestURL() + "]");

        if (isBrowserRequest(request)) {
            return errorModelAndView(errorResponse);
        }
        return ResponseEntity.status(ErrorCode.ORDER_NOT_FOUND.getHttpStatus()).body(errorResponse);
    }

    // ───────────────────────────────────────
    // 5. 그 외 예상치 못한 예외 (500)
    // ───────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request) {
        log.error("[UnhandledException] {}", e.getMessage(), e);
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);

        if (isBrowserRequest(request)) {
            return errorModelAndView(errorResponse);
        }
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus()).body(errorResponse);
    }

    // ───────────────────────────────────────
    // 내부 헬퍼
    // ───────────────────────────────────────

    /**
     * Accept 헤더 기준으로 브라우저 요청 여부 판단
     * text/html 을 수용하면 브라우저 요청으로 간주
     */
    private boolean isBrowserRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/html");
    }

    /**
     * 에러 정보를 모델에 담아 error.html 뷰로 반환
     */
    private ModelAndView errorModelAndView(ErrorResponse errorResponse) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", errorResponse.getCode());
        mav.addObject("errorMessage", errorResponse.getMessage());
        mav.addObject("errorStatus", errorResponse.getStatus());
        return mav;
    }
}
