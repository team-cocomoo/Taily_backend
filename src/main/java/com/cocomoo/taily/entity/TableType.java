package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "table_types")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TableType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TableTypeCategory category;
}
