package controller;

import dao.OrderDao;
import model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class CancelOrderController extends HttpServlet {

    // CHÚ Ý: Bắt buộc phải có doPost vì form HTML đang gửi bằng method="post"
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Account userSession = (Account) session.getAttribute("userSession");
        
        if (userSession == null) {
            response.sendRedirect("Login");
            return;
        }
        
        try {
            int orderId = Integer.parseInt(request.getParameter("orderId"));
            OrderDao oDao = new OrderDao();
            
            // Gọi hàm hủy đơn hàng
            boolean isCancelled = oDao.cancelOrderByUser(orderId, userSession.getUserId());
            
            // Gửi cờ tín hiệu cho SweetAlert2 bên JSP
            if (isCancelled) {
                session.setAttribute("sweetSuccess", "Đã hủy đơn hàng #" + orderId + " thành công!");
            } else {
                session.setAttribute("sweetError", "Hủy thất bại! Đơn hàng này đã được Admin xử lý hoặc đang giao.");
            }
            
            response.sendRedirect("OrderDetail?orderId=" + orderId);
            
        } catch (Exception e) {
            response.sendRedirect("profilm");
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}