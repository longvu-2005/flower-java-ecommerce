package controller;

import dao.OrderDao;
import dao.ProductDao;
import model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class AdminDashboardController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        jakarta.servlet.http.HttpSession session = request.getSession();
        model.Account userSession = (model.Account) session.getAttribute("userSession");
        if (userSession == null || userSession.getRoleId() != 1) {
            response.sendRedirect("Login");
            return;
        }

        setHeaderData(request);
        String keyword = request.getParameter("keyword");
        String categoryId = request.getParameter("category");
        String sortType = request.getParameter("sort");

        ProductDao pDao = new ProductDao();
        OrderDao oDao = new OrderDao();

        request.setAttribute("totalRevenue", oDao.getTotalRevenue());
        request.setAttribute("totalOrders", oDao.getTotalOrders());

        List<Product> listP = pDao.searchAndSortProducts(keyword, categoryId, sortType);
        request.setAttribute("allProducts", listP);
        request.setAttribute("totalProductCount", pDao.getTotalProducts());
        request.setAttribute("listCC", pDao.getAllCategories()); 

        request.setAttribute("currentKeyword", keyword);
        request.setAttribute("currentCategory", categoryId);
        request.setAttribute("currentSort", sortType);

        request.getRequestDispatcher("view/admin.jsp").forward(request, response);
    }
}