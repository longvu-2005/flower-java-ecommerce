package controller;

import model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

public class RemoveFromCartController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("pid");
        if (idParam != null) {
            int id = Integer.parseInt(idParam);
            HttpSession session = request.getSession();
            Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");

            if (cart != null) {
                // 1. Xóa khỏi Session (RAM)
                cart.remove(id);
                session.setAttribute("cart", cart);

                // 2. Xóa khỏi Database nếu đã đăng nhập
                Account userSession = (Account) session.getAttribute("userSession");
                if (userSession != null) {
                    dao.CartDao cartDao = new dao.CartDao();
                    // Sửa thành:
                    int cartId = cartDao.getOrCreateCartId(userSession.getUserId());
                    cartDao.removeCartItem(cartId, id);
                }
            }
        }

        // Quay lại trang giỏ hàng sau khi xóa
        response.sendRedirect("cart");
    }
}
