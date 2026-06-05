package controller;

import dao.ProductDao;
import model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class ReviewController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Account user = (Account) session.getAttribute("userSession");
        String pidStr = request.getParameter("pid");

        if (user == null) {
            session.setAttribute("errorMsg", "Vui lòng đăng nhập để để lại đánh giá!");
            response.sendRedirect("Login");
            return;
        }

        try {
            int pid = Integer.parseInt(pidStr);
            int rating = Integer.parseInt(request.getParameter("rating"));
            String comment = request.getParameter("comment");

            ProductDao pDao = new ProductDao();
            boolean isSuccess = pDao.insertReview(user.getUserId(), pid, rating, comment);

            if(isSuccess) {
                session.setAttribute("successMsg", "Cảm ơn bạn! Đánh giá đã được ghi nhận.");
            } else {
                session.setAttribute("errorMsg", "Đã xảy ra lỗi khi gửi đánh giá.");
            }
            response.sendRedirect("ProductDetail?pid=" + pid);
        } catch (Exception e) {
            response.sendRedirect("index");
        }
    }
}