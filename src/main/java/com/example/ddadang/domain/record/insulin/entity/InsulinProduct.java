package com.example.ddadang.domain.record.insulin.entity;

import com.example.ddadang.domain.record.insulin.enums.InsulinActionType;
import com.example.ddadang.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "insulin_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InsulinProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    private InsulinActionType actionType;

    public InsulinProduct(String name, InsulinActionType actionType) {
        this.name = name;
        this.actionType = actionType;
    }
}
