package dao;

import dal.DBContext;
import model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ProductDao {
    // Các kết nối sẽ được quản lý cục bộ bằng try-with-resources

    // 1. LẤY TẤT CẢ SẢN PHẨM (TỐI ƯU BẰNG JOIN AN TOÀN)
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();

        // 🟢 ĐÃ FIX LỖI: Câu lệnh SQL này đảm bảo luôn lấy ra TẤT CẢ sản phẩm dù có đơn hàng nào Completed hay chưa
        String sql = "SELECT p.product_id, p.product_name, p.description, p.price, p.stock, p.image, p.category_id, p.discount, "
                + "ISNULL((SELECT SUM(od.quantity) "
                + "        FROM order_details od "
                + "        JOIN orders o ON od.order_id = o.order_id "
                + "        WHERE od.product_id = p.product_id AND o.status = N'Completed'), 0) as sold "
                + "FROM products p "
                + "ORDER BY p.product_id DESC";

        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getString("image"),
                        rs.getInt("category_id"),
                        rs.getInt("sold"), // Sẽ trả về 0 nếu chưa bán được bó nào
                        5.0,
                        rs.getInt("discount")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Product> getHomeProducts() {
        return getAllProducts();
    }

    // 2. LẤY CHI TIẾT 1 SẢN PHẨM (TỐI ƯU BẰNG JOIN AN TOÀN)
    public Product getProductById(int id) {
        String sql = "SELECT p.product_id, p.product_name, p.description, p.price, p.stock, p.image, p.category_id, p.discount, "
                + "ISNULL((SELECT SUM(od.quantity) "
                + "        FROM order_details od "
                + "        JOIN orders o ON od.order_id = o.order_id "
                + "        WHERE od.product_id = p.product_id AND o.status = N'Completed'), 0) as sold "
                + "FROM products p "
                + "WHERE p.product_id = ?";

        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            rs.getString("image"),
                            rs.getInt("category_id"),
                            rs.getInt("sold"),
                            5.0,
                            rs.getInt("discount")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 3. CÁC HÀM KHÁC (GIỮ NGUYÊN)
    public int getTotalProducts() {
        String query = "SELECT COUNT(*) FROM products";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void insertProduct(String name, String description, double price, int discount, int stock, String image, int cateID) {
        String query = "INSERT INTO products (product_name, [description], price, discount, stock, [image], category_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setDouble(3, price);
            ps.setInt(4, discount);
            ps.setInt(5, stock);
            ps.setString(6, image);
            ps.setInt(7, cateID);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateProduct(int id, String name, String desc, double price, int discount, int stock, String image, int cateId) {
        String sql = "UPDATE products SET product_name=?, description=?, price=?, discount=?, stock=?, category_id=? ";
        if (image != null && !image.isEmpty()) {
            sql += ", image=? WHERE product_id=?";
        } else {
            sql += " WHERE product_id=?";
        }
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, desc);
            ps.setDouble(3, price);
            ps.setInt(4, discount);
            ps.setInt(5, stock);
            ps.setInt(6, cateId);
            if (image != null && !image.isEmpty()) {
                ps.setString(7, image);
                ps.setInt(8, id);
            } else {
                ps.setInt(7, id);
            }
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deleteProduct(String id) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static List<Map<String, Object>> cachedCategories = null;
    private static long lastCacheTime = 0;

    public List<Map<String, Object>> getAllCategories() {
        if (cachedCategories != null && (System.currentTimeMillis() - lastCacheTime) < 60000) {
            return cachedCategories;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("cid", rs.getInt(1));
                map.put("cname", rs.getString(2));
                list.add(map);
            }
            cachedCategories = list;
            lastCacheTime = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Map<Integer, Product> getProductsForCart(java.util.Set<Integer> productIds) {
        Map<Integer, Product> map = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) {
            return map;
        }

        StringBuilder sql = new StringBuilder("SELECT product_id, product_name, description, price, stock, image, category_id, discount FROM products WHERE product_id IN (");
        for (int i = 0; i < productIds.size(); i++) {
            sql.append("?");
            if (i < productIds.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");

        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            for (Integer id : productIds) {
                ps.setInt(index++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product(
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            rs.getString("image"),
                            rs.getInt("category_id"),
                            0,
                            5.0,
                            rs.getInt("discount")
                    );
                    map.put(p.getId(), p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public List<Product> searchAndSortProducts(String keyword, String categoryId, String sortType) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT p.product_id, p.product_name, p.description, p.price, p.stock, p.image, p.category_id, p.discount, "
                + "ISNULL((SELECT SUM(od.quantity) FROM order_details od JOIN orders o ON od.order_id = o.order_id WHERE od.product_id = p.product_id AND o.status = N'Completed'), 0) as sold "
                + "FROM products p WHERE 1=1");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND product_name LIKE ?");
        }
        if (categoryId != null && !categoryId.equals("all") && !categoryId.isEmpty()) {
            sql.append(" AND category_id = ?");
        }

        if ("bestseller".equals(sortType)) {
            sql.append(" ORDER BY sold DESC");
        } else if ("price_asc".equals(sortType)) {
            sql.append(" ORDER BY (price * (1 - discount/100.0)) ASC");
        } else if ("price_desc".equals(sortType)) {
            sql.append(" ORDER BY (price * (1 - discount/100.0)) DESC");
        } else {
            sql.append(" ORDER BY product_id DESC");
        }

        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(paramIndex++, "%" + keyword.trim() + "%");
            }
            if (categoryId != null && !categoryId.equals("all") && !categoryId.isEmpty()) {
                ps.setInt(paramIndex++, Integer.parseInt(categoryId));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Product(rs.getInt("product_id"), rs.getString("product_name"), rs.getString("description"), rs.getDouble("price"), rs.getInt("stock"), rs.getString("image"), rs.getInt("category_id"), rs.getInt("sold"), 5.0, rs.getInt("discount")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Map<String, Object>> getReviewsByProductId(int productId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT r.rating, r.comment, r.review_date, u.fullname, u.email FROM reviews r JOIN users u ON r.user_id = u.user_id WHERE r.product_id = ? ORDER BY r.review_date DESC";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("rating", rs.getInt("rating"));
                    map.put("comment", rs.getString("comment"));
                    map.put("review_date", rs.getString("review_date"));
                    map.put("fullname", rs.getString("fullname"));
                    map.put("email", rs.getString("email"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 4. THÊM ĐÁNH GIÁ SẢN PHẨM MỚI VÀO DATABASE
    public boolean insertReview(int userId, int productId, int rating, String comment) {
        String sql = "INSERT INTO reviews (user_id, product_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setInt(3, rating);
            ps.setString(4, comment);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
