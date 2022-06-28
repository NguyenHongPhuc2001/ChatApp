package hcmute.edu.vn.zalo.model;

public class User {
    //khai báo các thuộc tính của model User
    private String userPhone;
    private String userPassword;
    private String userName;
    private String userImage;
    private String userCover;
    private int userAge;
    private String userDateofBirth;
    private String userSex;
    private String status;

    //khởi tạo constructor của User gồm các thuộc tính đã khai báo
    public User(String userPhone, String userPassword, String userName, String userImage
            , String userCover, int userAge, String userDateofBirth, String userSex, String status) {
        //ánh xạ thuộc tính đã khai báo bằng giá trị các biến được truyền vào constructor
        this.userPhone = userPhone;
        this.userPassword = userPassword;
        this.userName = userName;
        this.userImage = userImage;
        this.userAge = userAge;
        this.userCover = userCover;
        this.userDateofBirth = userDateofBirth;
        this.userSex = userSex;
        this.status = status;
    }

    //khởi tạo một init constructor
    public User() {
    }

    //dưới đây là các hàm getter và setter
    //với hàm get thì ta không truyền tham số gì cả chỉ trả về giá trị thuộc tính được khai báo
    public String getUserPhone() {
        return userPhone;
    }

    //hàm set thì sẽ truyền vào tham số có type cùng với type của thuộc tính đươc
    //khai báo ở trên và sẽ set giá trị của thuộc tính bằng giá trị được truyền vào
    // trong hàm set. Tương tự các hàm get-set khác
    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserCover() {
        return userCover;
    }

    public void setUserCover(String userCover) {
        this.userCover = userCover;
    }

    public int getUserAge() {
        return userAge;
    }

    public void setUserAge(int userAge) {
        this.userAge = userAge;
    }

    public String getUserDateofBirth() {
        return userDateofBirth;
    }

    public void setUserDateofBirth(String userDateofBirth) {
        this.userDateofBirth = userDateofBirth;
    }

    public String getUserSex() {
        return userSex;
    }

    public void setUserSex(String userSex) {
        this.userSex = userSex;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
