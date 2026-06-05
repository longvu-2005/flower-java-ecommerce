package model;

public class Coupon {
    private int id;
    private String code;
    private int discountPercent;
    private String expiryDate;
    private int status; // 1 = active, 0 = inactive
    private double minOrderValue; // ĐIỀU KIỆN ÁP DỤNG MỚI

    public Coupon() {}

    public Coupon(int id, String code, int discountPercent, String expiryDate, int status, double minOrderValue) {
        this.id = id;
        this.code = code;
        this.discountPercent = discountPercent;
        this.expiryDate = expiryDate;
        this.status = status;
        this.minOrderValue = minOrderValue;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public int getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public double getMinOrderValue() { return minOrderValue; }
    public void setMinOrderValue(double minOrderValue) { this.minOrderValue = minOrderValue; }
}
