package com.example.common.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int total,
        int page,
        int pageSize,
        int totalPages) {

    public PageResponse(List<T> items,
                 int total,
                 Page<?> pageable) {
        this(items, total, pageable.getNumber(), pageable.getSize(), pageable.getTotalPages());
    }


}
