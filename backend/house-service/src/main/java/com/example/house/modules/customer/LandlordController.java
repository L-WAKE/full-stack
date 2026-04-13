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

import java.util.List;

@RestController
@RequestMapping("/api/landlords")
public class LandlordController {

    private final LandlordService landlordService;

    public LandlordController(LandlordService landlordService) {
        this.landlordService = landlordService;
    }

    @GetMapping
    public ApiResponse<PageResponse<LandlordService.LandlordRecord>> list(
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(landlordService.list(pageNum, pageSize, keyword));
    }

    @GetMapping("/options")
    public ApiResponse<List<LandlordService.LandlordOption>> options() {
        return ApiResponse.success(landlordService.options());
    }

    @PostMapping
    public ApiResponse<LandlordService.LandlordRecord> create(@Valid @RequestBody LandlordService.LandlordUpsertRequest request) {
        return ApiResponse.success(landlordService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<LandlordService.LandlordRecord> update(
        @PathVariable Long id,
        @Valid @RequestBody LandlordService.LandlordUpsertRequest request
    ) {
        return ApiResponse.success(landlordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        landlordService.delete(id);
        return ApiResponse.success();
    }
}
