package model;

public class CouponUsage {
    private int usageId;
    private int couponId;
    private int userId;
    private int orderId;
    private String usedAt;
    
    // Fields mở rộng để hiển thị trên UI
    private String userEmail; 
    private String couponCode;

    public CouponUsage() {}

    public CouponUsage(int usageId, int couponId, int userId, int orderId, String usedAt, String userEmail, String couponCode) {
        this.usageId = usageId;
        this.couponId = couponId;
        this.userId = userId;
        this.orderId = orderId;
        this.usedAt = usedAt;
        this.userEmail = userEmail;
        this.couponCode = couponCode;
    }

    public int getUsageId() { return usageId; }
    public void setUsageId(int usageId) { this.usageId = usageId; }
    public int getCouponId() { return couponId; }
    public void setCouponId(int couponId) { this.couponId = couponId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getUsedAt() { return usedAt; }
    public void setUsedAt(String usedAt) { this.usedAt = usedAt; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
}
