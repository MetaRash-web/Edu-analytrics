package com.linter.eduanalitycs.model.dto;

import java.time.LocalDateTime;

public record PeriodInfo(
        LocalDateTime start,
        LocalDateTime end
) {}
