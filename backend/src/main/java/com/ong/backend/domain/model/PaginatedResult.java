package com.ong.backend.domain.model;

import java.util.Collections;
import java.util.List;

public record PaginatedResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public PaginatedResult {
        content = content != null ? Collections.unmodifiableList(content) : List.of();
    }
}
