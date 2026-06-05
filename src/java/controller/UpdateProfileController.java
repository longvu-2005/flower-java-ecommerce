package controller;

import dal.DBContext;
import model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class UpdateProfileController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Account userSession = (Account) session.getAttribute("userSession");

        if (userSession == null) {
            response.sendRedirect("Login");
            return;
        }

        String fullname = request.getParameter("fullname");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");

        String sql = "UPDATE users SET fullname = ?, phone = ?, address = ? WHERE user_id = ?";
        try (Connection con = new DBContext().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, fullname);
            ps.setString(2, phone);
            ps.setString(3, address);
            ps.setInt(4, userSession.getUserId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                // Đồng bộ cập nhật lại dữ liệu mới vào Session để hiển thị ngay lập tức
                userSession.setFullname(fullname);
                userSession.setPhone(phone);
                userSession.setAddress(address);
                session.setAttribute("userSession", userSession);
                
                session.setAttribute("message", "Cập nhật thông tin tài khoản cá nhân thành công!");
            } else {
                session.setAttribute("error", "Không thể cập nhật, vui lòng kiểm tra lại hệ thống.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
        }
        response.sendRedirect("profilm"); // Chuyển hướng quay về nạp lại trang hồ sơ cá nhân
    }
}