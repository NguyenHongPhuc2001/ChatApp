package hcmute.edu.vn.zalo.model;

import android.util.Log;

public class LoginHistory {
    //khai báo các thuộc tính của model LoginHistory
    private String deviceName;
    private String time;
    private String user;

    //khởi tạo constructor rỗng
    public LoginHistory() {
    }


    //khởi tạo constructor của LoginHistory gồm các thuộc tính đã khai báo
    public LoginHistory(String deviceName, String time, String user) {
        //ánh xạ thuộc tính đã khai báo bằng giá trị các biến được truyền vào constructor
        this.deviceName = deviceName;
        this.time = time;
        this.user = user;
    }

    //dưới đây là các hàm getter và setter
    //với hàm get thì ta không truyền tham số gì cả chỉ trả về giá trị thuộc tính được khai báo
    public String getDeviceName() {
        return deviceName;
    }

    //hàm set thì sẽ truyền vào tham số có type cùng với type của thuộc tính đươc
    //khai báo ở trên và sẽ set giá trị của thuộc tính bằng giá trị được truyền vào
    // trong hàm set. Tương tự các hàm get-set khác
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
