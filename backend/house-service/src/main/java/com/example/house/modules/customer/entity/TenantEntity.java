package com.example.house.modules.customer.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.house.common.entity.BaseEntity;

@TableName("tenant")
public class TenantEntity extends BaseEntity {

    @TableId
    private Long id;
    private String name;
    private String mobile;
    private String gender;
    private String emergencyContact;
    private String emergencyMobile;
    private String houseName;
    private String checkinDate;
    private String checkoutDate;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    public String getEmergencyMobile() { return emergencyMobile; }
    public void setEmergencyMobile(String emergencyMobile) { this.emergencyMobile = emergencyMobile; }
    public String getHouseName() { return houseName; }
    public void setHouseName(String houseName) { this.houseName = houseName; }
    public String getCheckinDate() { return checkinDate; }
    public void setCheckinDate(String checkinDate) { this.checkinDate = checkinDate; }
    public String getCheckoutDate() { return checkoutDate; }
    public void setCheckoutDate(String checkoutDate) { this.checkoutDate = checkoutDate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
