package dao;

import dal.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class CartDao {
    
    // 1. Lấy mã giỏ hàng, đã sửa account_id thành user_id CHUẨN SQL
    public int getOrCreateCartId(int userId) {
        String selectSql = "SELECT cart_id FROM cart WHERE user_id = ?";
        String insertSql = "INSERT INTO cart (user_id) VALUES (?)";
        
        try (Connection con = new DBContext().getConnection();
             PreparedStatement psSel = con.prepareStatement(selectSql)) {
            
            psSel.setInt(1, userId);
            try (ResultSet rs = psSel.executeQuery()) {
                if (rs.next()) return rs.getInt("cart_id");
            }
            
            try (PreparedStatement psIns = con.prepareStatement(insertSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                psIns.setInt(1, userId);
                psIns.executeUpdate();
                try (ResultSet rsKeys = psIns.getGeneratedKeys()) {
                    if (rsKeys.next()) return rsKeys.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<Integer, Integer> loadCartItems(int cartId) {
        Map<Integer, Integer> items = new HashMap<>();
        String sql = "SELECT product_id, quantity FROM cart_items WHERE cart_id = ?";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) items.put(rs.getInt("product_id"), rs.getInt("quantity"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return items;
    }

    public void saveOrUpdateCartItem(int cartId, int productId, int quantity) {
        if (cartId <= 0) return; 
        String checkSql = "SELECT quantity FROM cart_items WHERE cart_id = ? AND product_id = ?";
        String updateSql = "UPDATE cart_items SET quantity = ? WHERE cart_id = ? AND product_id = ?";
        String insertSql = "INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?)";
        
        try (Connection con = new DBContext().getConnection()) {
            boolean exists = false;
            try (PreparedStatement psCheck = con.prepareStatement(checkSql)) {
                psCheck.setInt(1, cartId); psCheck.setInt(2, productId);
                try (ResultSet rs = psCheck.executeQuery()) { if (rs.next()) exists = true; }
            }
            if (exists) {
                try (PreparedStatement psUp = con.prepareStatement(updateSql)) {
                    psUp.setInt(1, quantity); psUp.setInt(2, cartId); psUp.setInt(3, productId);
                    psUp.executeUpdate();
                }
            } else {
                try (PreparedStatement psIn = con.prepareStatement(insertSql)) {
                    psIn.setInt(1, cartId); psIn.setInt(2, productId); psIn.setInt(3, quantity);
                    psIn.executeUpdate();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void removeCartItem(int cartId, int productId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cartId); ps.setInt(2, productId); ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void clearCart(int cartId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cartId); ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}