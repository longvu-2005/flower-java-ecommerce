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

public class ManageOrderController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setHeaderData(request);
        
        HttpSession session = request.getSession();
        Account userSession = (Account) session.getAttribute("userSession");
        
        // Chỉ Admin (roleId == 1) mới được phép vào trang này
        if (userSession == null || userSession.getRoleId() != 1) {
            response.sendRedirect("index");
            return;
        }

        OrderDao oDao = new OrderDao();
        List<Map<String, Object>> allOrders = oDao.getAllOrdersForAdmin();
        
        request.setAttribute("allOrders", allOrders);
        request.getRequestDispatcher("view/manageOrders.jsp").forward(request, response);
    }
}