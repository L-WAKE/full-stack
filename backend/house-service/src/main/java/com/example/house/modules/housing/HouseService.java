package com.example.house.modules.housing;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.house.common.api.PageResponse;
import com.example.house.common.exception.BusinessException;
import com.example.house.modules.customer.entity.LandlordEntity;
import com.example.house.modules.customer.mapper.LandlordMapper;
import com.example.house.modules.housing.entity.HouseEntity;
import com.example.house.modules.housing.mapper.HouseMapper;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class HouseService {

    private final HouseMapper houseMapper;
    private final LandlordMapper landlordMapper;

    public HouseService(HouseMapper houseMapper, LandlordMapper landlordMapper) {
        this.houseMapper = houseMapper;
        this.landlordMapper = landlordMapper;
    }

    public PageResponse<HouseRecord> list(int pageNum, int pageSize, String keyword, RentalMode rentalMode, HouseStatus status) {
        Page<HouseEntity> page = houseMapper.selectPage(
            new Page<>(pageNum, pageSize),
            new LambdaQueryWrapper<HouseEntity>()
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                    .like(HouseEntity::getHouseName, keyword)
                    .or()
                    .like(HouseEntity::getHouseCode, keyword))
                .eq(rentalMode != null, HouseEntity::getRentalMode, rentalMode == null ? null : rentalMode.name())
                .eq(status != null, HouseEntity::getStatus, status == null ? null : status.name())
                .orderByDesc(HouseEntity::getId)
        );
        return new PageResponse<>(page.getRecords().stream().map(this::toRecord).toList(), page.getTotal(), pageNum, pageSize);
    }

    public HouseRecord get(Long id) {
        return toRecord(requireHouse(id));
    }

    public HouseRecord create(HouseUpsertRequest request) {
        HouseEntity entity = new HouseEntity();
        fillEntity(entity, request);
        entity.setHouseCode("HZ-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));
        entity.setOrgId(1L);
        entity.setCreatedBy(1L);
        entity.setUpdatedBy(1L);
        entity.setDeleted(0);
        houseMapper.insert(entity);
        return toRecord(entity);
    }

    public HouseRecord update(Long id, HouseUpsertRequest request) {
        HouseEntity entity = requireHouse(id);
        fillEntity(entity, request);
        entity.setUpdatedBy(1L);
        houseMapper.updateById(entity);
        return toRecord(entity);
    }

    public HouseRecord updateStatus(Long id, HouseStatus status) {
        HouseEntity entity = requireHouse(id);
        entity.setStatus(status.name());
        entity.setUpdatedBy(1L);
        houseMapper.updateById(entity);
        return toRecord(entity);
    }

    public void delete(Long id) {
        if (houseMapper.deleteById(id) == 0) {
            throw new BusinessException("House not found");
        }
    }

    private void fillEntity(HouseEntity entity, HouseUpsertRequest request) {
        entity.setHouseName(request.houseName());
        entity.setRentalMode(request.rentalMode().name());
        entity.setProjectName(request.projectName());
        entity.setAddress(request.address());
        entity.setArea(request.area());
        entity.setRentPrice(request.rentPrice());
        entity.setDepositPrice(BigDecimal.ZERO);
        entity.setStatus(request.status().name());
        entity.setLandlordName(request.landlordName());
        entity.setTenantName(request.tenantName());
        LandlordEntity landlord = landlordMapper.selectOne(new LambdaQueryWrapper<LandlordEntity>()
            .eq(LandlordEntity::getName, request.landlordName())
            .last("limit 1"));
        entity.setLandlordId(landlord == null ? null : landlord.getId());
    }

    private HouseEntity requireHouse(Long id) {
        HouseEntity entity = houseMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("House not found");
        }
        return entity;
    }

    private HouseRecord toRecord(HouseEntity entity) {
        return new HouseRecord(
            entity.getId(),
            entity.getHouseCode(),
            entity.getHouseName(),
            RentalMode.valueOf(entity.getRentalMode()),
            entity.getProjectName(),
            entity.getAddress(),
            entity.getArea(),
            entity.getRentPrice(),
            HouseStatus.valueOf(entity.getStatus()),
            entity.getLandlordName(),
            entity.getTenantName()
        );
    }

    public record HouseRecord(
        Long id,
        String houseCode,
        String houseName,
        RentalMode rentalMode,
        String projectName,
        String address,
        BigDecimal area,
        BigDecimal rentPrice,
        HouseStatus status,
        String landlordName,
        String tenantName
    ) {
    }

    public record HouseUpsertRequest(
        @NotBlank(message = "房源名称不能为空") String houseName,
        @NotNull(message = "租赁模式不能为空") RentalMode rentalMode,
        @NotBlank(message = "所属项目不能为空") String projectName,
        @NotBlank(message = "详细地址不能为空") String address,
        @NotNull(message = "面积不能为空") @DecimalMin(value = "0.1", message = "面积必须大于 0") BigDecimal area,
        @NotNull(message = "租金不能为空") @DecimalMin(value = "0.0", message = "租金不能小于 0") BigDecimal rentPrice,
        @NotNull(message = "状态不能为空") HouseStatus status,
        @NotBlank(message = "房东不能为空") String landlordName,
        String tenantName
    ) {
    }
}
