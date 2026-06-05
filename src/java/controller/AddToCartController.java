package controller;

import dao.ProductDao;
import model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddToCartController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        
        // YÊU CẦU ĐĂNG NHẬP: Bắt buộc đăng nhập mới được mua/thêm giỏ hàng
        if (session.getAttribute("userSession") == null) {
            session.setAttribute("errorMsg", "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!");
            response.sendRedirect("Login");
            return;
        }

        String idStr = request.getParameter("id");
        String action = request.getParameter("action");

        try {
            int id = Integer.parseInt(idStr);
            ProductDao pDao = new ProductDao();
            Product p = pDao.getProductById(id);

            // 1. CHẶN NẾU SẢN PHẨM HẾT HÀNG TỪ GỐC
            if (p == null || p.getStock() <= 0) {
                session.setAttribute("errorMsg", "Rất tiếc! Sản phẩm này hiện đã hết hàng.");
                response.sendRedirect(request.getHeader("Referer"));
                return;
            }

            Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
            if (cart == null) {
                cart = new HashMap<>();
            }

            // 2. TÍNH TOÁN SỐ LƯỢNG TRONG GIỎ SO VỚI TỒN KHO
            int currentQtyInCart = cart.containsKey(id) ? cart.get(id) : 0;
            
            if (currentQtyInCart + 1 > p.getStock()) {
                session.setAttribute("errorMsg", "Không thể thêm! Giỏ hàng đã đạt giới hạn tồn kho hiện tại (" + p.getStock() + " bó).");
                response.sendRedirect(request.getHeader("Referer"));
                return;
            }

            // 3. THÊM VÀO GIỎ THÀNH CÔNG
            cart.put(id, currentQtyInCart + 1);
            session.setAttribute("cart", cart);

            if ("buynow".equals(action)) {
                response.sendRedirect("checkout");
            } else {
                session.setAttribute("successMsg", "Đã thêm " + p.getPname() + " vào giỏ hàng!");
                response.sendRedirect(request.getHeader("Referer"));
            }

        } catch (Exception e) {
            response.sendRedirect("index");
        }
    }
}