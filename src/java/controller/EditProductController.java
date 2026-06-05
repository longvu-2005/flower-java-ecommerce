package controller;

import dao.ProductDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

// 🔴 ĐÃ XÓA @WebServlet ĐỂ KHÔNG XUNG ĐỘT VỚI WEB.XML CỦA BẠN
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024 * 50)
public class EditProductController extends HttpServlet {
    
    // Xử lý khi Admin bấm nút "Sửa" ở trang danh sách -> Lắng nghe /loadProduct từ web.xml
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        jakarta.servlet.http.HttpSession session = request.getSession();
        model.Account userSession = (model.Account) session.getAttribute("userSession");
        if (userSession == null || userSession.getRoleId() != 1) {
            response.sendRedirect("Login");
            return;
        }

        String pid = request.getParameter("pid");
        String from = request.getParameter("from"); 
        if (pid == null) pid = request.getParameter("id"); 
        
        if (pid != null && !pid.isEmpty()) {
            ProductDao dao = new ProductDao();
            request.setAttribute("detail", dao.getProductById(Integer.parseInt(pid))); 
            request.setAttribute("listCC", dao.getAllCategories()); 
            request.setAttribute("fromPage", from); 
            
            request.getRequestDispatcher("view/UpdateProduct.jsp").forward(request, response);
        } else {
            response.sendRedirect("AdminDashboard");
        }
    } 

    // Xử lý khi Admin bấm nút "Lưu thay đổi" -> Lắng nghe /EditProduct từ web.xml
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        jakarta.servlet.http.HttpSession session = request.getSession();
        model.Account userSession = (model.Account) session.getAttribute("userSession");
        if (userSession == null || userSession.getRoleId() != 1) {
            response.sendRedirect("Login");
            return;
        }

        request.setCharacterEncoding("UTF-8");
        
        String from = request.getParameter("fromPage");
        String targetPage = "admin".equals(from) ? "AdminDashboard" : "ManagerProduct";

        try {
            int id = Integer.parseInt(request.getParameter("pid"));
            String name = request.getParameter("pname");
            String desc = request.getParameter("pdescription");
            double price = Double.parseDouble(request.getParameter("pprice"));
            int discount = Integer.parseInt(request.getParameter("pdiscount"));
            int stock = Integer.parseInt(request.getParameter("pstock"));
            int cateID = Integer.parseInt(request.getParameter("pcategory"));

            String oldImage = request.getParameter("old_pimage");
            String fileName = oldImage; 

            Part part = request.getPart("new_pimage"); 
            if (part != null && part.getSize() > 0) {
                fileName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                String buildPath = getServletContext().getRealPath("/images");
                File buildDir = new File(buildPath);
                if (!buildDir.exists()) buildDir.mkdir();
                part.write(buildPath + File.separator + fileName);

                try {
                    String appPath = getServletContext().getRealPath("/"); 
                    String sourcePath = appPath.replace("build\\web", "web\\images").replace("build/web", "web/images");
                    File sourceDir = new File(sourcePath);
                    if (!sourceDir.exists()) sourceDir.mkdirs();
                    part.write(sourcePath + File.separator + fileName);
                } catch (Exception e) { }
            }

            ProductDao dao = new ProductDao();
            dao.updateProduct(id, name, desc, price, discount, stock, fileName, cateID);
            
            request.getSession().setAttribute("msgSuccess", "Cập nhật sản phẩm #" + id + " thành công!");
            response.sendRedirect(targetPage);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("msgError", "Lỗi dữ liệu hệ thống, cập nhật thất bại!");
            response.sendRedirect(targetPage);
        }
    }
}