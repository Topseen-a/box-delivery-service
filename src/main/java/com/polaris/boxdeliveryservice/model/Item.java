package com.polaris.boxdeliveryservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "name may only contain letters, numbers, hyphen and underscore")
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Column(name = "weight", nullable = false, precision = 6, scale = 2)
    private BigDecimal weight;

    @NotNull
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "code may only contain upper case letters, numbers and underscore")
    @Column(name = "code",  nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "box_id", nullable = false)
    @JsonIgnore
    private Box box;
}
