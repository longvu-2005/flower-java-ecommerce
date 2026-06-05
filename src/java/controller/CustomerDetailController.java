package controller;

import dao.AccountDAO;
import dao.OrderDao;
import model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CustomerDetailController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setHeaderData(request);
        
        int id = Integer.parseInt(request.getParameter("id"));
        String tab = request.getParameter("tab"); 
        
        AccountDAO accDao = new AccountDAO();
        OrderDao orderDao = new OrderDao();
        
        Account acc = accDao.getAccountById(id);
        request.setAttribute("customer", acc);
        request.setAttribute("activeTab", tab);

        if ("history".equals(tab)) {
            // ĐÃ SỬA: Lấy từ hàm chống lặp đơn hàng của OrderDao thay vì AccountDAO cũ
            List<Map<String, Object>> orders = orderDao.getOrderHistoryByUserId(id);
            List<Map<String, String>> reviews = accDao.getCustomerReviews(id);
            
            request.setAttribute("orders", orders);
            request.setAttribute("reviews", reviews);
        }

        request.getRequestDispatcher("view/customerDetail.jsp").forward(request, response);
    }
}