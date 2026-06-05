package controller;

import dao.CategoryDao;
import model.Category;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public class ManageCategoryController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setHeaderData(request);
        CategoryDao dao = new CategoryDao();
        List<Category> categories = dao.getAllCategories();
        request.setAttribute("categories", categories);
        request.getRequestDispatcher("view/manageCategory.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String action = request.getParameter("action");
        CategoryDao dao = new CategoryDao();

        try {
            if ("add".equals(action)) {
                String name = request.getParameter("cname");
                if (dao.insertCategory(name)) {
                    session.setAttribute("msgSuccess", "Thêm danh mục thành công!");
                } else {
                    session.setAttribute("msgError", "Thêm danh mục thất bại!");
                }
            } else if ("update".equals(action)) {
                int id = Integer.parseInt(request.getParameter("cid"));
                String name = request.getParameter("cname");
                if (dao.updateCategory(id, name)) {
                    session.setAttribute("msgSuccess", "Cập nhật danh mục thành công!");
                } else {
                    session.setAttribute("msgError", "Cập nhật danh mục thất bại!");
                }
            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(request.getParameter("cid"));
                if (dao.deleteCategory(id)) {
                    session.setAttribute("msgSuccess", "Xóa danh mục thành công!");
                } else {
                    session.setAttribute("msgError", "Xóa danh mục thất bại! Có thể danh mục đang có sản phẩm.");
                }
            }
        } catch (Exception e) {
            session.setAttribute("msgError", "Có lỗi xảy ra: " + e.getMessage());
        }
        response.sendRedirect("ManageCategory");
    }
}
