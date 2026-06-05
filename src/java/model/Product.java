package model;

public class Product {

    private int id;
    private String pname;
    private String pdescription;
    private double pprice;
    private int pstock;
    private String pimage;
    private int cateID;
    private int sold;     // Số lượng đã bán
    private double review; // Điểm đánh giá (vd: 4.5)
    private int discount;
    private int stock;

    // Constructor đã được sửa lại để gán đúng các giá trị
    public Product(int id, String pname, String pdescription, double pprice, int pstock, String pimage, int cateID, int sold, double review, int discount) {
        this.id = id;
        this.pname = pname;
        this.pdescription = pdescription;
        this.pprice = pprice;
        this.pstock = pstock;
        this.pimage = pimage;
        this.cateID = cateID;
        this.sold = sold;
        this.review = review;
        this.discount = discount;
        this.stock = pstock;
    }

    public Product(int id, String pname, String pdescription, double pprice, int pstock, String pimage, int cateID, int sold, double review, int discount, int stock) {
        this.id = id;
        this.pname = pname;
        this.pdescription = pdescription;
        this.pprice = pprice;
        this.pstock = pstock;
        this.pimage = pimage;
        this.cateID = cateID;
        this.sold = sold;
        this.review = review;
        this.discount = discount;
        this.stock = stock;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getPdescription() {
        return pdescription;
    }

    public void setPdescription(String pdescription) {
        this.pdescription = pdescription;
    }

    public double getPprice() {
        return pprice;
    }

    public void setPprice(double pprice) {
        this.pprice = pprice;
    }

    public int getPstock() {
        return pstock;
    }

    public void setPstock(int pstock) {
        this.pstock = pstock;
    }

    public String getPimage() {
        return pimage;
    }

    public void setPimage(String pimage) {
        this.pimage = pimage;
    }

    public int getCateID() {
        return cateID;
    }

    public void setCateID(int cateID) {
        this.cateID = cateID;
    }

    public int getSold() {
        return sold;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }

    public double getReview() {
        return review;
    }

    public void setReview(double review) {
        this.review = review;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "Product{" + "id=" + id + ", pname=" + pname + ", pdescription=" + pdescription + ", pprice=" + pprice + ", pstock=" + pstock + ", pimage=" + pimage + ", cateID=" + cateID + ", sold=" + sold + ", review=" + review + ", discount=" + discount + ", stock=" + stock + '}';
    }

}
