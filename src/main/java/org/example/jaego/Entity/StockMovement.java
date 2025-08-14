package org.example.jaego.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Long movementId;

    // 재고와의 외래키 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", referencedColumnName = "inventory_id", nullable = false)
    private Inventory inventory;

    // 배치와의 외래키 관계 (어떤 배치에서 나갔는지 추적)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", referencedColumnName = "id")
    private stockBatches stockBatch;

    @Column(name = "movement_type", nullable = false, length = 10)
    private String movementType; // IN(입고), OUT(출고), ADJUST(조정), EXPIRE(만료)

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reason", length = 200)
    private String reason; // 이동 사유

    @Column(name = "reference_id")
    private String referenceId; // 참조 번호 (주문번호 등)

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy; // 작업자 ID

    // 비즈니스 메서드
    public boolean isInbound() {
        return "IN".equals(this.movementType);
    }

    public boolean isOutbound() {
        return "OUT".equals(this.movementType);
    }

    public boolean isAdjustment() {
        return "ADJUST".equals(this.movementType);
    }

    public boolean isExpired() {
        return "EXPIRE".equals(this.movementType);
    }
}
