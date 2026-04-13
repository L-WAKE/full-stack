package com.example.house.modules.dashboard;

import com.example.house.common.api.ApiResponse;
import com.example.house.modules.customer.entity.LandlordEntity;
import com.example.house.modules.customer.entity.TenantEntity;
import com.example.house.modules.customer.mapper.LandlordMapper;
import com.example.house.modules.customer.mapper.TenantMapper;
import com.example.house.modules.housing.entity.HouseEntity;
import com.example.house.modules.housing.mapper.HouseMapper;
import com.example.house.modules.workorder.entity.CleaningOrderEntity;
import com.example.house.modules.workorder.entity.MaintenanceOrderEntity;
import com.example.house.modules.workorder.mapper.CleaningOrderMapper;
import com.example.house.modules.workorder.mapper.MaintenanceOrderMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final HouseMapper houseMapper;
    private final TenantMapper tenantMapper;
    private final LandlordMapper landlordMapper;
    private final MaintenanceOrderMapper maintenanceOrderMapper;
    private final CleaningOrderMapper cleaningOrderMapper;

    public DashboardController(
        HouseMapper houseMapper,
        TenantMapper tenantMapper,
        LandlordMapper landlordMapper,
        MaintenanceOrderMapper maintenanceOrderMapper,
        CleaningOrderMapper cleaningOrderMapper
    ) {
        this.houseMapper = houseMapper;
        this.tenantMapper = tenantMapper;
        this.landlordMapper = landlordMapper;
        this.maintenanceOrderMapper = maintenanceOrderMapper;
        this.cleaningOrderMapper = cleaningOrderMapper;
    }

    @GetMapping("/overview")
    public ApiResponse<OverviewResponse> overview() {
        long totalHouse = houseMapper.selectCount(null);
        long occupiedHouse = houseMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HouseEntity>().eq(HouseEntity::getStatus, "OCCUPIED"));
        long totalTenant = tenantMapper.selectCount(null);
        long totalLandlord = landlordMapper.selectCount(null);
        long pendingMaintenance = maintenanceOrderMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MaintenanceOrderEntity>().in(MaintenanceOrderEntity::getStatus, "PENDING", "PROCESSING"));
        long pendingCleaning = cleaningOrderMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CleaningOrderEntity>().in(CleaningOrderEntity::getStatus, "PENDING_ASSIGN", "ASSIGNED"));
        double occupancyRate = totalHouse == 0 ? 0 : occupiedHouse * 100.0 / totalHouse;
        return ApiResponse.success(new OverviewResponse((int) totalHouse, (int) occupiedHouse, (int) totalTenant, (int) totalLandlord, (int) pendingMaintenance, (int) pendingCleaning, occupancyRate));
    }

    @GetMapping("/trend")
    public ApiResponse<TrendResponse> trend() {
        return ApiResponse.success(new TrendResponse(
            new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"},
            new int[]{76, 80, 84, 89, 92, 94, 96},
            new int[]{4, 6, 5, 7, 9, 8, 6}
        ));
    }

    public record OverviewResponse(
        int totalHouseCount,
        int occupiedHouseCount,
        int totalTenantCount,
        int totalLandlordCount,
        int pendingMaintenanceCount,
        int pendingCleaningCount,
        double occupancyRate
    ) {
    }

    public record TrendResponse(String[] labels, int[] occupancyRates, int[] newOrders) {
    }
}
