package controller;

import dao.ProductDao;
import model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProductDetailController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            setHeaderData(request);

            int pid = Integer.parseInt(request.getParameter("pid"));
            ProductDao pDao = new ProductDao();
            
            Product p = pDao.getProductById(pid);
            List<Map<String, Object>> reviews = pDao.getReviewsByProductId(pid);
            
            request.setAttribute("p", p);
            request.setAttribute("reviews", reviews);
            request.getRequestDispatcher("view/productDetail.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect("index");
        }
    }
}