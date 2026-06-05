package dao;

import dal.DBContext;
import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    // 1. TỔNG ĐƠN HÀNG (Ẩn các đơn chưa hoàn tất thanh toán 'Unpaid')
    public int getTotalOrders() {
        String query = "SELECT COUNT(*) FROM orders WHERE status != 'Unpaid'";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // 2. DOANH THU: CHỈ TÍNH NHỮNG ĐƠN ĐÃ GIAO & NHẬN TIỀN (Completed)
    public double getTotalRevenue() {
        String query = "SELECT SUM(total_price) FROM orders WHERE status = 'Completed'";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // 3. TỔNG CHI TIÊU USER: CHỈ TÍNH NHỮNG ĐƠN ĐÃ GIAO (Completed)
    public double getTotalMoneyByUserId(int userId) {
        String sql = "SELECT SUM(total_price) FROM orders WHERE user_id = ? AND status = 'Completed'";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // 4. LẤY LỊCH SỬ ĐƠN (USER) - 🟢 ĐÃ FIX: Ẩn các đơn 'Unpaid' đang bị khách bỏ dở ở trang Payment
    public List<Map<String, Object>> getOrderHistoryByUserId(int userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT order_id, order_date, total_price, status, payment_method FROM orders WHERE user_id = ? AND status != 'Unpaid' ORDER BY order_date DESC";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("order_id", rs.getInt("order_id"));
                    map.put("order_date", rs.getString("order_date"));
                    map.put("total_price", rs.getDouble("total_price"));
                    map.put("status", rs.getString("status"));
                    map.put("payment_method", rs.getString("payment_method"));
                    list.add(map);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 5. LẤY DANH SÁCH ĐƠN CHO ADMIN - 🟢 ĐÃ FIX: Không cho Admin thấy rác (các đơn khách chưa chốt)
    public List<Map<String, Object>> getAllOrdersForAdmin() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT order_id, sender_name, receiver_name, order_date, total_price, status, payment_method FROM orders WHERE status != 'Unpaid' ORDER BY order_date DESC";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("order_id", rs.getInt("order_id"));
                map.put("sender_name", rs.getString("sender_name"));
                map.put("receiver_name", rs.getString("receiver_name"));
                map.put("order_date", rs.getString("order_date"));
                map.put("total_price", rs.getDouble("total_price"));
                map.put("status", rs.getString("status"));
                map.put("payment_method", rs.getString("payment_method"));
                list.add(map);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 6. LƯU ĐƠN HÀNG (CHỈ TẠO ĐƠN, KHÔNG TRỪ KHO Ở BƯỚC NÀY)
    public int saveOrder(int userId, String sName, String sPhone, String sEmail, String rName, String rPhone, String rAddress, String rCity, String rDistrict, String delivDate, String delivTime, String cardMsg, String specialRequests, double subTotal, double vat, double grandTotal, Map<Integer, Integer> cart, String couponCode, double discountAmount, String paymentMethod) {
        int orderId = 0;
        Connection con = null;
        PreparedStatement psOrder = null;
        PreparedStatement psDetail = null;
        ResultSet rs = null;

        String sqlOrder = "INSERT INTO orders (user_id, sender_name, sender_phone, sender_email, receiver_name, receiver_phone, receiver_address, receiver_city, receiver_district, delivery_date, delivery_time, card_message, special_requests, total_price, vat_amount, coupon_code, discount_amount, payment_method, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Unpaid')";
        String sqlDetail = "INSERT INTO order_details (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";

        // FETCH PRICES BEFORE TRANSACTION TO PREVENT DEADLOCK
        ProductDao pDao = new ProductDao();
        Map<Integer, Double> productPrices = new HashMap<>();
        for (Integer productId : cart.keySet()) {
            model.Product p = pDao.getProductById(productId);
            double finalPrice = p.getPprice();
            if (p.getDiscount() > 0) {
                finalPrice = p.getPprice() * (1 - (double) p.getDiscount() / 100);
            }
            productPrices.put(productId, finalPrice);
        }

        try {
            con = new DBContext().getConnection();
            con.setAutoCommit(false); 

            // Lưu bảng Orders
            psOrder = con.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, userId);
            psOrder.setString(2, sName);
            psOrder.setString(3, sPhone);
            psOrder.setString(4, sEmail);
            psOrder.setString(5, rName);
            psOrder.setString(6, rPhone);
            psOrder.setString(7, rAddress);
            psOrder.setString(8, rCity);
            psOrder.setString(9, rDistrict);
            
            if (delivDate == null || delivDate.trim().isEmpty()) {
                psOrder.setNull(10, java.sql.Types.DATE);
            } else {
                psOrder.setDate(10, java.sql.Date.valueOf(delivDate));
            }
            
            psOrder.setString(11, delivTime);
            psOrder.setString(12, cardMsg);
            psOrder.setString(13, specialRequests);
            psOrder.setDouble(14, grandTotal); 
            psOrder.setDouble(15, vat);
            
            if (couponCode == null || couponCode.trim().isEmpty()) {
                psOrder.setNull(16, java.sql.Types.VARCHAR);
            } else {
                psOrder.setString(16, couponCode);
            }
            
            psOrder.setDouble(17, discountAmount);
            psOrder.setString(18, paymentMethod);

            psOrder.executeUpdate();
            rs = psOrder.getGeneratedKeys();
            if (rs.next()) { orderId = rs.getInt(1); }

            // Lưu bảng order_details (ĐÃ BỎ LỆNH TRỪ KHO)
            psDetail = con.prepareStatement(sqlDetail);
            
            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();
                double finalPrice = productPrices.get(productId);

                psDetail.setInt(1, orderId);
                psDetail.setInt(2, productId);
                psDetail.setInt(3, quantity);
                psDetail.setDouble(4, finalPrice);
                psDetail.addBatch(); 
            }
            psDetail.executeBatch();
            
            con.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (con != null) { try { con.rollback(); } catch (SQLException ex) {} }
            return 0;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) {}
            if (psDetail != null) try { psDetail.close(); } catch (SQLException ex) {}
            if (psOrder != null) try { psOrder.close(); } catch (SQLException ex) {}
            if (con != null) try { con.close(); } catch (SQLException ex) {}
        }
        return orderId;
    }

    // 7. CẬP NHẬT TRẠNG THÁI VÀ THỰC HIỆN TRỪ KHO KHI KHÁCH CHỐT ĐƠN
    public void updatePaymentMethod(int orderId, String method, String status) {
        String sqlUpdateOrder = "UPDATE orders SET payment_method = ?, status = ? WHERE order_id = ?";
        String sqlGetDetails = "SELECT product_id, quantity FROM order_details WHERE order_id = ?";
        String sqlUpdateStock = "UPDATE products SET stock = stock - ? WHERE product_id = ?";
        
        Connection con = null;
        try {
            con = new DBContext().getConnection();
            con.setAutoCommit(false);
            
            try (PreparedStatement psOrder = con.prepareStatement(sqlUpdateOrder)) {
                psOrder.setString(1, method);
                psOrder.setString(2, status);
                psOrder.setInt(3, orderId);
                psOrder.executeUpdate();
            }
            
            try (PreparedStatement psGet = con.prepareStatement(sqlGetDetails);
                 PreparedStatement psStock = con.prepareStatement(sqlUpdateStock)) {
                psGet.setInt(1, orderId);
                try (ResultSet rs = psGet.executeQuery()) {
                    while (rs.next()) {
                        psStock.setInt(1, rs.getInt("quantity"));
                        psStock.setInt(2, rs.getInt("product_id"));
                        psStock.addBatch();
                    }
                }
                psStock.executeBatch();
            }
            
            con.commit();
        } catch (Exception e) { 
            e.printStackTrace();
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) {}
            }
        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException ex) {}
            }
        }
    }

    public boolean updateOrderStatusByAdmin(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean cancelOrderByUser(int orderId, int userId) {
        String sql = "UPDATE orders SET status = 'Cancelled' WHERE order_id = ? AND user_id = ? AND status = 'Pending'";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public String getLatestValidPaymentMethod(int userId) {
        String sql = "SELECT TOP 1 payment_method FROM orders WHERE user_id = ? AND payment_method IS NOT NULL AND payment_method <> N'Chưa chọn' ORDER BY order_date DESC";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("payment_method");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "Chưa chọn";
    }

    public Map<String, String> getOrderSummaryById(int orderId) {
        String sql = "SELECT order_id, total_price FROM orders WHERE order_id = ?";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("order_id", String.valueOf(rs.getInt("order_id")));
                    map.put("total_price", String.valueOf(rs.getDouble("total_price")));
                    return map;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public Map<String, Object> getOrderFullDetailsById(int orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("order_id", rs.getInt("order_id"));
                    map.put("user_id", rs.getInt("user_id"));
                    map.put("sender_name", rs.getString("sender_name"));
                    map.put("sender_phone", rs.getString("sender_phone"));
                    map.put("sender_email", rs.getString("sender_email"));
                    map.put("receiver_name", rs.getString("receiver_name"));
                    map.put("receiver_phone", rs.getString("receiver_phone"));
                    map.put("receiver_address", rs.getString("receiver_address"));
                    map.put("receiver_city", rs.getString("receiver_city"));
                    map.put("receiver_district", rs.getString("receiver_district"));
                    map.put("delivery_date", rs.getString("delivery_date"));
                    map.put("delivery_time", rs.getString("delivery_time"));
                    map.put("card_message", rs.getString("card_message"));
                    map.put("special_requests", rs.getString("special_requests"));
                    map.put("total_price", rs.getDouble("total_price"));
                    map.put("vat_amount", rs.getDouble("vat_amount"));
                    map.put("coupon_code", rs.getString("coupon_code"));
                    map.put("discount_amount", rs.getDouble("discount_amount"));
                    map.put("payment_method", rs.getString("payment_method"));
                    map.put("status", rs.getString("status"));
                    map.put("order_date", rs.getString("order_date"));
                    return map;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public List<Map<String, Object>> getOrderLineItems(int orderId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT od.product_id, od.quantity, od.price, p.product_name, p.image FROM order_details od JOIN products p ON od.product_id = p.product_id WHERE od.order_id = ?";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("product_id", rs.getInt("product_id"));
                    map.put("quantity", rs.getInt("quantity"));
                    map.put("price", rs.getDouble("price"));
                    map.put("product_name", rs.getString("product_name"));
                    map.put("image", rs.getString("image"));
                    list.add(map);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateOrderDeliveryAndMessages(int orderId, int userId, String rName, String rPhone, String rAddress, String rDistrict, String rCity, String delivDate, String delivTime, String specialRequests, String cardMessage) {
        String sql = "UPDATE orders SET receiver_name=?, receiver_phone=?, receiver_address=?, receiver_district=?, receiver_city=?, delivery_date=?, delivery_time=?, special_requests=?, card_message=? WHERE order_id=? AND user_id=? AND (status='Pending' OR status='Processing')";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rName); ps.setString(2, rPhone); ps.setString(3, rAddress);
            ps.setString(4, rDistrict); ps.setString(5, rCity); ps.setString(6, delivDate);
            ps.setString(7, delivTime); ps.setString(8, specialRequests); ps.setString(9, cardMessage);
            ps.setInt(10, orderId); ps.setInt(11, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean insertReview(int userId, int productId, int rating, String comment) {
        String sql = "INSERT INTO reviews (user_id, product_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setInt(2, productId); ps.setInt(3, rating); ps.setString(4, comment);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}