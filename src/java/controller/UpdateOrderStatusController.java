package controller;

import dao.OrderDao;
import model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class UpdateOrderStatusController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Account userSession = (Account) session.getAttribute("userSession");
        
        if (userSession == null || userSession.getRoleId() != 1) {
            response.sendRedirect("Login");
            return;
        }

        try {
            int orderId = Integer.parseInt(request.getParameter("orderId"));
            String newStatus = request.getParameter("status");

            OrderDao oDao = new OrderDao();
            boolean success = oDao.updateOrderStatusByAdmin(orderId, newStatus);

            if (success) {
                session.setAttribute("msgSuccess", "Cập nhật trạng thái đơn #" + orderId + " thành công!");
                
                // Tự động sinh mã giảm giá 10% nếu đơn hàng được Hoàn thành và >= 3.000.000đ
                if ("Completed".equals(newStatus)) {
                    java.util.Map<String, Object> orderDetails = oDao.getOrderFullDetailsById(orderId);
                    if (orderDetails != null) {
                        double grandTotal = (Double) orderDetails.get("total_price");
                        int customerId = (Integer) orderDetails.get("user_id");
                        // Có thể dùng email trong đơn hàng hoặc fetch từ user, tạm dùng email trong DB đơn hàng (hoặc rỗng nếu không có)
                        String customerEmail = (String) orderDetails.get("sender_email"); 
                        
                        if (grandTotal >= 3000000) {
                            dao.CouponDao cDao = new dao.CouponDao();
                            String newCoupon = cDao.generateCoupon();
                            if (newCoupon != null) {
                                dao.MessageDao mDao = new dao.MessageDao();
                                String msg = "Chúc mừng! Đơn hàng #" + orderId + " của bạn đã được xác nhận hoàn thành với tổng tiền đạt chuẩn. Bạn được tặng 1 mã giảm giá 10% cho đơn hàng tiếp theo. Mã của bạn là: " + newCoupon;
                                mDao.saveMessage(customerId, customerEmail != null ? customerEmail : "customer", "admin", msg);
                            }
                        }
                    }
                }
            } else {
                session.setAttribute("msgError", "Lỗi cập nhật trạng thái hệ thống.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Quay lại trang quản lý đơn hàng
        response.sendRedirect("ManageOrder");
    }
}