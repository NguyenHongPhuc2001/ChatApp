package hcmute.edu.vn.zalo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Activity_LoginOption extends AppCompatActivity{
    //khai báo các biến cần dùng
    private Button btnLogin, btnSignup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_loginoption);

        //ánh xá các biến tới layout cần sử dụng
        btnLogin = findViewById(R.id.button_PageLoginSignup_DangNhap);
        btnSignup = findViewById(R.id.button_PageLoginSignup_DangKy);


        //khi nhấn vào nút login sẽ chuyển qua màn hình login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //khỏi tạo một intent mới với 2 tham số là activity đang đứng và activity đích đến
                Intent intent = new Intent(Activity_LoginOption.this, Activity_Login.class);
                //dùng hàm startActivity để bắt đầu chuyển đến trang đăng nhập
                startActivity(intent);
            }
        });

        //khi nhắn vào nút signup sẽ chuyển đến trang đăng ký tài khoản
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //khởi tạo intent mới với 2 tham số là activity đang đứng và activity đích đến
                Intent intent = new Intent(Activity_LoginOption.this, Activity_Signup.class);
                //dùng hàm startActivity để bắt đầu chuyển qua trang đăng ký.
                startActivity(intent);
            }
        });
    }
}
