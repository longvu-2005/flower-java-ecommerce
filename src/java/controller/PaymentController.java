package controller;

import dao.OrderDao;
import dao.CartDao;
import model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

public class PaymentController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setHeaderData(request);
        try {
            int orderId = Integer.parseInt(request.getParameter("orderId"));
            String isDirect = request.getParameter("isDirect"); 
            
            OrderDao oDao = new OrderDao();
            Map<String, String> order = oDao.getOrderSummaryById(orderId);
            
            request.setAttribute("order", order);
            request.setAttribute("isDirect", isDirect);
            
            request.getRequestDispatcher("view/payment.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect("index");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int orderId = Integer.parseInt(request.getParameter("orderId"));
            String paymentMethod = request.getParameter("paymentMethod"); 
            
            String status = "Pending"; 
            String methodNameText = "Chuyển khoản qua QR Code Online";
            
            if ("COD".equals(paymentMethod)) {
                methodNameText = "Thanh toán tiền mặt trực tiếp khi nhận hoa (COD)";
                status = "Pending";
            }
            
            OrderDao oDao = new OrderDao();
            oDao.updatePaymentMethod(orderId, methodNameText, status);
            
            HttpSession session = request.getSession();
            Account userSession = (Account) session.getAttribute("userSession");
            if (userSession != null) {
                try {
                    CartDao cartDao = new CartDao();
                    int cartId = cartDao.getOrCreateCartId(userSession.getUserId());
                    cartDao.clearCart(cartId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // Xóa session giỏ hàng RAM
            session.removeAttribute("cart");
            
            // 🟢 ĐÃ HOÀN TẤT ĐƠN: Xóa sạch toàn bộ dữ liệu form tạm thời trong Session
            session.removeAttribute("temp_sender_name");
            session.removeAttribute("temp_sender_phone");
            session.removeAttribute("temp_sender_email");
            session.removeAttribute("temp_receiver_name");
            session.removeAttribute("temp_receiver_phone");
            session.removeAttribute("temp_receiver_address");
            session.removeAttribute("temp_receiver_city");
            session.removeAttribute("temp_receiver_district");
            session.removeAttribute("temp_delivery_date");
            session.removeAttribute("temp_delivery_time");
            session.removeAttribute("temp_card_message");
            session.removeAttribute("temp_special_requests");
            session.removeAttribute("temp_is_direct");
            
            session.setAttribute("orderSuccessAlert", "true");
            response.sendRedirect("index");
        } catch (Exception e) {
            response.sendRedirect("index");
        }
    }
}