package controller;

import dao.ProductDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024 * 50)
public class AddProductController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ProductDao dao = new ProductDao();
        request.setAttribute("listCC", dao.getAllCategories());
        request.setAttribute("fromPage", request.getParameter("from"));
        
        // ĐÃ SỬA: Khớp chuẩn 100% tên file addProduct.jsp chữ thường của bạn
        request.getRequestDispatcher("view/addProduct.jsp").forward(request, response);
    }

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
            String name = request.getParameter("pname");
            String desc = request.getParameter("pdescription");
            double price = Double.parseDouble(request.getParameter("pprice"));
            int discount = Integer.parseInt(request.getParameter("pdiscount"));
            int stock = Integer.parseInt(request.getParameter("pstock"));
            int cateID = Integer.parseInt(request.getParameter("pcategory"));

            Part part = request.getPart("pimage");
            String fileName = "";
            if(part != null) {
                fileName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
            }

            if (fileName != null && !fileName.isEmpty()) {
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
            dao.insertProduct(name, desc, price, discount, stock, fileName, cateID);

            request.getSession().setAttribute("msgSuccess", "Đã thêm hoa mới vào kho thành công!");
            response.sendRedirect(targetPage);

        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("msgError", "Thêm sản phẩm thất bại. Kiểm tra lại thông tin!");
            response.sendRedirect(targetPage);
        }
    }
}