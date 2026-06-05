package controller;

import dao.MessageDao;
import dao.ProductDao;
import model.Account;
import model.Product;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

public class BaseController extends HttpServlet {

    protected void setHeaderData(HttpServletRequest request) {
        ProductDao pDao = new ProductDao();

        // 1. Danh sách danh mục (có cache 60 giây trong ProductDao)
        List<Map<String, Object>> categories = pDao.getAllCategories();
        request.setAttribute("listCategories", categories);

        // 2. Dữ liệu giỏ hàng cho Mini Cart trên Header
        HttpSession session = request.getSession(false);
        if (session != null) {
            Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
            if (cart != null && !cart.isEmpty()) {
                Map<Integer, Product> cartProducts = pDao.getProductsForCart(cart.keySet());
                request.setAttribute("cartProducts", cartProducts);
            }

            // 3. Thông báo tin nhắn chưa đọc (chỉ khi user đã đăng nhập)
            Account user = (Account) session.getAttribute("userSession");
            if (user != null) {
                MessageDao mDao = new MessageDao();
                if (user.getRoleId() == 1) { // ADMIN
                    int unreadAdmin = mDao.getUnreadCountForAdmin();
                    request.setAttribute("unreadAdmin", unreadAdmin);
                } else { // USER
                    int unread = mDao.getUnreadCountForUser(user.getUserId());
                    request.setAttribute("unreadUser", unread);
                }
            }
        }
    }
}
