package hcmute.edu.vn.zalo.model;

public class Contact {
    //khai báo các thuộc tính của model Contact
    public String name;
    public String phoneNumber;

    //khởi tạo constructor rỗng
    public Contact() {
    }


    //khởi tạo constructor của Contact gồm các thuộc tính đã khai báo
    public Contact(String name, String phoneNumber ) {
        //ánh xạ thuộc tính đã khai báo bằng giá trị các biến được truyền vào constructor
        this.name = name;
        this.phoneNumber = phoneNumber;
    }


    //dưới đây là các hàm getter và setter
    //với hàm get thì ta không truyền tham số gì cả chỉ trả về giá trị thuộc tính được khai báo
    public String getName() {
        return name;
    }

    //hàm set thì sẽ truyền vào tham số có type cùng với type của thuộc tính đươc
    //khai báo ở trên và sẽ set giá trị của thuộc tính bằng giá trị được truyền vào
    // trong hàm set. Tương tự các hàm get-set khác
    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
