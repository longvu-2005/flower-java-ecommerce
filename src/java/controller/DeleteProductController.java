package controller;

import dao.ProductDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DeleteProductController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        jakarta.servlet.http.HttpSession session = request.getSession();
        model.Account userSession = (model.Account) session.getAttribute("userSession");
        if (userSession == null || userSession.getRoleId() != 1) {
            response.sendRedirect("Login");
            return;
        }

        String id = request.getParameter("pid");
        String from = request.getParameter("from");
        String targetPage = "admin".equals(from) ? "AdminDashboard" : "ManagerProduct";

        if (id != null && !id.isEmpty()) {
            try {
                ProductDao dao = new ProductDao();
                boolean isDeleted = dao.deleteProduct(id);
                if (isDeleted) {
                    session.setAttribute("msgSuccess", "Đã xóa sản phẩm thành công!");
                } else {
                    session.setAttribute("msgError", "Không thể xóa sản phẩm. Có thể do lỗi dữ liệu liên kết.");
                }
            } catch (Exception e) {
                session.setAttribute("msgError", "ID không hợp lệ!");
            }
        }
        response.sendRedirect(targetPage);
    }
}