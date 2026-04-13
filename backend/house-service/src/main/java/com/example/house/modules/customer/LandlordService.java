package com.example.house.modules.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.house.common.api.PageResponse;
import com.example.house.common.exception.BusinessException;
import com.example.house.modules.customer.entity.LandlordEntity;
import com.example.house.modules.customer.mapper.LandlordMapper;
import com.example.house.modules.housing.entity.HouseEntity;
import com.example.house.modules.housing.mapper.HouseMapper;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LandlordService {

    private final LandlordMapper landlordMapper;
    private final HouseMapper houseMapper;

    public LandlordService(LandlordMapper landlordMapper, HouseMapper houseMapper) {
        this.landlordMapper = landlordMapper;
        this.houseMapper = houseMapper;
    }

    public PageResponse<LandlordRecord> list(int pageNum, int pageSize, String keyword) {
        Page<LandlordEntity> page = landlordMapper.selectPage(
            new Page<>(pageNum, pageSize),
            new LambdaQueryWrapper<LandlordEntity>()
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                    .like(LandlordEntity::getName, keyword)
                    .or()
                    .like(LandlordEntity::getMobile, keyword))
                .orderByDesc(LandlordEntity::getId)
        );
        return new PageResponse<>(page.getRecords().stream().map(this::toRecord).toList(), page.getTotal(), pageNum, pageSize);
    }

    public List<LandlordOption> options() {
        return landlordMapper.selectList(new LambdaQueryWrapper<LandlordEntity>().orderByDesc(LandlordEntity::getId))
            .stream()
            .map(item -> new LandlordOption(item.getId(), item.getName()))
            .toList();
    }

    public LandlordRecord create(LandlordUpsertRequest request) {
        LandlordEntity entity = new LandlordEntity();
        fillEntity(entity, request);
        entity.setOrgId(1L);
        entity.setCreatedBy(1L);
        entity.setUpdatedBy(1L);
        entity.setDeleted(0);
        landlordMapper.insert(entity);
        return toRecord(entity);
    }

    public LandlordRecord update(Long id, LandlordUpsertRequest request) {
        LandlordEntity entity = requireEntity(id);
        fillEntity(entity, request);
        entity.setUpdatedBy(1L);
        landlordMapper.updateById(entity);
        return toRecord(entity);
    }

    public void delete(Long id) {
        if (landlordMapper.deleteById(id) == 0) {
            throw new BusinessException("Landlord not found");
        }
    }

    private LandlordEntity requireEntity(Long id) {
        LandlordEntity entity = landlordMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("Landlord not found");
        }
        return entity;
    }

    private void fillEntity(LandlordEntity entity, LandlordUpsertRequest request) {
        entity.setName(request.name());
        entity.setMobile(request.mobile());
        entity.setIdNo(request.idNo());
        entity.setAddress(request.address());
        entity.setBankAccount(request.bankAccount());
        entity.setRemark(request.remark());
    }

    private LandlordRecord toRecord(LandlordEntity entity) {
        Long count = houseMapper.selectCount(new LambdaQueryWrapper<HouseEntity>()
            .eq(HouseEntity::getLandlordId, entity.getId()));
        return new LandlordRecord(entity.getId(), entity.getName(), entity.getMobile(), entity.getIdNo(), entity.getAddress(), entity.getBankAccount(), entity.getRemark(), count.intValue());
    }

    public record LandlordRecord(
        Long id,
        String name,
        String mobile,
        String idNo,
        String address,
        String bankAccount,
        String remark,
        int houseCount
    ) {
    }

    public record LandlordOption(Long id, String name) {
    }

    public record LandlordUpsertRequest(
        @NotBlank String name,
        @NotBlank String mobile,
        String idNo,
        String address,
        String bankAccount,
        String remark
    ) {
    }
}
