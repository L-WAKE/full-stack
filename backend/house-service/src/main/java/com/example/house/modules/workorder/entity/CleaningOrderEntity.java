package com.example.house.modules.workorder.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.house.common.entity.BaseEntity;

@TableName("cleaning_order")
public class CleaningOrderEntity extends BaseEntity {

    @TableId
    private Long id;
    private String orderNo;
    private String houseName;
    private String cleaningType;
    private String appointmentTime;
    private String assigneeName;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getHouseName() { return houseName; }
    public void setHouseName(String houseName) { this.houseName = houseName; }
    public String getCleaningType() { return cleaningType; }
    public void setCleaningType(String cleaningType) { this.cleaningType = cleaningType; }
    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }
    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
