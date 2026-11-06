package com.ong.backend.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ong.backend.domain.model.PaginatedResult;

import java.util.List;
import java.util.function.Function;

public record PaginatedResponse<T>(
        List<T> items,
        int page,
        int size,
        @JsonProperty("total_elements")
        long totalElements,
        @JsonProperty("total_pages")
        int totalPages
) {
    public static <D, R> PaginatedResponse<R> fromDomain(PaginatedResult<D> result, Function<D, R> mapper) {
        List<R> items = result.content().stream().map(mapper).toList();
        return new PaginatedResponse<>(
                items,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }
}
