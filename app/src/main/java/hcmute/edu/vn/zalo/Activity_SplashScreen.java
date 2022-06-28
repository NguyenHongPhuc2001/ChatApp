package hcmute.edu.vn.zalo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Activity_SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set layout hiển thị khi activity bắt đầu
        setContentView(R.layout.page_splashscreen);
        //khởi tạo 1 new Handler để delay activity
        //dùng method postDelayed để tạo delay cho activity, truyền vào 2 tham số là Runnable và thời gian delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //khởi tạo intent dẫn đến activityLoginOption và dùng hàm startActivity để bắt đầu quá trình chuyển đến activity mới
                startActivity(new Intent(Activity_SplashScreen.this, Activity_LoginOption.class));
            }
        }, 2000);// chọn thời gian delay (đơn vị là mili giây)
    }
}
