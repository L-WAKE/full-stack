package com.example.house.modules.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.house.common.api.PageResponse;
import com.example.house.common.exception.BusinessException;
import com.example.house.modules.customer.entity.TenantEntity;
import com.example.house.modules.customer.mapper.TenantMapper;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;

@Service
public class TenantService {

    private final TenantMapper tenantMapper;

    public TenantService(TenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    public PageResponse<TenantRecord> list(int pageNum, int pageSize, String keyword) {
        Page<TenantEntity> page = tenantMapper.selectPage(
            new Page<>(pageNum, pageSize),
            new LambdaQueryWrapper<TenantEntity>()
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                    .like(TenantEntity::getName, keyword)
                    .or()
                    .like(TenantEntity::getMobile, keyword))
                .orderByDesc(TenantEntity::getId)
        );
        return new PageResponse<>(page.getRecords().stream().map(this::toRecord).toList(), page.getTotal(), pageNum, pageSize);
    }

    public TenantRecord create(TenantUpsertRequest request) {
        TenantEntity entity = new TenantEntity();
        fillEntity(entity, request);
        entity.setOrgId(1L);
        entity.setCreatedBy(1L);
        entity.setUpdatedBy(1L);
        entity.setDeleted(0);
        tenantMapper.insert(entity);
        return toRecord(entity);
    }

    public TenantRecord update(Long id, TenantUpsertRequest request) {
        TenantEntity entity = requireEntity(id);
        fillEntity(entity, request);
        entity.setUpdatedBy(1L);
        tenantMapper.updateById(entity);
        return toRecord(entity);
    }

    public void delete(Long id) {
        if (tenantMapper.deleteById(id) == 0) {
            throw new BusinessException("Tenant not found");
        }
    }

    private TenantEntity requireEntity(Long id) {
        TenantEntity entity = tenantMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("Tenant not found");
        }
        return entity;
    }

    private void fillEntity(TenantEntity entity, TenantUpsertRequest request) {
        entity.setName(request.name());
        entity.setMobile(request.mobile());
        entity.setGender(request.gender());
        entity.setHouseName(request.houseName());
        entity.setCheckinDate(request.checkinDate());
        entity.setCheckoutDate(request.checkoutDate());
        entity.setEmergencyContact(request.emergencyContact());
        entity.setEmergencyMobile(request.emergencyMobile());
    }

    private TenantRecord toRecord(TenantEntity entity) {
        return new TenantRecord(
            entity.getId(),
            entity.getName(),
            entity.getMobile(),
            entity.getGender(),
            entity.getHouseName(),
            entity.getCheckinDate(),
            entity.getCheckoutDate(),
            entity.getEmergencyContact(),
            entity.getEmergencyMobile()
        );
    }

    public record TenantRecord(
        Long id,
        String name,
        String mobile,
        String gender,
        String houseName,
        String checkinDate,
        String checkoutDate,
        String emergencyContact,
        String emergencyMobile
    ) {
    }

    public record TenantUpsertRequest(
        @NotBlank String name,
        @NotBlank String mobile,
        String gender,
        String houseName,
        String checkinDate,
        String checkoutDate,
        String emergencyContact,
        String emergencyMobile
    ) {
    }
}
