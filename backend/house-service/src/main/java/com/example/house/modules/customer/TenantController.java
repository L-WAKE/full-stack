package com.example.house.modules.customer;

import com.example.house.common.api.ApiResponse;
import com.example.house.common.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public ApiResponse<PageResponse<TenantService.TenantRecord>> list(
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(tenantService.list(pageNum, pageSize, keyword));
    }

    @PostMapping
    public ApiResponse<TenantService.TenantRecord> create(@Valid @RequestBody TenantService.TenantUpsertRequest request) {
        return ApiResponse.success(tenantService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TenantService.TenantRecord> update(
        @PathVariable Long id,
        @Valid @RequestBody TenantService.TenantUpsertRequest request
    ) {
        return ApiResponse.success(tenantService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return ApiResponse.success();
    }
}
