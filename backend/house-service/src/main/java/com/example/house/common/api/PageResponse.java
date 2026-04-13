package com.example.house.common.api;

import java.util.List;

public record PageResponse<T>(List<T> list, long total, int pageNum, int pageSize) {
}
