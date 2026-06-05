package controller;

import dao.AccountDAO;
import model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class RegisterController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setHeaderData(request);
        request.getRequestDispatcher("view/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        String fullname = request.getParameter("fullname");
        String email = request.getParameter("email");
        String pass = request.getParameter("password");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String question = request.getParameter("question");
        String answer = request.getParameter("answer");

        AccountDAO dao = new AccountDAO();
        
        // Tạo đối tượng Account (id=0 vì DB tự tăng, role=2 là khách hàng)
        Account acc = new Account(0, fullname, email, pass, phone, address, question, answer, 2);
        
        // Gọi hàm đăng ký và nhận kết quả trả về
        boolean isSuccess = dao.register(acc);
        
        if (isSuccess) {
            // 🟢 THÀNH CÔNG: Gắn thông báo vào Session và chuyển hướng về trang Đăng Nhập
            HttpSession session = request.getSession();
            session.setAttribute("successMsg", "Chúc mừng bạn đã tạo tài khoản thành công! Hãy đăng nhập để mua hoa nhé.");
            response.sendRedirect("Login");
        } else {
            // 🔴 THẤT BẠI: Trùng Email, trả lại trang đăng ký kèm thông báo lỗi
            request.setAttribute("error", "Email này đã được sử dụng! Vui lòng dùng email khác.");
            request.getRequestDispatcher("view/register.jsp").forward(request, response);
        }
    }
}