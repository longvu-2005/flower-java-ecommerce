package controller;

import dao.ProductDao;
import model.Account;
import model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CheckoutController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            HttpSession session = request.getSession();
            Account userSession = (Account) session.getAttribute("userSession");
            
            // 1. KIỂM TRA ĐĂNG NHẬP
            if (userSession == null) {
                session.setAttribute("errorMsg", "Vui lòng đăng nhập hoặc đăng ký tài khoản mới để tiến hành thanh toán!");
                response.sendRedirect("Login"); 
                return;
            }

            // Nạp dữ liệu header
            setHeaderData(request);
            
            // 2. LẤY GIỎ HÀNG TỪ SESSION
            Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
            
            List<Product> cartList = new ArrayList<>();
            double subTotal = 0;
            
            if (cart != null && !cart.isEmpty()) {
                ProductDao pDao = new ProductDao();
                Map<Integer, Product> mapProducts = pDao.getProductsForCart(cart.keySet());
                for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                    Product p = mapProducts.get(entry.getKey());
                    if (p != null) {
                        double finalPrice = p.getPprice();
                        if (p.getDiscount() > 0) {
                            finalPrice = p.getPprice() * (1 - (double) p.getDiscount() / 100);
                        }
                        p.setSold(entry.getValue()); 
                        subTotal += finalPrice * entry.getValue();
                        cartList.add(p);
                    }
                }
            }
            
            // 3. TÍNH THUẾ VAT VÀ TỔNG TIỀN CHỐT
            double vat = subTotal * 0.08; 
            double grandTotal = subTotal + vat;
            
            // 4. ĐẨY DỮ LIỆU SANG JSP
            request.setAttribute("cartList", cartList);
            request.setAttribute("subTotal", subTotal);
            request.setAttribute("vat", vat);
            request.setAttribute("grandTotal", grandTotal);
            
            // 5. CHUYỂN HƯỚNG VỀ VIEW
            request.getRequestDispatcher("view/checkout.jsp").forward(request, response);
            
        } catch (Exception e) {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<h1 style='color:red;'>⚠️ PHÁT HIỆN LỖI BACKEND:</h1>");
            out.println("<pre style='background:#f4f4f4; padding:20px; font-size:16px; border-left: 5px solid red;'>");
            e.printStackTrace(out);
            out.println("</pre>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}