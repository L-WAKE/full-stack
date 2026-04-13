package com.example.house.modules.housing;

import com.example.house.common.api.ApiResponse;
import com.example.house.common.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/houses")
public class HouseController {

    private final HouseService houseService;

    public HouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    @GetMapping
    public ApiResponse<PageResponse<HouseService.HouseRecord>> list(
        @RequestParam(defaultValue = "1") @Min(1) int pageNum,
        @RequestParam(defaultValue = "10") @Min(1) int pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) RentalMode rentalMode,
        @RequestParam(required = false) HouseStatus status
    ) {
        return ApiResponse.success(houseService.list(pageNum, pageSize, keyword, rentalMode, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<HouseService.HouseRecord> detail(@PathVariable Long id) {
        return ApiResponse.success(houseService.get(id));
    }

    @PostMapping
    public ApiResponse<HouseService.HouseRecord> create(@Valid @RequestBody HouseService.HouseUpsertRequest request) {
        return ApiResponse.success(houseService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<HouseService.HouseRecord> update(
        @PathVariable Long id,
        @Valid @RequestBody HouseService.HouseUpsertRequest request
    ) {
        return ApiResponse.success(houseService.update(id, request));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<HouseService.HouseRecord> updateStatus(@PathVariable Long id, @RequestBody HouseStatusRequest request) {
        return ApiResponse.success(houseService.updateStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        houseService.delete(id);
        return ApiResponse.success();
    }

    public record HouseStatusRequest(HouseStatus status) {
    }
}
