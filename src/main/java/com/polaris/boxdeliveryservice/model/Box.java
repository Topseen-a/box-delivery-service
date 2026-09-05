package com.polaris.boxdeliveryservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "boxes")
@Getter
@Setter
@NoArgsConstructor
public class Box {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 20)
    @Column(name = "txref", nullable = false, unique = true, length = 20)
    private String txref;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @DecimalMax(value = "500")
    @Column(name = "weight_limit", nullable = false, precision = 6, scale = 2)
    private BigDecimal weightLimit;

    @NotNull
    @Column(name = "battery_capacity", nullable = false)
    private Integer batteryCapacity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "state",  nullable = false, length = 20)
    private BoxState state = BoxState.IDLE;

    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL,  orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Item> items = new ArrayList<>();

    public BigDecimal totalLoadedWeight() {
        return items.stream()
                .map(Item::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal remainingCapacity() {
        return weightLimit.subtract(totalLoadedWeight());
    }
}
