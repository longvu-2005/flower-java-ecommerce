package dao;

import dal.DBContext;
import java.sql.*;
import java.util.*;

public class MessageDao {

    private static boolean isInitialized = false;

    public MessageDao() {
        if (!isInitialized) {
            try (Connection con = new DBContext().getConnection(); Statement st = con.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT count(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'support_messages' AND COLUMN_NAME = 'is_read'");
                if (rs.next() && rs.getInt(1) == 0) {
                    st.execute("ALTER TABLE support_messages ADD is_read BIT DEFAULT 0");
                }
                isInitialized = true;
            } catch (Exception e) {}
        }
    }

    // 1. Quét danh sách những khách hàng đã từng nhắn tin
    public List<Map<String, String>> getChattedCustomers() {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT DISTINCT client_id, client_email FROM support_messages";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, String> map = new HashMap<>();
                map.put("clientId", String.valueOf(rs.getInt("client_id")));
                map.put("email", rs.getString("client_email"));
                list.add(map);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 2. Lấy toàn bộ lịch sử trò chuyện của 1 khách hàng cụ thể
    public List<Map<String, String>> getMessagesByClient(int clientId) {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT sender_role, message_text FROM support_messages WHERE client_id = ? ORDER BY created_at ASC";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("role", rs.getString("sender_role"));
                    map.put("text", rs.getString("message_text"));
                    list.add(map);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 3. Lưu tin nhắn mới vào SQL
    public boolean saveMessage(int clientId, String clientEmail, String senderRole, String messageText) {
        String sql = "INSERT INTO support_messages (client_id, client_email, sender_role, message_text) VALUES (?, ?, ?, ?)";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ps.setString(2, clientEmail);
            ps.setString(3, senderRole);
            ps.setString(4, messageText);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Đếm số tin nhắn chưa đọc của Admin (từ User)
    public int getUnreadCountForAdmin() {
        String sql = "SELECT COUNT(*) FROM support_messages WHERE sender_role = 'user' AND is_read = 0";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {}
        return 0;
    }

    // Đếm số tin nhắn chưa đọc của một User cụ thể (từ Admin)
    public int getUnreadCountForUser(int clientId) {
        String sql = "SELECT COUNT(*) FROM support_messages WHERE client_id = ? AND sender_role = 'admin' AND is_read = 0";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {}
        return 0;
    }

    // Đánh dấu tin nhắn là đã đọc (Khi Admin mở chat)
    public void markAsReadByAdmin(int clientId) {
        String sql = "UPDATE support_messages SET is_read = 1 WHERE client_id = ? AND sender_role = 'user' AND is_read = 0";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ps.executeUpdate();
        } catch (Exception e) {}
    }

    // Đánh dấu tin nhắn là đã đọc (Khi User mở chat)
    public void markAsReadByUser(int clientId) {
        String sql = "UPDATE support_messages SET is_read = 1 WHERE client_id = ? AND sender_role = 'admin' AND is_read = 0";
        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ps.executeUpdate();
        } catch (Exception e) {}
    }
}