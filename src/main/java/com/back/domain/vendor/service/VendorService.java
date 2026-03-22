package com.back.domain.vendor.service;

import com.back.domain.vendor.dto.request.VendorSaveRequest;
import com.back.domain.vendor.dto.response.VendorResponse;
import com.back.domain.vendor.entity.Vendor;
import com.back.domain.vendor.repository.VendorRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VendorService {

    private final VendorRepository vendorRepository;

    // 전체 목록 (이름 오름차순)
    public List<VendorResponse> findAll() {
        return vendorRepository.findAllByOrderByNameAsc()
                .stream().map(VendorResponse::from).toList();
    }

    // 단건 조회
    public VendorResponse findById(Long id) {
        return VendorResponse.from(findVendorById(id));
    }

    // 이름 검색
    public List<VendorResponse> search(String keyword) {
        return vendorRepository.findByNameContainingIgnoreCaseOrderByNameAsc(keyword)
                .stream().map(VendorResponse::from).toList();
    }

    // 등록
    @Transactional
    public VendorResponse save(VendorSaveRequest request) {
        if (vendorRepository.existsByName(request.getName())) {
            throw new CustomException(ErrorCode.VENDOR_DUPLICATE_NAME,
                    "'" + request.getName() + "'은(는) 이미 등록된 협력사입니다.");
        }
        return VendorResponse.from(vendorRepository.save(request.toEntity()));
    }

    // 수정
    @Transactional
    public VendorResponse update(Long id, VendorSaveRequest request) {
        Vendor vendor = findVendorById(id);
        vendor.update(request.getContactName(), request.getPhone(),
                request.getEmail(), request.getNote());
        return VendorResponse.from(vendor);
    }

    // 삭제
    @Transactional
    public void delete(Long id) {
        Vendor vendor = findVendorById(id);
        vendorRepository.delete(vendor);
    }

    // 내부 전용 Entity 조회
    private Vendor findVendorById(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.VENDOR_NOT_FOUND,
                        "협력사 ID [" + id + "]를 찾을 수 없습니다."));
    }
}
