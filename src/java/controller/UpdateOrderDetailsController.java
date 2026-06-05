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

public class UpdateOrderDetailsController extends HttpServlet {

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
            String rName = request.getParameter("receiver_name");
            String rPhone = request.getParameter("receiver_phone");
            String rAddress = request.getParameter("receiver_address");
            String rDistrict = request.getParameter("receiver_district");
            String rCity = request.getParameter("receiver_city");
            String delivDate = request.getParameter("delivery_date");
            String delivTime = request.getParameter("delivery_time");
            String specialRequests = request.getParameter("special_requests");
            String cardMessage = request.getParameter("card_message");
            
            OrderDao oDao = new OrderDao();
            boolean isUpdated = oDao.updateOrderDeliveryAndMessages(
                    orderId, userSession.getUserId(), rName, rPhone, rAddress, rDistrict, rCity, delivDate, delivTime, specialRequests, cardMessage
            );
            
            if (isUpdated) {
                session.setAttribute("reviewMsg", "Cập nhật thông tin nhận hoa và lời nhắn thành công!");
            } else {
                session.setAttribute("reviewMsg", "❌ Cập nhật thất bại! Đơn hàng đã hoàn thành hoặc đã hủy nên không thể chỉnh sửa.");
            }
            
            response.sendRedirect("OrderDetail?orderId=" + orderId);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("profilm");
        }
    }
}