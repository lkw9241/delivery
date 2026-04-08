package com.back.domain.vendor.controller;

import com.back.domain.vendor.dto.request.VendorSaveRequest;
import com.back.domain.vendor.dto.response.VendorResponse;
import com.back.domain.vendor.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vendors")
public class VendorController {

    private final VendorService vendorService;

    // ───────────────────────────────────────
    // 협력사 목록
    // ───────────────────────────────────────
    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        List<VendorResponse> vendors = (keyword != null && !keyword.isBlank())
                ? vendorService.search(keyword)
                : vendorService.findAll();

        model.addAttribute("vendors", vendors);
        model.addAttribute("keyword", keyword);
        return "vendor-list";
    }

    // ───────────────────────────────────────
    // 협력사 상세
    // ───────────────────────────────────────
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("vendor", vendorService.findById(id));
        return "vendor-detail";
    }

    // ───────────────────────────────────────
    // 협력사 등록 폼
    // ───────────────────────────────────────
    @GetMapping("/new")
    public String newForm() {
        return "vendor-form";
    }

    // ───────────────────────────────────────
    // 협력사 등록 처리
    // ───────────────────────────────────────
    @PostMapping
    public String save(@ModelAttribute VendorSaveRequest request) {
        VendorResponse saved = vendorService.save(request);
        return "redirect:/vendors/" + saved.getId();
    }

    // ───────────────────────────────────────
    // 협력사 수정 폼
    // ───────────────────────────────────────
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("vendor", vendorService.findById(id));
        return "vendor-form";
    }

    // ───────────────────────────────────────
    // 협력사 수정 처리
    // ───────────────────────────────────────
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute VendorSaveRequest request) {
        vendorService.update(id, request);
        return "redirect:/vendors/" + id;
    }

    // ───────────────────────────────────────
    // 협력사 삭제
    // ───────────────────────────────────────
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        vendorService.delete(id);
        return "redirect:/vendors";
    }
}
