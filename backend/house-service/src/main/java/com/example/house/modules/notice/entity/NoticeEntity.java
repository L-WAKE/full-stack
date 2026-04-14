package com.example.house.modules.notice.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.house.common.entity.BaseEntity;

import java.time.LocalDateTime;

@TableName("notice")
public class NoticeEntity extends BaseEntity {

    @TableId
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String status;
    private Integer pinned;
    private LocalDateTime publishTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getPinned() { return pinned; }
    public void setPinned(Integer pinned) { this.pinned = pinned; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
}
