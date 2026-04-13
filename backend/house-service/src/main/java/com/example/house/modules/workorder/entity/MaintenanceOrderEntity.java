package com.example.house.modules.workorder.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.house.common.entity.BaseEntity;

@TableName("maintenance_order")
public class MaintenanceOrderEntity extends BaseEntity {

    @TableId
    private Long id;
    private String orderNo;
    private String houseName;
    private String issueType;
    private String reporterName;
    private String assigneeName;
    private String priority;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getHouseName() { return houseName; }
    public void setHouseName(String houseName) { this.houseName = houseName; }
    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
