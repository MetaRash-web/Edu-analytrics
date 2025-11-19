package com.linter.eduanalitycs.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DashboardStats {
    private long userCount;
    private long courseCount;
    private long orderCount;
    private BigDecimal totalRevenue;
}
