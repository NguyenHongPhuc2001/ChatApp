package hcmute.edu.vn.zalo.model;

public class Chat {
    // Khai báo các thuộc tính của model Chat
    private String sender;
    private String receiver;
    private String message;
    private int isImageMessage;
    private String time;

    //khởi tạo constructor rỗng
    public Chat() {
    }

    //khởi tạo constructor của Chat gồm các thuộc tính đã khai báo
    public Chat(String sender, String receiver, String message, int isImageMessage, String time) {
        //ánh xạ thuộc tính đã khai báo bằng giá trị các biến được truyền vào constructor
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isImageMessage = isImageMessage;
        this.time = time;
    }


    //dưới đây là các hàm getter và setter
    //với hàm get thì ta không truyền tham số gì cả chỉ trả về giá trị thuộc tính được khai báo
    public String getSender() {
        return sender;
    }

    //hàm set thì sẽ truyền vào tham số có type cùng với type của thuộc tính đươc
    //khai báo ở trên và sẽ set giá trị của thuộc tính bằng giá trị được truyền vào
    // trong hàm set. Tương tự các hàm get-set khác
    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getIsImageMessage() {
        return isImageMessage;
    }

    public void setIsImageMessage(int isImageMessage) {
        this.isImageMessage = isImageMessage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
