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

public class ProfilmController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Account userSession = (Account) session.getAttribute("userSession");
        if (userSession == null) {
            response.sendRedirect("Login");
            return;
        }

        setHeaderData(request);

        OrderDao oDao = new OrderDao();
        double totalMoney = oDao.getTotalMoneyByUserId(userSession.getUserId());
        
        List<Map<String, Object>> listOrder = oDao.getOrderHistoryByUserId(userSession.getUserId());

        request.setAttribute("totalMoney", totalMoney);
        request.setAttribute("listOrder", listOrder);

        request.getRequestDispatcher("view/profilm.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}