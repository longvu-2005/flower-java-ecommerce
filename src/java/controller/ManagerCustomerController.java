package controller;

import dao.AccountDAO;
import model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class ManagerCustomerController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        jakarta.servlet.http.HttpSession session = request.getSession();
        model.Account userSession = (model.Account) session.getAttribute("userSession");
        if (userSession == null || userSession.getRoleId() != 1) {
            response.sendRedirect("Login");
            return;
        }

        setHeaderData(request);
        
        // Gọi DAO để lấy danh sách khách hàng
        AccountDAO dao = new AccountDAO();
        List<Account> listC = dao.getAllCustomers();
        
        // Đẩy sang JSP
        request.setAttribute("listC", listC);
        request.getRequestDispatcher("view/ManagerCustomer.jsp").forward(request, response);
    }
}