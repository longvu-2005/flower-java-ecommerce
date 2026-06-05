package dao;

import dal.DBContext;
import model.Account;
import java.sql.*;

public class AccountDAO extends DBContext {

   // REGISTER: Đã sửa lại để trả về boolean (true nếu thành công, false nếu trùng email)
    public boolean register(Account acc) {
        String sql = "INSERT INTO users(fullname, email, [password], phone, [address], security_question, security_answer, role_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, 2)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, acc.getFullname());
            ps.setString(2, acc.getEmail());
            ps.setString(3, acc.getPassword());
            ps.setString(4, acc.getPhone());
            ps.setString(5, acc.getAddress());
            ps.setString(6, acc.getSecurityQuestion());
            ps.setString(7, acc.getSecurityAnswer()); 

            int row = ps.executeUpdate();
            return row > 0; // Trả về true nếu chèn dữ liệu thành công

        } catch (Exception e) {
            System.out.println("Lỗi đăng ký (Có thể do trùng Email): " + e.getMessage());
            return false; // Trả về false nếu bị lỗi trùng khóa Email
        }
    }

    // LOGIN: Đã thêm đầy đủ các trường dữ liệu để object Account không bị thiếu thông tin
    public Account login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND [password] = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account acc = new Account();
                    acc.setUserId(rs.getInt("user_id"));
                    acc.setFullname(rs.getString("fullname"));
                    acc.setEmail(rs.getString("email"));
                    acc.setPassword(rs.getString("password"));
                    acc.setPhone(rs.getString("phone"));
                    acc.setAddress(rs.getString("address"));
                    acc.setSecurityQuestion(rs.getString("security_question"));
                    acc.setSecurityAnswer(rs.getString("security_answer"));
                    acc.setRoleId(rs.getInt("role_id"));
                    return acc;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // QUÊN MẬT KHẨU: Logic reset dựa trên email và câu trả lời bảo mật
    public boolean resetPassword(String email, String answer, String newPassword) {
        String sql = "UPDATE users SET [password] = ? WHERE email = ? AND security_answer = ?";

        try (
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setString(2, email);
            ps.setString(3, answer);

            int row = ps.executeUpdate();
            return row > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Lấy danh sách tất cả khách hàng (role_id = 2)
    public java.util.List<Account> getAllCustomers() {
        java.util.List<Account> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM users WHERE role_id = 2 ORDER BY user_id DESC";
        
        try (
            java.sql.Connection con = getConnection();
            java.sql.PreparedStatement ps = con.prepareStatement(sql);
            java.sql.ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Account acc = new Account();
                acc.setUserId(rs.getInt("user_id"));
                acc.setFullname(rs.getString("fullname"));
                acc.setEmail(rs.getString("email"));
                // Không cần lấy password ra để bảo mật
                acc.setPhone(rs.getString("phone"));
                acc.setAddress(rs.getString("address"));
                acc.setRoleId(rs.getInt("role_id"));
                list.add(acc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // 1. Lấy thông tin cá nhân của 1 khách hàng
    public Account getAccountById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (java.sql.Connection con = getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account acc = new Account();
                    acc.setUserId(rs.getInt("user_id"));
                    acc.setFullname(rs.getString("fullname"));
                    acc.setEmail(rs.getString("email"));
                    acc.setPhone(rs.getString("phone"));
                    acc.setAddress(rs.getString("address"));
                    
                    // 🟢 THÊM 2 DÒNG NÀY ĐỂ LẤY CÂU HỎI VÀ CÂU TRẢ LỜI BẢO MẬT:
                    acc.setSecurityQuestion(rs.getString("security_question"));
                    acc.setSecurityAnswer(rs.getString("security_answer"));
                    
                    return acc;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // 2. Lấy lịch sử mua hàng (Nối bảng orders, order_details, products)
    public java.util.List<java.util.Map<String, String>> getCustomerOrders(int userId) {
        java.util.List<java.util.Map<String, String>> list = new java.util.ArrayList<>();
        String sql = "SELECT o.order_date, p.product_name, p.image, od.quantity, od.price, o.status " +
                     "FROM orders o " +
                     "JOIN order_details od ON o.order_id = od.order_id " +
                     "JOIN products p ON od.product_id = p.product_id " +
                     "WHERE o.user_id = ? ORDER BY o.order_date DESC";
        try (java.sql.Connection con = getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, String> map = new java.util.HashMap<>();
                    map.put("order_date", rs.getString("order_date"));
                    map.put("product_name", rs.getString("product_name"));
                    map.put("image", rs.getString("image"));
                    map.put("quantity", rs.getString("quantity"));
                    map.put("price", rs.getString("price"));
                    map.put("status", rs.getString("status"));
                    list.add(map);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 3. Lấy lịch sử đánh giá (Nối bảng reviews và products)
    public java.util.List<java.util.Map<String, String>> getCustomerReviews(int userId) {
        java.util.List<java.util.Map<String, String>> list = new java.util.ArrayList<>();
        String sql = "SELECT p.product_name, r.rating, r.comment, r.review_date " +
                     "FROM reviews r JOIN products p ON r.product_id = p.product_id " +
                     "WHERE r.user_id = ? ORDER BY r.review_date DESC";
        try (java.sql.Connection con = getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, String> map = new java.util.HashMap<>();
                    map.put("product_name", rs.getString("product_name"));
                    map.put("rating", rs.getString("rating"));
                    map.put("comment", rs.getString("comment"));
                    map.put("review_date", rs.getString("review_date"));
                    list.add(map);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}