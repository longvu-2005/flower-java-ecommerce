package controller;

import dao.CartDao;
import dao.ProductDao;
import model.Account;
import model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

public class UpdateCartController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pidParam = request.getParameter("pid");
        String action = request.getParameter("action");
        
        if (pidParam != null && action != null) {
            int pid = Integer.parseInt(pidParam);
            HttpSession session = request.getSession();
            Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");

            if (cart != null && cart.containsKey(pid)) {
                int quantity = cart.get(pid);
                
                if (action.equals("inc")) {
                    // Kiểm tra tồn kho trước khi tăng số lượng
                    ProductDao pDao = new ProductDao();
                    Product p = pDao.getProductById(pid);
                    if (p != null && quantity + 1 > p.getStock()) {
                        session.setAttribute("successMsg", "Không thể thêm! Đã đạt giới hạn tồn kho (" + p.getStock() + " bó).");
                        response.sendRedirect("cart");
                        return;
                    }
                    quantity++;
                } else if (action.equals("dec")) {
                    quantity--;
                }
                
                Account userSession = (Account) session.getAttribute("userSession");
                CartDao cartDao = new CartDao();
                int cartId = 0;
                
                if (userSession != null) {
                    cartId = cartDao.getOrCreateCartId(userSession.getUserId());
                }

                if (quantity <= 0) {
                    cart.remove(pid);
                    if (userSession != null) {
                        cartDao.removeCartItem(cartId, pid);
                    }
                } else {
                    cart.put(pid, quantity);
                    if (userSession != null) {
                        cartDao.saveOrUpdateCartItem(cartId, pid, quantity);
                    }
                }
                
                session.setAttribute("cart", cart);
            }
        }
        
        response.sendRedirect("cart"); 
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}