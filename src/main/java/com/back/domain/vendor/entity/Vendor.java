package com.back.domain.vendor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회사명 (unique)
    @Column(nullable = false, unique = true)
    private String name;

    // 담당자명
    private String contactName;

    // 연락처
    private String phone;

    // 이메일
    private String email;

    // 비고
    private String note;

    // 등록일시
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ───────────────────────────────────────
    // 수정 메서드
    // ───────────────────────────────────────
    public void update(String contactName, String phone, String email, String note) {
        this.contactName = contactName;
        this.phone = phone;
        this.email = email;
        this.note = note;
    }
}
