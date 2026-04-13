package com.example.house.modules.housing.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.house.common.entity.BaseEntity;

import java.math.BigDecimal;

@TableName("house")
public class HouseEntity extends BaseEntity {

    @TableId
    private Long id;
    private String houseCode;
    private String houseName;
    private String rentalMode;
    private String projectName;
    private String communityName;
    private String address;
    private String layoutDesc;
    private BigDecimal area;
    private BigDecimal rentPrice;
    private BigDecimal depositPrice;
    private String status;
    private Long landlordId;
    private String landlordName;
    private String tenantName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHouseCode() { return houseCode; }
    public void setHouseCode(String houseCode) { this.houseCode = houseCode; }
    public String getHouseName() { return houseName; }
    public void setHouseName(String houseName) { this.houseName = houseName; }
    public String getRentalMode() { return rentalMode; }
    public void setRentalMode(String rentalMode) { this.rentalMode = rentalMode; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getCommunityName() { return communityName; }
    public void setCommunityName(String communityName) { this.communityName = communityName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getLayoutDesc() { return layoutDesc; }
    public void setLayoutDesc(String layoutDesc) { this.layoutDesc = layoutDesc; }
    public BigDecimal getArea() { return area; }
    public void setArea(BigDecimal area) { this.area = area; }
    public BigDecimal getRentPrice() { return rentPrice; }
    public void setRentPrice(BigDecimal rentPrice) { this.rentPrice = rentPrice; }
    public BigDecimal getDepositPrice() { return depositPrice; }
    public void setDepositPrice(BigDecimal depositPrice) { this.depositPrice = depositPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getLandlordId() { return landlordId; }
    public void setLandlordId(Long landlordId) { this.landlordId = landlordId; }
    public String getLandlordName() { return landlordName; }
    public void setLandlordName(String landlordName) { this.landlordName = landlordName; }
    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
}
