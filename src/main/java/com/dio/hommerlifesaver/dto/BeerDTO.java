package com.dio.hommerlifesaver.dto;

import com.dio.hommerlifesaver.enums.BeerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BeerDTO {

    private Long id;

    @NotNull
    @Size(min=1, max=200)
    private String name;

    @NotNull
    @Size(min=1, max=200)
    private String brand;

    @NotNull
    @Size(max=500)
    private Integer max;

    @NotNull
    @Size(max=100)
    private Integer quantity;

    @NotNull
    @Enumerated(EnumType.STRING)
    private BeerType type;
}
