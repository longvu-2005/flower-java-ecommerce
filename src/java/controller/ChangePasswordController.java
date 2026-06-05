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

public class ChangePasswordController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Account userSession = (Account) session.getAttribute("userSession");

        if (userSession == null) {
            response.sendRedirect("Login");
            return;
        }

        String oldPass = request.getParameter("oldPass");
        String newPass = request.getParameter("newPass");
        String reNewPass = request.getParameter("reNewPass");

        // 1. Kiểm tra mật khẩu cũ xem nhập có chính xác không
        if (!userSession.getPassword().equals(oldPass)) {
            session.setAttribute("error", "Mật khẩu hiện tại nhập vào không chính xác!");
            response.sendRedirect("profilm");
            return;
        }

        // 2. Kiểm tra mật khẩu mới và xác nhận mật khẩu có trùng khớp không
        if (!newPass.equals(reNewPass)) {
            session.setAttribute("error", "Mật khẩu mới và mật khẩu xác nhận không khớp nhau!");
            response.sendRedirect("profilm");
            return;
        }

        // 3. Tiến hành cập nhật mật khẩu mới vào Database SQL
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection con = new DBContext().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, newPass);
            ps.setInt(2, userSession.getUserId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                userSession.setPassword(newPass); // Cập nhật lại mật khẩu trong Session
                session.setAttribute("userSession", userSession);
                session.setAttribute("message", "Thay đổi mật khẩu tài khoản thành công!");
            } else {
                session.setAttribute("error", "Đổi mật khẩu thất bại. Vui lòng thử lại sau.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Lỗi hệ thống: " + e.getMessage());
        }
        response.sendRedirect("profilm");
    }
}