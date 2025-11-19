package com.linter.eduanalitycs.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProductPerformanceDTO {
    private Long courseId;
    private String courseName;
    private Long salesCount;
    private BigDecimal revenue;
}