package hcmute.edu.vn.zalo;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import hcmute.edu.vn.zalo.model.User;


public class Activity_Login extends AppCompatActivity {

    //khai báo các biến sử dụng
    private Button btnLogin, btnSignup;
    private EditText edtPhoneNB, edtPassword, edtUsername;
    private AlertDialog dialog;
    private String uPhone;
    int count = 0;// khai báo biến count để check xem tài khoản có trong db hay không


    //tạo đường dẫn đến firebase
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference ref = db.child("User"); // chỉ đường dẫn đến nút User
    private DatabaseReference ref_2 = db.child("History Login"); //chỉ đường dẫn đến nút History Login


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_login);

        // ánh xạ các biến đã khai báo đến file layout sử dụng trong activity này
        btnSignup = findViewById(R.id.button_PageLogin_DangKy);
        btnLogin = findViewById(R.id.button_PageLogin_DangNhap);
        edtPhoneNB = findViewById(R.id.edittext_PageLogin_Sodienthoai);
        edtPassword = findViewById(R.id.edittext_PageLogin_Matkhau);


        // khi click vào btnLogin sẽ thực hiện chức năng đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hiện dialog trong khi thực hiện so sánh tài khoản trên db
                DialogProcessing();
                // tạo một User mới với phone và pass lấy từ edt, những thuộc tính còn lại của User thì set là null
                // set null bỏi vì khi vào trong app thì User sẽ update thông tin của bản thân
                User user = new User(edtPhoneNB.getText().toString(), edtPassword.getText().toString(), null,
                        null, null, 0, null, null, "online");


                //tạo một valueEventListener thực hiện viêc so sánh tài khoản từ màn hình đăng nhập với tài khoản trên database
                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //khởi tạo 1 user mới để chứa thông tin của user trên db khi get dữ liệu về.
                        User u = new User();
                        //chạy vòng lặp trên tất cả các tài khoản có trên db để kiểm tra
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            //thực hiện get dữ liệu từ database về class User
                            u = dataSnapshot.getValue(User.class);
                            //so sánh số điện thoại và password nếu trùng nhau thì tiến hành tăng biến count lên 1
                            if (user.getUserPhone().equals(u.getUserPhone()) && user.getUserPassword().equals(u.getUserPassword())) {
                                count = count + 1;
                            }
                        }
                        //khi biến count tăng chứng tỏ là tài khoản chính xác, nếu coutn vẫn bằng 0 => tài khoản hoặc mật khẩu không chính xác
                        //=> in ra thông báo cho người dùng thấy để xem lại tài khoản và mật khẩu
                        if (count == 0) {
                            //dùng Toast để thông báo ra màn hình: context - lấy activity hiện tại,text - câu muốn thông báo, LENGTH_SHORT - hiện thị thông báo trong thời gian ngắn, show() - hiển thị thông báo ra màn hình
                            Toast.makeText(Activity_Login.this, "Số điện thoại hoặc mật khẩu không chính xác !", Toast.LENGTH_SHORT).show();
                            //Khi tài khoản hoặc mật khẩu không thành công thì tắt dialog
                            dialog.cancel();
                        } else {
                            //gán count lại bằng 0  để xử lý cho nhưng trường hợp đăng nhập tiếp theo
                            count = 0;
                            //khởi tạo Intent tới activity mới, truyền vào 2 tham số là activity đang đúng và activity đích đến
                            Intent intent = new Intent(Activity_Login.this, Activity_Home.class);
                            //lấy số điện thoại của tài khoản đăng nhập
                            uPhone = user.getUserPhone();
                            //dùng putExtra chuyển thông tin về SĐT và password sang activity đích để sừ dụng
                            intent.putExtra("phone", user.getUserPhone());
                            intent.putExtra("pass", user.getUserPassword());
                            intent.putExtra("success", 1); // biến success dùng để kiểm tra đăng nhập thành công hay chưa để in ra thông báo bên activity đích
                            //khởi tạo 1 date mới, măc định là ngày hiện tại theo hệ thống
                            Date date = new Date();
                            //khởi tạo SimpleDateFormat để định dạng ngày và thời gian theo ý muốn
                            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            // dùng method format và truyền vào biến date để định dạng biến date về đúng định dạng mong muốn
                            String time = format.format(date);
                            //khởi tạo biến deviceName để lấy tên thiết bị
                            //sử dụng hàm có sẵn của hệ thống là Build.MODEL hàm này sẽ trả về tên của điện thoại.
                            String deviceName = Build.MODEL;
                            //hàm SaveHistory dùng để lưu thông tin đăng nhập của tài khoản đó trên thiết bị nào vào thời gian nào.
                            //truyền vào 3 biến là tên thiết bị, thời gian đăng nhập  vào lúc nhấn button login và số điện thoại của tài khoản login.
                            SaveHistory(deviceName, time, uPhone);
                            //dùng lệnh startActivity truyền vào 1 intent để chuyển qua activity mới.
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };


                //add valueenvent cho đường dẫn đến nút cần thực hiện ở trên firebase
                //truyền vào một valueEventListener (đã được định nghĩa các công việc thực hiện trong event đó)
                ref.addListenerForSingleValueEvent(valueEventListener);
                //sau khi thực xong thì xóa event đó đi.
                ref.removeEventListener(valueEventListener);
            }

        });

        //khi click vào button signup thì sẽ chuyển đến trang đăng ký tài khoản
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Khởi tạo intent mới truyền vào 2 biến lần lượt là activity đang đứng và acitvity đích đến
                Intent intent = new Intent(Activity_Login.this, Activity_Signup.class);
                //dùng lệnh startActivity để bắt đầu chuyển qua trang đăng ký tài khoản
                startActivity(intent);
            }
        });
    }

    // Hàm SaveHistory dùng để lưu lại thông tin đăng nhập của tài khoản trên thiết bị nào và vào thời gian nào.
    //truyền vào 3 tham số là tên thiết bị dùng để đăng nhập, thời gian ngay tại lúc đăng nhập và số điện thoại của tài khoản đăng nhập.
    private void SaveHistory(String deviceName, String time, String userPhone) {
        //khởi tạo một hashmap mới
        HashMap<String, Object> hashMap = new HashMap<>();
        //lần lượt truyền vào 3 giá trị là tên thiết bị, thời gian và sô diện thoại của tài khoản để lưu lại trên databse.
        //dùng hàm put() để lưu lại giá trị, truyền vào 2 tham số lần lượt là: tên biến và giá trị truyền vào.
        //lưu ý khi biến dùng để lưu trữ dữ liệu thì phải trùng với tên thuộc tính trong các model được định nghĩa
        //ví dụ: Model LoginHistory có 3 thuộc tính là deviceName, time và user thì biến dùng để lưu trong hashmap cũng phải trùng với tên biến trong model
        //vì khi get dữ liệu từ database nếu tên biến không trùng thì sẽ không get được dữ liệu
        hashMap.put("deviceName", deviceName); //lưu tên thiết bị vào biến deviceName
        hashMap.put("time", time);  //lưu thời gian vào biến time
        hashMap.put("user", userPhone);  //lưu số diện thoại của tài khoản đăng nhập vào biến user
        // dùng lệnh push() để đẩy dữ liệu lên database và setValue() truyền vào dữ liệu cần đẩy lên
        //lệnh push() sẽ tạo random 1 key làm nút với đường đẫn tới nút History Login
        ref_2.push().setValue(hashMap);
    }

    //hàm DialogProcessing để hiển thị dialog khi đang chạy kiểm tra tài khoản có trong db hay không
    private void DialogProcessing() {
        //khởi tạo builder và xác định context nơi sẽ builder dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //khởi tạo inflater
        LayoutInflater inflater = this.getLayoutInflater();
        //dùng method inflate để get layout muốn hiển thị trong dialog, root: để là null vì ta chỉ get layout ra chứ không hiển thị ngay lập tức
        builder.setView(inflater.inflate(R.layout.dialog_processing, null));
        // method setCancleable dùng để tắt dialog bằng phím back trên điện thoại
        builder.setCancelable(true);

        //tạo dialog bằng lệnh create
        dialog = builder.create();
        //hiển thị dialog
        dialog.show();
    }

    //khi đăng nhập thành công và chuyển qua activity mới thì dialog sẽ được hủy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //hủy dialog khi activity bị hủy
        dialog.cancel();
    }
}



