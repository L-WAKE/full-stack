package com.example.house.modules.system;

import com.example.house.common.api.ApiResponse;
import com.example.house.common.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class SystemController {

    private final SystemService systemService;

    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    @GetMapping("/employees")
    public ApiResponse<PageResponse<SystemService.EmployeeRecord>> employees(
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(systemService.listEmployees(pageNum, pageSize, keyword));
    }

    @PostMapping("/employees")
    public ApiResponse<SystemService.EmployeeRecord> createEmployee(@Valid @RequestBody SystemService.EmployeeUpsertRequest request) {
        return ApiResponse.success(systemService.createEmployee(request));
    }

    @PutMapping("/employees/{id}")
    public ApiResponse<SystemService.EmployeeRecord> updateEmployee(
        @PathVariable Long id,
        @Valid @RequestBody SystemService.EmployeeUpsertRequest request
    ) {
        return ApiResponse.success(systemService.updateEmployee(id, request));
    }

    @PutMapping("/employees/{id}/status")
    public ApiResponse<SystemService.EmployeeRecord> updateEmployeeStatus(
        @PathVariable Long id,
        @Valid @RequestBody StatusUpdateRequest request
    ) {
        return ApiResponse.success(systemService.updateEmployeeStatus(id, request.status()));
    }

    @GetMapping("/roles")
    public ApiResponse<List<SystemService.RoleRecord>> roles() {
        return ApiResponse.success(systemService.listRoles());
    }

    @PostMapping("/roles")
    public ApiResponse<SystemService.RoleRecord> createRole(@Valid @RequestBody SystemService.RoleUpsertRequest request) {
        return ApiResponse.success(systemService.createRole(request));
    }

    @PutMapping("/roles/{id}")
    public ApiResponse<SystemService.RoleRecord> updateRole(
        @PathVariable Long id,
        @Valid @RequestBody SystemService.RoleUpsertRequest request
    ) {
        return ApiResponse.success(systemService.updateRole(id, request));
    }

    @GetMapping("/menus/tree")
    public ApiResponse<List<SystemService.MenuRecord>> menus() {
        return ApiResponse.success(systemService.listMenus());
    }

    @PostMapping("/menus")
    public ApiResponse<SystemService.MenuRecord> createMenu(@Valid @RequestBody SystemService.MenuUpsertRequest request) {
        return ApiResponse.success(systemService.createMenu(request));
    }

    @PutMapping("/menus/{id}")
    public ApiResponse<SystemService.MenuRecord> updateMenu(
        @PathVariable Long id,
        @Valid @RequestBody SystemService.MenuUpsertRequest request
    ) {
        return ApiResponse.success(systemService.updateMenu(id, request));
    }

    public record StatusUpdateRequest(@NotBlank String status) {
    }
}
