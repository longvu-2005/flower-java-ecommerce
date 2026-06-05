package controller;

import dao.ProductDao;
import model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminReviewController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setHeaderData(request);
        String pid = request.getParameter("pid");
        if (pid != null && !pid.isEmpty()) {
            ProductDao dao = new ProductDao();
            Product product = dao.getProductById(Integer.parseInt(pid));
            List<Map<String, Object>> reviews = dao.getReviewsByProductId(Integer.parseInt(pid));
            
            request.setAttribute("productDetail", product);
            request.setAttribute("listReviews", reviews);
            request.getRequestDispatcher("view/AdminProductReviews.jsp").forward(request, response);
        } else {
            response.sendRedirect("AdminDashboard");
        }
    }
}