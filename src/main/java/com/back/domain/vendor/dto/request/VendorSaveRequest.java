package com.back.domain.vendor.dto.request;

import com.back.domain.vendor.entity.Vendor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VendorSaveRequest {

    private String name;
    private String contactName;
    private String phone;
    private String email;
    private String note;

    public Vendor toEntity() {
        return Vendor.builder()
                .name(this.name)
                .contactName(this.contactName)
                .phone(this.phone)
                .email(this.email)
                .note(this.note)
                .build();
    }
}
