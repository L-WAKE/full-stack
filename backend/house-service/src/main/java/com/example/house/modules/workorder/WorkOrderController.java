package com.example.house.modules.workorder;

import com.example.house.common.api.ApiResponse;
import com.example.house.common.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @GetMapping("/api/maintenance-orders")
    public ApiResponse<PageResponse<WorkOrderService.MaintenanceOrderRecord>> maintenance(
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(workOrderService.listMaintenance(pageNum, pageSize, keyword));
    }

    @PostMapping("/api/maintenance-orders")
    public ApiResponse<WorkOrderService.MaintenanceOrderRecord> createMaintenance(
        @Valid @RequestBody WorkOrderService.MaintenanceUpsertRequest request
    ) {
        return ApiResponse.success(workOrderService.createMaintenance(request));
    }

    @PutMapping("/api/maintenance-orders/{id}")
    public ApiResponse<WorkOrderService.MaintenanceOrderRecord> updateMaintenance(
        @PathVariable Long id,
        @Valid @RequestBody WorkOrderService.MaintenanceUpsertRequest request
    ) {
        return ApiResponse.success(workOrderService.updateMaintenance(id, request));
    }

    @DeleteMapping("/api/maintenance-orders/{id}")
    public ApiResponse<Void> deleteMaintenance(@PathVariable Long id) {
        workOrderService.deleteMaintenance(id);
        return ApiResponse.success();
    }

    @GetMapping("/api/cleaning-orders")
    public ApiResponse<PageResponse<WorkOrderService.CleaningOrderRecord>> cleaning(
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(workOrderService.listCleaning(pageNum, pageSize, keyword));
    }

    @PostMapping("/api/cleaning-orders")
    public ApiResponse<WorkOrderService.CleaningOrderRecord> createCleaning(
        @Valid @RequestBody WorkOrderService.CleaningUpsertRequest request
    ) {
        return ApiResponse.success(workOrderService.createCleaning(request));
    }

    @PutMapping("/api/cleaning-orders/{id}")
    public ApiResponse<WorkOrderService.CleaningOrderRecord> updateCleaning(
        @PathVariable Long id,
        @Valid @RequestBody WorkOrderService.CleaningUpsertRequest request
    ) {
        return ApiResponse.success(workOrderService.updateCleaning(id, request));
    }

    @DeleteMapping("/api/cleaning-orders/{id}")
    public ApiResponse<Void> deleteCleaning(@PathVariable Long id) {
        workOrderService.deleteCleaning(id);
        return ApiResponse.success();
    }
}
