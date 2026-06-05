package controller;

import dao.AccountDAO;
import dao.CouponDao;
import dao.MessageDao;
import model.Account;
import model.Coupon;
import model.CouponUsage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Long
 */
public class ManageCouponController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setHeaderData(request);
        
        HttpSession session = request.getSession();
        Account userSession = (Account) session.getAttribute("userSession");
        if (userSession == null || userSession.getRoleId() != 1) {
            response.sendRedirect("Login");
            return;
        }

        String action = request.getParameter("action");
        CouponDao dao = new CouponDao();

        if ("delete".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            if (dao.deleteCoupon(id)) {
                session.setAttribute("msgSuccess", "Đã xóa mã giảm giá thành công!");
            } else {
                session.setAttribute("msgError", "Lỗi: Không thể xóa mã này!");
            }
            response.sendRedirect("ManageCoupon");
            return;
        }

        if ("disable".equals(action)) {
            String code = request.getParameter("code");
            dao.disableCoupon(code);
            session.setAttribute("msgSuccess", "Đã vô hiệu hóa mã: " + code);
            response.sendRedirect("ManageCoupon");
            return;
        }

        if ("broadcast".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            List<Coupon> allCoupons = dao.getAllCoupons();
            Coupon target = null;
            for(Coupon c : allCoupons) {
                if(c.getId() == id) {
                    target = c; 
                    break;
                }
            }
            if (target != null && target.getStatus() == 1) {
                AccountDAO aDao = new AccountDAO();
                List<Account> users = aDao.getAllCustomers();
                MessageDao mDao = new MessageDao();
                String minValStr = target.getMinOrderValue() > 0 ? (" cho đơn từ " + String.format("%,.0f", target.getMinOrderValue()) + "đ") : "";
                String msg = "🎁 TẶNG BẠN MÃ GIẢM GIÁ: " + target.getCode() + " giảm " + target.getDiscountPercent() + "%" + minValStr + "!";
                
                for(Account u : users) {
                    mDao.saveMessage(u.getUserId(), u.getEmail(), "admin", msg);
                }
                session.setAttribute("msgSuccess", "Đã phát mã cho " + users.size() + " khách hàng!");
            } else {
                session.setAttribute("msgError", "Mã không hợp lệ hoặc đã hết hạn!");
            }
            response.sendRedirect("ManageCoupon");
            return;
        }

        // Lấy danh sách Coupon
        List<Coupon> coupons = dao.getAllCoupons();
        
        // Lấy lịch sử sử dụng
        Map<Integer, List<CouponUsage>> usageMap = new HashMap<>();
        for (Coupon c : coupons) {
            usageMap.put(c.getId(), dao.getCouponUsageHistory(c.getId()));
        }

        request.setAttribute("coupons", coupons);
        request.setAttribute("usageMap", usageMap);
        request.getRequestDispatcher("view/manageCoupon.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Account userSession = (Account) session.getAttribute("userSession");
        if (userSession == null || userSession.getRoleId() != 1) {
            response.sendRedirect("Login");
            return;
        }

        String action = request.getParameter("action");
        CouponDao dao = new CouponDao();

        if ("add".equals(action)) {
            String code = request.getParameter("code");
            int percent = Integer.parseInt(request.getParameter("discountPercent"));
            String expiry = request.getParameter("expiryDate");
            int status = Integer.parseInt(request.getParameter("status"));
            double minVal = Double.parseDouble(request.getParameter("minOrderValue"));

            if (dao.insertCoupon(code, percent, expiry, status, minVal)) {
                session.setAttribute("msgSuccess", "Thêm mã giảm giá thành công!");
            } else {
                session.setAttribute("msgError", "Thêm thất bại (Có thể trùng mã)!");
            }
        } 
        else if ("update".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            String code = request.getParameter("code");
            int percent = Integer.parseInt(request.getParameter("discountPercent"));
            String expiry = request.getParameter("expiryDate");
            int status = Integer.parseInt(request.getParameter("status"));
            double minVal = Double.parseDouble(request.getParameter("minOrderValue"));

            if (dao.updateCoupon(id, code, percent, expiry, status, minVal)) {
                session.setAttribute("msgSuccess", "Cập nhật mã giảm giá thành công!");
            } else {
                session.setAttribute("msgError", "Cập nhật thất bại!");
            }
        }
        else if ("generate".equals(action)) {
            String code = dao.generateCoupon();
            if (code != null) {
                session.setAttribute("msgSuccess", "Đã tạo mã ngẫu nhiên: " + code);
            } else {
                session.setAttribute("msgError", "Tạo mã thất bại!");
            }
        }

        response.sendRedirect("ManageCoupon");
    }
}
