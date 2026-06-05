package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BuyNowController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        int id = Integer.parseInt(request.getParameter("id"));
        
        // Tạo giỏ hàng chỉ chứa duy nhất 1 sản phẩm này
        Map<Integer, Integer> cart = new HashMap<>();
        cart.put(id, 1);
        session.setAttribute("cart", cart); // Ghi đè giỏ hàng cũ bằng giỏ hàng mới này
        
        response.sendRedirect("checkout"); // Chuyển thẳng sang thanh toán
    }
}