package controller;

import dao.ProductDao;
import model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Long
 */
public class ManagerProductController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        jakarta.servlet.http.HttpSession session = request.getSession();
        model.Account userSession = (model.Account) session.getAttribute("userSession");
        if (userSession == null || userSession.getRoleId() != 1) {
            response.sendRedirect("Login");
            return;
        }

        setHeaderData(request);
        
        // 1. Nhận các tham số tìm kiếm và lọc từ giao diện Admin gửi lên
        String keyword = request.getParameter("keyword");
        String categoryId = request.getParameter("category");
        String sortType = request.getParameter("sort");

        ProductDao dao = new ProductDao();
        
        // 2. Gọi hàm tìm kiếm và sắp xếp đa năng (Đã tạo trong ProductDao)
        List<Product> listP = dao.searchAndSortProducts(keyword, categoryId, sortType);
        
        // 3. Đẩy dữ liệu danh sách sản phẩm và danh mục sang JSP
        request.setAttribute("listP", listP);
        request.setAttribute("listCC", dao.getAllCategories()); // Lấy danh mục để đổ vào thẻ <select>
        
        // 4. Giữ lại trạng thái tìm kiếm trên thanh UI để Admin biết mình đang lọc cái gì
        request.setAttribute("currentKeyword", keyword);
        request.setAttribute("currentCategory", categoryId);
        request.setAttribute("currentSort", sortType);

        // 5. Chuyển hướng sang trang giao diện
        request.getRequestDispatcher("view/ManagerProduct.jsp").forward(request, response);
    }
}