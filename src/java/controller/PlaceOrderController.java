package controller;

import dao.CartDao;
import dao.CouponDao;
import dao.OrderDao;
import dao.ProductDao;
import model.Account;
import model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

public class PlaceOrderController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("cart");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        
        HttpSession session = request.getSession();
        
        // 1. KIỂM TRA ĐĂNG NHẬP
        Account userSession = (Account) session.getAttribute("userSession");
        if (userSession == null) {
            response.sendRedirect("Login");
            return;
        }
        
        // 2. KIỂM TRA GIỎ HÀNG CÒN TRÊN PHIÊN KHÔNG
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            session.setAttribute("orderError", "Giỏ hàng rỗng!");
            response.sendRedirect("checkout");
            return;
        }
        
        try {
            // 3. LẤY THÔNG TIN TỪ FORM
            String isDirect = request.getParameter("is_direct_receiver");
            
            String sName = request.getParameter("sender_name");
            String sPhone = request.getParameter("sender_phone");
            String sEmail = request.getParameter("sender_email");
            
            String rName = request.getParameter("receiver_name");
            String rPhone = request.getParameter("receiver_phone");
            String rAddress = request.getParameter("receiver_address");
            String rCity = request.getParameter("receiver_city");
            String rDistrict = request.getParameter("receiver_district");
            
            // Nếu khách tự nhận, gán tên người đặt cho người nhận
            if ("true".equals(isDirect)) {
                rName = sName;
                rPhone = sPhone;
            }
            
            String delivDate = request.getParameter("delivery_date");
            String delivTime = request.getParameter("delivery_time");
            String cardMessage = request.getParameter("card_message");
            String specialRequests = request.getParameter("special_requests");
            String appliedCoupon = request.getParameter("applied_coupon");

            // 🟢 LƯU TẠM TOÀN BỘ THÔNG TIN VÀO SESSION ĐỂ TRÁNH MẤT KHI USER BACK LẠI TRANG
            session.setAttribute("temp_sender_name", sName);
            session.setAttribute("temp_sender_phone", sPhone);
            session.setAttribute("temp_sender_email", sEmail);
            session.setAttribute("temp_receiver_name", rName);
            session.setAttribute("temp_receiver_phone", rPhone);
            session.setAttribute("temp_receiver_address", rAddress);
            session.setAttribute("temp_receiver_city", rCity);
            session.setAttribute("temp_receiver_district", rDistrict);
            session.setAttribute("temp_delivery_date", delivDate);
            session.setAttribute("temp_delivery_time", delivTime);
            session.setAttribute("temp_card_message", cardMessage);
            session.setAttribute("temp_special_requests", specialRequests);
            session.setAttribute("temp_is_direct", isDirect);

            // 4. TÍNH TOÁN LẠI TIỀN (Chống gian lận sửa HTML) - TỐI ƯU N+1 QUERY
            double subTotal = 0;
            ProductDao pDao = new ProductDao();
            Map<Integer, Product> mapProducts = pDao.getProductsForCart(cart.keySet());
            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                Product p = mapProducts.get(entry.getKey());
                if (p != null) {
                    double price = p.getPprice();
                    if (p.getDiscount() > 0) {
                        price = price * (1 - (double) p.getDiscount() / 100);
                    }
                    subTotal += price * entry.getValue();
                }
            }
            
            // 5. ÁP DỤNG MÃ GIẢM GIÁ
            double discountAmount = 0;
            if (appliedCoupon != null && !appliedCoupon.trim().isEmpty()) {
                CouponDao cDao = new CouponDao();
                String[] codes = appliedCoupon.split(",");
                int totalPercent = 0;
                for (String code : codes) {
                    int p = cDao.getDiscountPercent(code.trim());
                    if (p > 0) {
                        totalPercent += p;
                    }
                }
                if (totalPercent > 100) totalPercent = 100;
                if (totalPercent > 0) {
                    discountAmount = subTotal * (totalPercent / 100.0);
                }
                
                // Tránh lỗi vượt quá độ dài cột trong DB nếu nhập quá nhiều mã
                if (appliedCoupon.length() > 200) {
                    appliedCoupon = appliedCoupon.substring(0, 200);
                }
            }
            
            double vat = (subTotal - discountAmount) * 0.08;
            double grandTotal = (subTotal - discountAmount) + vat;
            
            // 6. LƯU ĐƠN HÀNG VÀO DB
            OrderDao oDao = new OrderDao();
            
            int orderId = oDao.saveOrder(
                    userSession.getUserId(),
                    sName, sPhone, sEmail,                   
                    rName, rPhone, rAddress, rCity, rDistrict, 
                    delivDate, delivTime, cardMessage, specialRequests,
                    subTotal, vat, grandTotal, cart, appliedCoupon, discountAmount,
                    "Chưa chọn" 
            );
            
            // 7. PHÂN LUỒNG ĐIỀU HƯỚNG KẾT QUẢ
            if (orderId > 0) {
                // Đánh dấu mã giảm giá đã sử dụng và ghi nhận lịch sử
                if (appliedCoupon != null && !appliedCoupon.trim().isEmpty()) {
                    CouponDao cDao = new CouponDao();
                    String[] codes = appliedCoupon.split(",");
                    for (String code : codes) {
                        String cleanCode = code.trim();
                        model.Coupon coupon = cDao.getCouponByCode(cleanCode);
                        if (coupon != null) {
                            cDao.recordUsage(coupon.getId(), userSession.getUserId(), orderId);
                        }
                    }
                }
                
                String isDirectParam = (isDirect != null && isDirect.equals("true")) ? "true" : "false";
                response.sendRedirect("payment?orderId=" + orderId + "&isDirect=" + isDirectParam);
            } else {
                session.setAttribute("orderError", "Lỗi lưu đơn hàng vào Database!");
                response.sendRedirect("checkout");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("orderError", "Lỗi hệ thống Backend: " + e.getMessage());
            response.sendRedirect("checkout");
        }
    }
}