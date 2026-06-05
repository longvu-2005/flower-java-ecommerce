package controller;

import dao.ProductDao;
import model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class HomeController extends BaseController {
   
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        setHeaderData(request);

        ProductDao dao = new ProductDao();
        List<Product> list = dao.getHomeProducts();
        
        request.setAttribute("dataProducts", list);
        
        request.getRequestDispatcher("view/index.jsp").forward(request, response);
    }
}