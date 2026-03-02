package com.back.domain.order.order.entity;

import com.back.domain.order.order.enums.OrderStatus;
import com.back.domain.order.order.enums.RiskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // PO 번호
    @Column(nullable = false)
    private String poNumber;

    // 협력사
    @Column(nullable = false)
    private String vendor;

    // 품목명
    @Column(nullable = false)
    private String item;

    // 발주일
    private LocalDate orderDate;

    // 납기일
    private LocalDate dueDate;

    // 입고 여부
    private boolean received;

    // 진행 상태 (미착수, 가공중, 완료, 입고)
    @Enumerated(EnumType.STRING)
    private OrderStatus progressStatus;

    // 위험 상태 (정상, 위험, 지연)
    @Enumerated(EnumType.STRING)
    private RiskStatus riskStatus;


     //진행 상태 변경
    public void updateProgressStatus(OrderStatus status) {
        this.progressStatus = status;
    }


    //입고 처리
    public void markReceived() {
        this.received = true;
        this.progressStatus = OrderStatus.RECEIVED;
    }

    /**
     * 위험 상태 업데이트
     */
    public void updateRiskStatus(RiskStatus riskStatus) {
        this.riskStatus = riskStatus;
    }
}
