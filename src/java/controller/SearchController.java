package controller;

import dao.ProductDao;
import model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class SearchController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        setHeaderData(request);

        String keyword = request.getParameter("keyword");
        
        ProductDao dao = new ProductDao();
        List<Product> searchResults = dao.searchAndSortProducts(keyword, "all", "default");
        
        request.setAttribute("dataProducts", searchResults);
        request.setAttribute("searchKeyword", keyword);
        
        request.getRequestDispatcher("view/index.jsp").forward(request, response);
    }
}