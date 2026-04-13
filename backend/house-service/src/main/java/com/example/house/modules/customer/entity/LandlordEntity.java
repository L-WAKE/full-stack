package com.example.house.modules.customer.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.house.common.entity.BaseEntity;

@TableName("landlord")
public class LandlordEntity extends BaseEntity {

    @TableId
    private Long id;
    private String name;
    private String mobile;
    private String idNo;
    private String bankAccount;
    private String address;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getIdNo() { return idNo; }
    public void setIdNo(String idNo) { this.idNo = idNo; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
