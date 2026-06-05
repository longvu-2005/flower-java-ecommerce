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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReOrderController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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
            
            Map<String, Object> oldOrder = oDao.getOrderFullDetailsById(orderId);
            List<Map<String, Object>> oldItems = oDao.getOrderLineItems(orderId);
            
            if (oldOrder != null && oldItems != null && !oldItems.isEmpty()) {
                Map<Integer, Integer> cart = new HashMap<>();
                double subTotal = 0.0;
                dao.ProductDao pDao = new dao.ProductDao();
                for (Map<String, Object> item : oldItems) {
                    int pId = (Integer) item.get("product_id");
                    int qty = (Integer) item.get("quantity");
                    cart.put(pId, qty);
                    
                    model.Product p = pDao.getProductById(pId);
                    if (p != null) {
                        double finalPrice = p.getPprice();
                        if (p.getDiscount() > 0) {
                            finalPrice = p.getPprice() * (1 - (double) p.getDiscount() / 100);
                        }
                        subTotal += finalPrice * qty;
                    }
                }
                
                String targetPaymentMethod = oDao.getLatestValidPaymentMethod(userSession.getUserId());
                if (targetPaymentMethod == null || targetPaymentMethod.isEmpty()) {
                    targetPaymentMethod = "Chưa chọn";
                }
                
                String couponCode = "";
                double discountAmount = 0.0;
                double vat = subTotal * 0.08;
                double grandTotal = subTotal + vat;
                
                int newOrderId = oDao.saveOrder(
                        userSession.getUserId(),
                        (String) oldOrder.get("sender_name"),
                        (String) oldOrder.get("sender_phone"),
                        (String) oldOrder.get("sender_email"),
                        (String) oldOrder.get("receiver_name"),
                        (String) oldOrder.get("receiver_phone"),
                        (String) oldOrder.get("receiver_address"),
                        (String) oldOrder.get("receiver_city"),
                        (String) oldOrder.get("receiver_district"),
                        (String) oldOrder.get("delivery_date"),
                        (String) oldOrder.get("delivery_time"),
                        (String) oldOrder.get("card_message"),
                        (String) oldOrder.get("special_requests"),
                        0.0, 
                        vat,
                        grandTotal,
                        cart,
                        couponCode,
                        discountAmount,
                        targetPaymentMethod 
                );
                
                if (newOrderId > 0) {
                    // ĐÃ SỬA: Đẩy sang trang Payment để người dùng tùy chọn lại mã QR hoặc Tiền mặt
                    response.sendRedirect("payment?orderId=" + newOrderId + "&isDirect=false");
                    return;
                }
            }
            
            response.sendRedirect("profilm");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("profilm");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}