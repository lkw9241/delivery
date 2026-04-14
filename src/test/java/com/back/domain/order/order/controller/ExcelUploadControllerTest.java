package com.back.domain.order.order.controller;

import com.back.domain.order.order.service.ExcelService;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExcelUploadControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ExcelService excelService;

    // ───────────────────────────────────────
    // POST /upload — 정상 케이스
    // ───────────────────────────────────────

    @Test
    @DisplayName("엑셀 업로드 - .xlsx 파일을 정상 업로드하면 대시보드로 리다이렉트된다")
    void upload_xlsx_success() throws Exception {
        // given
        MockMultipartFile file = buildMockFile("orders.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        willDoNothing().given(excelService).upload(any());

        // when & then
        mvc.perform(multipart("/upload").file(file))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("엑셀 업로드 - .xls 파일을 정상 업로드하면 대시보드로 리다이렉트된다")
    void upload_xls_success() throws Exception {
        // given
        MockMultipartFile file = buildMockFile("orders.xls", "application/vnd.ms-excel");
        willDoNothing().given(excelService).upload(any());

        // when & then
        mvc.perform(multipart("/upload").file(file))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    // ───────────────────────────────────────
    // POST /upload — 파일 유효성 실패
    // ───────────────────────────────────────

    @Test
    @DisplayName("엑셀 업로드 - 빈 파일이면 EXCEL_EMPTY_FILE 예외가 발생한다")
    void upload_emptyFile() throws Exception {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );
        willThrow(new CustomException(ErrorCode.EXCEL_EMPTY_FILE))
                .given(excelService).upload(any());

        // when & then
        mvc.perform(multipart("/upload").file(emptyFile))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("엑셀 업로드 - 허용되지 않는 확장자(.csv)이면 EXCEL_INVALID_FORMAT 예외가 발생한다")
    void upload_invalidExtension_csv() throws Exception {
        // given
        MockMultipartFile csvFile = buildMockFile("orders.csv", "text/csv");
        willThrow(new CustomException(ErrorCode.EXCEL_INVALID_FORMAT,
                "허용되지 않는 파일 형식입니다. (.xls, .xlsx 만 업로드 가능)"))
                .given(excelService).upload(any());

        // when & then
        mvc.perform(multipart("/upload").file(csvFile))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("엑셀 업로드 - 허용되지 않는 확장자(.txt)이면 EXCEL_INVALID_FORMAT 예외가 발생한다")
    void upload_invalidExtension_txt() throws Exception {
        // given
        MockMultipartFile txtFile = buildMockFile("orders.txt", "text/plain");
        willThrow(new CustomException(ErrorCode.EXCEL_INVALID_FORMAT,
                "허용되지 않는 파일 형식입니다. (.xls, .xlsx 만 업로드 가능)"))
                .given(excelService).upload(any());

        // when & then
        mvc.perform(multipart("/upload").file(txtFile))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ───────────────────────────────────────
    // POST /upload — 파싱 실패
    // ───────────────────────────────────────

    @Test
    @DisplayName("엑셀 업로드 - 행에 필수값이 누락되어 있으면 EXCEL_INVALID_ROW 예외가 발생한다")
    void upload_invalidRow_missingRequiredField() throws Exception {
        // given
        MockMultipartFile file = buildMockFile("orders.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        willThrow(new CustomException(ErrorCode.EXCEL_INVALID_ROW,
                "2번째 행에 필수값(PO번호, 협력사, 품목)이 누락되었습니다."))
                .given(excelService).upload(any());

        // when & then
        mvc.perform(multipart("/upload").file(file))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("엑셀 업로드 - 파일 파싱 중 오류가 발생하면 EXCEL_PARSE_FAILED 예외가 발생한다")
    void upload_parseFailed() throws Exception {
        // given
        MockMultipartFile file = buildMockFile("orders.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        willThrow(new CustomException(ErrorCode.EXCEL_PARSE_FAILED,
                "엑셀 파일 파싱 중 오류가 발생했습니다. 파일 형식을 확인해주세요."))
                .given(excelService).upload(any());

        // when & then
        mvc.perform(multipart("/upload").file(file))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    // ───────────────────────────────────────
    // 테스트 픽스처 헬퍼
    // ───────────────────────────────────────

    private MockMultipartFile buildMockFile(String filename, String contentType) {
        return new MockMultipartFile(
                "file", filename, contentType,
                "mock excel content".getBytes()
        );
    }
}
