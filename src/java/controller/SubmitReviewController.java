package controller;

import dao.OrderDao;
import model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class SubmitReviewController extends HttpServlet {
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
        
        try {
            int orderId = Integer.parseInt(request.getParameter("orderId"));
            int productId = Integer.parseInt(request.getParameter("productId"));
            int rating = Integer.parseInt(request.getParameter("rating"));
            String comment = request.getParameter("comment");
            
            OrderDao oDao = new OrderDao();
            boolean success = oDao.insertReview(userSession.getUserId(), productId, rating, comment);
            
            if (success) {
                // Nội dung thông báo hiển thị 10 giây
                session.setAttribute("reviewMsg", 
                    "Cảm ơn bạn đã ghé thăm và dành thời gian đánh giá cho shop 🌸<br>" +
                    "Những lời yêu thương của bạn là động lực để tụi mình cố gắng mỗi ngày. " +
                    "Hy vọng sẽ tiếp tục được đồng hành cùng bạn trong những lần tới 💖"
                );
            }
            response.sendRedirect("OrderDetail?orderId=" + orderId);
        } catch (Exception e) {
            response.sendRedirect("profilm");
        }
    }
}