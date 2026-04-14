package com.example.house.modules.notice;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.house.common.api.PageResponse;
import com.example.house.common.exception.BusinessException;
import com.example.house.modules.notice.entity.NoticeEntity;
import com.example.house.modules.notice.mapper.NoticeMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NoticeService {

    private final NoticeMapper noticeMapper;

    public NoticeService(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    public PageResponse<NoticeRecord> list(int pageNum, int pageSize, String keyword, String status) {
        Page<NoticeEntity> page = noticeMapper.selectPage(
            new Page<>(pageNum, pageSize),
            new LambdaQueryWrapper<NoticeEntity>()
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                    .like(NoticeEntity::getTitle, keyword)
                    .or()
                    .like(NoticeEntity::getSummary, keyword))
                .eq(status != null && !status.isBlank(), NoticeEntity::getStatus, status)
                .orderByDesc(NoticeEntity::getPinned)
                .orderByDesc(NoticeEntity::getPublishTime)
                .orderByDesc(NoticeEntity::getId)
        );
        return new PageResponse<>(page.getRecords().stream().map(this::toRecord).toList(), page.getTotal(), pageNum, pageSize);
    }

    public NoticeRecord get(Long id) {
        return toRecord(requireNotice(id));
    }

    public NoticeRecord create(NoticeUpsertRequest request) {
        NoticeEntity entity = new NoticeEntity();
        fillEntity(entity, request);
        entity.setOrgId(1L);
        entity.setCreatedBy(1L);
        entity.setUpdatedBy(1L);
        entity.setDeleted(0);
        noticeMapper.insert(entity);
        return toRecord(entity);
    }

    public NoticeRecord update(Long id, NoticeUpsertRequest request) {
        NoticeEntity entity = requireNotice(id);
        LocalDateTime originalPublishTime = entity.getPublishTime();
        fillEntity(entity, request);
        preservePublishTime(entity, originalPublishTime);
        entity.setUpdatedBy(1L);
        noticeMapper.updateById(entity);
        return toRecord(entity);
    }

    public void delete(Long id) {
        if (noticeMapper.deleteById(id) == 0) {
            throw new BusinessException("Notice not found");
        }
    }

    public NoticeRecord publish(Long id) {
        NoticeEntity entity = requireNotice(id);
        entity.setStatus(NoticeStatus.PUBLISHED.name());
        if (entity.getPublishTime() == null) {
            entity.setPublishTime(LocalDateTime.now());
        }
        entity.setUpdatedBy(1L);
        noticeMapper.updateById(entity);
        return toRecord(entity);
    }

    public NoticeRecord offline(Long id) {
        NoticeEntity entity = requireNotice(id);
        entity.setStatus(NoticeStatus.OFFLINE.name());
        entity.setUpdatedBy(1L);
        noticeMapper.updateById(entity);
        return toRecord(entity);
    }

    public NoticeRecord updatePin(Long id, boolean pinned) {
        NoticeEntity entity = requireNotice(id);
        entity.setPinned(pinned ? 1 : 0);
        entity.setUpdatedBy(1L);
        noticeMapper.updateById(entity);
        return toRecord(entity);
    }

    private void fillEntity(NoticeEntity entity, NoticeUpsertRequest request) {
        entity.setTitle(request.title());
        entity.setSummary(request.summary());
        entity.setContent(request.content());
        entity.setStatus(request.status().name());
        entity.setPinned(request.pinned() ? 1 : 0);
        if (request.status() == NoticeStatus.PUBLISHED && entity.getPublishTime() == null) {
            entity.setPublishTime(LocalDateTime.now());
        }
        if (request.status() == NoticeStatus.DRAFT) {
            entity.setPublishTime(null);
        }
    }

    private void preservePublishTime(NoticeEntity entity, LocalDateTime originalPublishTime) {
        if (NoticeStatus.OFFLINE.name().equals(entity.getStatus())) {
            entity.setPublishTime(originalPublishTime);
        }
    }

    private NoticeEntity requireNotice(Long id) {
        NoticeEntity entity = noticeMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("Notice not found");
        }
        return entity;
    }

    private NoticeRecord toRecord(NoticeEntity entity) {
        return new NoticeRecord(
            entity.getId(),
            entity.getTitle(),
            entity.getSummary(),
            entity.getContent(),
            NoticeStatus.valueOf(entity.getStatus()),
            entity.getPinned() != null && entity.getPinned() == 1,
            entity.getPublishTime(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public enum NoticeStatus {
        DRAFT,
        PUBLISHED,
        OFFLINE
    }

    public record NoticeRecord(
        Long id,
        String title,
        String summary,
        String content,
        NoticeStatus status,
        boolean pinned,
        LocalDateTime publishTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
    }

    public record NoticeUpsertRequest(
        @NotBlank(message = "公告标题不能为空") String title,
        String summary,
        @NotBlank(message = "公告内容不能为空") String content,
        @NotNull(message = "公告状态不能为空") NoticeStatus status,
        boolean pinned
    ) {
    }
}
