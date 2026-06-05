package dao;

import dal.DBContext;
import model.Coupon;
import model.CouponUsage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CouponDao {

    // (GIỮ NGUYÊN) Hàm cũ để tương thích
    public int getDiscountPercent(String code) {
        String sql = "SELECT discount_percent FROM coupons WHERE code = ? AND [status] = 1 AND (expiry_date IS NULL OR expiry_date >= GETDATE())";
        try (Connection con = new DBContext().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("discount_percent");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // [THÊM MỚI] Lấy nguyên object Coupon để check minOrderValue
    public Coupon getCouponByCode(String code) {
        String sql = "SELECT * FROM coupons WHERE code = ? AND [status] = 1 AND (expiry_date IS NULL OR expiry_date >= GETDATE())";
        try (Connection con = new DBContext().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Coupon(rs.getInt("coupon_id"), rs.getString("code"), rs.getInt("discount_percent"), 
                                      rs.getString("expiry_date"), rs.getInt("status"), rs.getDouble("min_order_value"));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // [CẬP NHẬT] Thêm min_order_value
    public List<Coupon> getAllCoupons() {
        List<Coupon> list = new ArrayList<>();
        String sql = "SELECT coupon_id, code, discount_percent, expiry_date, [status], min_order_value FROM coupons ORDER BY coupon_id DESC";
        try (Connection con = new DBContext().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Coupon(
                    rs.getInt("coupon_id"), rs.getString("code"), rs.getInt("discount_percent"),
                    rs.getString("expiry_date") != null ? rs.getString("expiry_date").substring(0, 10) : "",
                    rs.getInt("status"), rs.getDouble("min_order_value")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // [CẬP NHẬT] Thêm minOrderValue
    public boolean insertCoupon(String code, int discountPercent, String expiryDate, int status, double minOrderValue) {
        String sql = "INSERT INTO coupons (code, discount_percent, expiry_date, [status], min_order_value) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, discountPercent);
            if (expiryDate != null && !expiryDate.isEmpty()) ps.setString(3, expiryDate); else ps.setNull(3, java.sql.Types.DATE);
            ps.setInt(4, status);
            ps.setDouble(5, minOrderValue);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // [CẬP NHẬT] Thêm minOrderValue
    public boolean updateCoupon(int id, String code, int discountPercent, String expiryDate, int status, double minOrderValue) {
        String sql = "UPDATE coupons SET code=?, discount_percent=?, expiry_date=?, [status]=?, min_order_value=? WHERE coupon_id=?";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, discountPercent);
            if (expiryDate != null && !expiryDate.isEmpty()) ps.setString(3, expiryDate); else ps.setNull(3, java.sql.Types.DATE);
            ps.setInt(4, status);
            ps.setDouble(5, minOrderValue);
            ps.setInt(6, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // [THÊM MỚI] Ghi nhận lịch sử dùng mã
    public void recordUsage(int couponId, int userId, int orderId) {
        String sql = "INSERT INTO coupon_usage (coupon_id, user_id, order_id) VALUES (?, ?, ?)";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, couponId);
            ps.setInt(2, userId);
            ps.setInt(3, orderId);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // [THÊM MỚI] Lấy lịch sử của 1 mã
    public List<CouponUsage> getCouponUsageHistory(int couponId) {
        List<CouponUsage> list = new ArrayList<>();
        String sql = "SELECT cu.*, u.email, c.code FROM coupon_usage cu " +
                     "JOIN users u ON cu.user_id = u.user_id " +
                     "JOIN coupons c ON cu.coupon_id = c.coupon_id " +
                     "WHERE cu.coupon_id = ? ORDER BY cu.used_at DESC";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, couponId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CouponUsage(
                        rs.getInt("usage_id"), rs.getInt("coupon_id"), rs.getInt("user_id"),
                        rs.getInt("order_id"), rs.getString("used_at"), rs.getString("email"), rs.getString("code")
                    ));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean deleteCoupon(int id) {
        String sql = "DELETE FROM coupons WHERE coupon_id = ?";
        try (Connection con = new DBContext().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void disableCoupon(String code) {
        String sql = "UPDATE coupons SET [status] = 0 WHERE code = ?";
        try (Connection con = new DBContext().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String generateCoupon() {
        String code = "VIP3M-" + (int)(Math.random() * 90000 + 10000);
        String sql = "INSERT INTO coupons (code, discount_percent, [status]) VALUES (?, 10, 1)";
        try (Connection con = new DBContext().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.executeUpdate();
            return code;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}