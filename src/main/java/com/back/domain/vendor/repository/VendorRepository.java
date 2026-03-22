package com.back.domain.vendor.repository;

import com.back.domain.vendor.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {

    // 이름으로 단건 조회 (중복 체크 / 검색)
    Optional<Vendor> findByName(String name);

    // 이름 존재 여부 (중복 방지)
    boolean existsByName(String name);

    // 이름 포함 검색
    List<Vendor> findByNameContainingIgnoreCaseOrderByNameAsc(String keyword);

    // 전체 이름 오름차순
    List<Vendor> findAllByOrderByNameAsc();
}
