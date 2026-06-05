package controller;

import dao.OrderDao;
import model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class OrderDetailController extends BaseController {
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
            setHeaderData(request);

            int orderId = Integer.parseInt(request.getParameter("orderId"));
            OrderDao oDao = new OrderDao();
            
            Map<String, Object> orderDetail = oDao.getOrderFullDetailsById(orderId);
            List<Map<String, Object>> orderItems = oDao.getOrderLineItems(orderId);
            
            request.setAttribute("order", orderDetail);
            request.setAttribute("items", orderItems);
            
            request.getRequestDispatcher("view/orderDetail.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect("profilm");
            
        }
    }
}