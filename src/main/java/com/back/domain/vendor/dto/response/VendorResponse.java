package com.back.domain.vendor.dto.response;

import com.back.domain.vendor.entity.Vendor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VendorResponse {

    private Long id;
    private String name;
    private String contactName;
    private String phone;
    private String email;
    private String note;
    private LocalDateTime createdAt;

    public static VendorResponse from(Vendor vendor) {
        return VendorResponse.builder()
                .id(vendor.getId())
                .name(vendor.getName())
                .contactName(vendor.getContactName())
                .phone(vendor.getPhone())
                .email(vendor.getEmail())
                .note(vendor.getNote())
                .createdAt(vendor.getCreatedAt())
                .build();
    }
}
