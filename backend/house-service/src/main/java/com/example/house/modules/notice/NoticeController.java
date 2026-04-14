package com.example.house.modules.notice;

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
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public ApiResponse<PageResponse<NoticeService.NoticeRecord>> list(
        @RequestParam(defaultValue = "1") @Min(1) int pageNum,
        @RequestParam(defaultValue = "10") @Min(1) int pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(noticeService.list(pageNum, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<NoticeService.NoticeRecord> detail(@PathVariable Long id) {
        return ApiResponse.success(noticeService.get(id));
    }

    @PostMapping
    public ApiResponse<NoticeService.NoticeRecord> create(@Valid @RequestBody NoticeService.NoticeUpsertRequest request) {
        return ApiResponse.success(noticeService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<NoticeService.NoticeRecord> update(
        @PathVariable Long id,
        @Valid @RequestBody NoticeService.NoticeUpsertRequest request
    ) {
        return ApiResponse.success(noticeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/publish")
    public ApiResponse<NoticeService.NoticeRecord> publish(@PathVariable Long id) {
        return ApiResponse.success(noticeService.publish(id));
    }

    @PutMapping("/{id}/offline")
    public ApiResponse<NoticeService.NoticeRecord> offline(@PathVariable Long id) {
        return ApiResponse.success(noticeService.offline(id));
    }

    @PutMapping("/{id}/pin")
    public ApiResponse<NoticeService.NoticeRecord> updatePin(
        @PathVariable Long id,
        @Valid @RequestBody NoticePinRequest request
    ) {
        return ApiResponse.success(noticeService.updatePin(id, request.pinned()));
    }

    public record NoticePinRequest(boolean pinned) {
    }
}
