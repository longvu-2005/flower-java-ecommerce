package controller;

import dao.OrderDao;
import model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminOrderDetailController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setHeaderData(request);
        HttpSession session = request.getSession();
        Account userSession = (Account) session.getAttribute("userSession");
        
        // Cản người dùng thường, bắt buộc phải là Admin
        if (userSession == null || userSession.getRoleId() != 1) {
            response.sendRedirect("Login");
            return;
        }
        
        try {
            int orderId = Integer.parseInt(request.getParameter("orderId"));
            OrderDao oDao = new OrderDao();
            
            // Bốc dữ liệu đầy đủ đưa sang trang Admin
            Map<String, Object> orderDetail = oDao.getOrderFullDetailsById(orderId);
            List<Map<String, Object>> orderItems = oDao.getOrderLineItems(orderId);
            
            request.setAttribute("order", orderDetail);
            request.setAttribute("items", orderItems);
            
            request.getRequestDispatcher("view/adminOrderDetail.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect("ManageOrder");
        }
    }
}