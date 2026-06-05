package controller;

import dao.AccountDAO;
import dao.CartDao;
import model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

public class LoginController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setHeaderData(request);
        request.getRequestDispatcher("view/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String pass = request.getParameter("password");

        AccountDAO dao = new AccountDAO();
        
        // 🔴 ĐÃ SỬA: Gọi đúng tên hàm login() có trong AccountDAO của bạn
        Account account = dao.login(email, pass);

        if (account != null) {
            HttpSession session = request.getSession();
            session.setAttribute("userSession", account);

            // --- ĐỒNG BỘ GIỎ HÀNG TỪ DATABASE LÊN WEB ---
            try {
                CartDao cartDao = new CartDao();
                int accountId = account.getUserId();
                int cartId = cartDao.getOrCreateCartId(accountId);

                // Lấy giỏ cũ từ DB
                Map<Integer, Integer> dbCart = cartDao.loadCartItems(cartId);
                // Lấy giỏ tạm thời khách đang thêm trước khi đăng nhập
                Map<Integer, Integer> guestCart = (Map<Integer, Integer>) session.getAttribute("cart");

                if (guestCart != null) {
                    // Gộp 2 giỏ lại với nhau
                    for (Map.Entry<Integer, Integer> entry : guestCart.entrySet()) {
                        int pid = entry.getKey();
                        int qty = entry.getValue();
                        dbCart.put(pid, dbCart.getOrDefault(pid, 0) + qty);
                        cartDao.saveOrUpdateCartItem(cartId, pid, dbCart.get(pid));
                    }
                }
                // Nạp giỏ hàng hoàn chỉnh vào Session
                session.setAttribute("cart", dbCart);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Phân quyền chuyển hướng
            if (account.getRoleId() == 1) {
                response.sendRedirect("AdminDashboard");
            } else {
                response.sendRedirect("index");
            }
        } else {
            request.setAttribute("error", "Sai email hoặc mật khẩu!");
            request.getRequestDispatcher("view/login.jsp").forward(request, response);
        }
    }
}