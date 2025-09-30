package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String path;

    @CreationTimestamp
    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false, foreignKey = @ForeignKey(name="fk_reports_reporter_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User reporter_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_id", nullable = false, foreignKey = @ForeignKey(name="fk_reports_reported_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User reported_id;

    // 작성자인지 확인
    public boolean isReporter(User user) {
        return this.reporter_id.getId().equals(user.getId());
    }

    // 신고 대상 확인
    public boolean isReported(User user) {
        return this.reported_id.getId().equals(user.getId());
    }
}
