package com.epayMall.pojo;

import java.math.BigDecimal;
import java.util.Date;

public class RefundInfo {
    private Integer id;

    private Integer userId;

    private Long orderNo;

    private Integer payPlatform;

    private String platformNumber;

    private String platformUserId;

    private String buyLoginId;

    private String refundStatus;

    private BigDecimal refundFee;

    private String refundDetailDesc;

    private String appId;
    
    private String refundReason;

    private Date createTime;

    private Date updateTime;

    public RefundInfo(Integer id, Integer userId, Long orderNo, Integer payPlatform, String platformNumber, String platformUserId, String buyLoginId, String refundStatus, BigDecimal refundFee, String refundDetailDesc, String appId, String refundReason, Date createTime, Date updateTime) {
        this.id = id;
        this.userId = userId;
        this.orderNo = orderNo;
        this.payPlatform = payPlatform;
        this.platformNumber = platformNumber;
        this.platformUserId = platformUserId;
        this.buyLoginId = buyLoginId;
        this.refundStatus = refundStatus;
        this.refundFee = refundFee;
        this.refundDetailDesc = refundDetailDesc;
        this.appId = appId;
        this.refundReason = refundReason;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public String getRefundReason() {
		return refundReason;
	}

	public void setRefundReason(String refundReason) {
		this.refundReason = refundReason;
	}

	public RefundInfo() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getPayPlatform() {
        return payPlatform;
    }

    public void setPayPlatform(Integer payPlatform) {
        this.payPlatform = payPlatform;
    }

    public String getPlatformNumber() {
        return platformNumber;
    }

    public void setPlatformNumber(String platformNumber) {
        this.platformNumber = platformNumber == null ? null : platformNumber.trim();
    }

    public String getPlatformUserId() {
        return platformUserId;
    }

    public void setPlatformUserId(String platformUserId) {
        this.platformUserId = platformUserId == null ? null : platformUserId.trim();
    }

    public String getBuyLoginId() {
        return buyLoginId;
    }

    public void setBuyLoginId(String buyLoginId) {
        this.buyLoginId = buyLoginId == null ? null : buyLoginId.trim();
    }

    public String getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus == null ? null : refundStatus.trim();
    }

    public BigDecimal getRefundFee() {
        return refundFee;
    }

    public void setRefundFee(BigDecimal refundFee) {
        this.refundFee = refundFee;
    }

    public String getRefundDetailDesc() {
        return refundDetailDesc;
    }

    public void setRefundDetailDesc(String refundDetailDesc) {
        this.refundDetailDesc = refundDetailDesc == null ? null : refundDetailDesc.trim();
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId == null ? null : appId.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}