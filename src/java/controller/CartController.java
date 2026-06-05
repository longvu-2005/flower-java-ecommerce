package controller;

import dao.ProductDao;
import model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CartController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setHeaderData(request);

        HttpSession session = request.getSession();
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        
        List<Product> cartList = new ArrayList<>();
        double totalAmount = 0;
        
        if (cart != null && !cart.isEmpty()) {
            ProductDao pDao = new ProductDao();
            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                Product p = pDao.getProductById(entry.getKey());
                if (p != null) {
                    double finalPrice = p.getPprice();
                    if (p.getDiscount() > 0) {
                        finalPrice = p.getPprice() * (1 - (double) p.getDiscount() / 100);
                    }
                    
                    p.setSold(entry.getValue()); 
                    
                    totalAmount += finalPrice * entry.getValue();
                    cartList.add(p);
                }
            }
        }
        
        request.setAttribute("cartList", cartList);
        request.setAttribute("totalAmount", totalAmount);
        
        request.getRequestDispatcher("view/cart.jsp").forward(request, response);
    }
}