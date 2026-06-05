package controller;

import dao.AccountDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class ForgotPasswordController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("view/forgotpassword.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        String email = request.getParameter("email");
        String answer = request.getParameter("answer");
        String newPassword = request.getParameter("newPassword");

        AccountDAO dao = new AccountDAO();
        
        // Gọi hàm reset mật khẩu trong DAO (Trả về true nếu email và câu trả lời khớp)
        boolean isReset = dao.resetPassword(email, answer, newPassword);

        if (isReset) {
            // 🟢 THÀNH CÔNG: Gắn thông báo vào Session và chuyển hướng về trang Đăng Nhập
            HttpSession session = request.getSession();
            session.setAttribute("successMsg", "Khôi phục mật khẩu thành công! Hãy dùng mật khẩu mới để đăng nhập.");
            response.sendRedirect("Login");
        } else {
            // 🔴 THẤT BẠI: Sai email hoặc câu trả lời bảo mật
            request.setAttribute("error", "Email hoặc câu trả lời bảo mật không chính xác. Đổi mật khẩu thất bại!");
            request.getRequestDispatcher("view/forgotpassword.jsp").forward(request, response);
        }
    }
}