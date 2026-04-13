package com.example.house.modules.workorder;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.house.common.api.PageResponse;
import com.example.house.common.exception.BusinessException;
import com.example.house.modules.workorder.entity.CleaningOrderEntity;
import com.example.house.modules.workorder.entity.MaintenanceOrderEntity;
import com.example.house.modules.workorder.mapper.CleaningOrderMapper;
import com.example.house.modules.workorder.mapper.MaintenanceOrderMapper;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class WorkOrderService {

    private final MaintenanceOrderMapper maintenanceOrderMapper;
    private final CleaningOrderMapper cleaningOrderMapper;

    public WorkOrderService(MaintenanceOrderMapper maintenanceOrderMapper, CleaningOrderMapper cleaningOrderMapper) {
        this.maintenanceOrderMapper = maintenanceOrderMapper;
        this.cleaningOrderMapper = cleaningOrderMapper;
    }

    public PageResponse<MaintenanceOrderRecord> listMaintenance(int pageNum, int pageSize, String keyword) {
        Page<MaintenanceOrderEntity> page = maintenanceOrderMapper.selectPage(
            new Page<>(pageNum, pageSize),
            new LambdaQueryWrapper<MaintenanceOrderEntity>()
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                    .like(MaintenanceOrderEntity::getOrderNo, keyword)
                    .or()
                    .like(MaintenanceOrderEntity::getHouseName, keyword))
                .orderByDesc(MaintenanceOrderEntity::getId)
        );
        return new PageResponse<>(page.getRecords().stream().map(this::toMaintenanceRecord).toList(), page.getTotal(), pageNum, pageSize);
    }

    public PageResponse<CleaningOrderRecord> listCleaning(int pageNum, int pageSize, String keyword) {
        Page<CleaningOrderEntity> page = cleaningOrderMapper.selectPage(
            new Page<>(pageNum, pageSize),
            new LambdaQueryWrapper<CleaningOrderEntity>()
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                    .like(CleaningOrderEntity::getOrderNo, keyword)
                    .or()
                    .like(CleaningOrderEntity::getHouseName, keyword))
                .orderByDesc(CleaningOrderEntity::getId)
        );
        return new PageResponse<>(page.getRecords().stream().map(this::toCleaningRecord).toList(), page.getTotal(), pageNum, pageSize);
    }

    public MaintenanceOrderRecord createMaintenance(MaintenanceUpsertRequest request) {
        MaintenanceOrderEntity entity = new MaintenanceOrderEntity();
        entity.setOrderNo("WX-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));
        fillMaintenance(entity, request);
        entity.setOrgId(1L);
        entity.setCreatedBy(1L);
        entity.setUpdatedBy(1L);
        entity.setDeleted(0);
        maintenanceOrderMapper.insert(entity);
        return toMaintenanceRecord(entity);
    }

    public MaintenanceOrderRecord updateMaintenance(Long id, MaintenanceUpsertRequest request) {
        MaintenanceOrderEntity entity = requireMaintenance(id);
        fillMaintenance(entity, request);
        maintenanceOrderMapper.updateById(entity);
        return toMaintenanceRecord(entity);
    }

    public void deleteMaintenance(Long id) {
        if (maintenanceOrderMapper.deleteById(id) == 0) {
            throw new BusinessException("Maintenance order not found");
        }
    }

    public CleaningOrderRecord createCleaning(CleaningUpsertRequest request) {
        CleaningOrderEntity entity = new CleaningOrderEntity();
        entity.setOrderNo("BJ-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));
        fillCleaning(entity, request);
        entity.setOrgId(1L);
        entity.setCreatedBy(1L);
        entity.setUpdatedBy(1L);
        entity.setDeleted(0);
        cleaningOrderMapper.insert(entity);
        return toCleaningRecord(entity);
    }

    public CleaningOrderRecord updateCleaning(Long id, CleaningUpsertRequest request) {
        CleaningOrderEntity entity = requireCleaning(id);
        fillCleaning(entity, request);
        cleaningOrderMapper.updateById(entity);
        return toCleaningRecord(entity);
    }

    public void deleteCleaning(Long id) {
        if (cleaningOrderMapper.deleteById(id) == 0) {
            throw new BusinessException("Cleaning order not found");
        }
    }

    private void fillMaintenance(MaintenanceOrderEntity entity, MaintenanceUpsertRequest request) {
        entity.setHouseName(request.houseName());
        entity.setIssueType(request.issueType());
        entity.setReporterName(request.reporterName());
        entity.setAssigneeName(request.assigneeName());
        entity.setPriority(request.priority());
        entity.setStatus(request.status());
    }

    private void fillCleaning(CleaningOrderEntity entity, CleaningUpsertRequest request) {
        entity.setHouseName(request.houseName());
        entity.setCleaningType(request.cleaningType());
        entity.setAppointmentTime(request.appointmentTime());
        entity.setAssigneeName(request.assigneeName());
        entity.setStatus(request.status());
    }

    private MaintenanceOrderEntity requireMaintenance(Long id) {
        MaintenanceOrderEntity entity = maintenanceOrderMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("Maintenance order not found");
        }
        return entity;
    }

    private CleaningOrderEntity requireCleaning(Long id) {
        CleaningOrderEntity entity = cleaningOrderMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("Cleaning order not found");
        }
        return entity;
    }

    private MaintenanceOrderRecord toMaintenanceRecord(MaintenanceOrderEntity entity) {
        return new MaintenanceOrderRecord(
            entity.getId(),
            entity.getOrderNo(),
            entity.getHouseName(),
            entity.getIssueType(),
            entity.getReporterName(),
            entity.getAssigneeName(),
            entity.getPriority(),
            entity.getStatus()
        );
    }

    private CleaningOrderRecord toCleaningRecord(CleaningOrderEntity entity) {
        return new CleaningOrderRecord(
            entity.getId(),
            entity.getOrderNo(),
            entity.getHouseName(),
            entity.getCleaningType(),
            entity.getAppointmentTime(),
            entity.getAssigneeName(),
            entity.getStatus()
        );
    }

    public record MaintenanceOrderRecord(Long id, String orderNo, String houseName, String issueType, String reporterName, String assigneeName, String priority, String status) {
    }

    public record CleaningOrderRecord(Long id, String orderNo, String houseName, String cleaningType, String appointmentTime, String assigneeName, String status) {
    }

    public record MaintenanceUpsertRequest(@NotBlank String houseName, @NotBlank String issueType, String reporterName, String assigneeName, String priority, String status) {
    }

    public record CleaningUpsertRequest(@NotBlank String houseName, @NotBlank String cleaningType, String appointmentTime, String assigneeName, String status) {
    }
}
