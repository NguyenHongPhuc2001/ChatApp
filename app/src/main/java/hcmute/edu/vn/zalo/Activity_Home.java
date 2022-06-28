package hcmute.edu.vn.zalo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import hcmute.edu.vn.zalo.adapter.ViewPageAdapter;

public class Activity_Home extends AppCompatActivity {
    //khai báo các biến sử dụng
    private BottomNavigationView bottomNavigationView;
    private ViewPager viewPager;
    private String Status, uPhone;


    //khai báo đường dẫn tới database
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference ref_2 = db.child("User");// chỉ đường dẫn tới nút User

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set layout khi bắt đầu activity, truyền vào layout muốn hiển thị
        setContentView(R.layout.page_home);

        //get dữ liệu từ activity trước truyền đến
        //sử dụng getIntent().getExtras() rồi chọn phương thức get phù hợp với kiểu dữ liệu được truyền qua
        int success = getIntent().getExtras().getInt("success");
        //kiểm tra xem dữ liệu truyền qua có đúng bằng 1 hay không, vì bên activity login nếu login được thì gửi dữ liệu có giá trị bằng 1
        //nếu bằng 1 thì hiển thị đăng nhập thành công
        if (success == 1) {
            //dùng Toast truyền vào 3 thang số, activity hiển thị, message muốn hiển thị, thời gian hiển thị (LENGTH_SHORT: hiển thị trong thời gian ngắn)
            // .show() để hiển thị message lên màn hình.
            Toast.makeText(Activity_Home.this, "Đăng nhập thành công !", Toast.LENGTH_SHORT).show();
        }

        //ánh xạ các biến đến file layout được set trong hàm setContentView.
        viewPager = (ViewPager) findViewById(R.id.viewpager_PageHome);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bnv_PageHome_TaskBar);


        //Khởi tạo một ViewPageAdapter mới, truyền vào 2 tham số: fragmentmanager và behavior.
        //adapter sẽ xử lý các fragment và hiển thị lên màn hình, có thanh BottomNavigation để chuyển đổi qua lại giữa các fragment
        //BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT: chỉ hiển thị fragment hiện tại đang được chọn(Resume state)
        // những fragment khác sẽ bị giới hạn và không thể start nếu như không được start.
        //chi tiết trong class ViewPageAdapter
        ViewPageAdapter viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager(),
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        //set adapter cho viewpager, truyền vào adapter vừa khởi tạo
        viewPager.setAdapter(viewPageAdapter);

        //khi fragment nào đang được chọn thì sẽ set trạng thái của BottomNavigation theo trạng thái được chọn.
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            //thực hiện khi fragment được chọn (bằng cách vuốt qua lại)
            @Override
            public void onPageSelected(int position) {
                //dùng switch để thực hiện khi rơi vào một trong các trường hợp bên trong
                switch (position) {
                    case 0:// nếu fragment ở vị trí 0
                        //set icon menu_chats của BottomNavigation thành trạng thái được chọn - setChecked(true)
                        //trạng thái này thì sẽ làm cho biểu tượng và chữ trong layout menu_chats to hơn.
                        bottomNavigationView.getMenu().findItem(R.id.menu_chats).setChecked(true);
                        break; // kết thúc ngay khi thực hiện xong.
                    // tương tự với case 1 và case 2.
                    case 1:
                        bottomNavigationView.getMenu().findItem(R.id.menu_friends).setChecked(true);
                        break;
                    case 2:
                        bottomNavigationView.getMenu().findItem(R.id.menu_settings).setChecked(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //khi click vào biểu tượng trong bottomnavigation sẽ thực hiện chuyển fragment
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //dùng switch để thực hiện khi rơi vào 1 trong các trường hợp
                switch (item.getItemId()) {// lấy id trong menu file
                    case R.id.menu_chats: // nếu bằng id của menu_chats
                        // thực hiện setCurrentItem tại vị trí 0
                        //method setCurrentItem(position) sẽ chuyển qua viewpager ở vị trí mà ta điền vào
                        viewPager.setCurrentItem(0);
                        break;// kết thúc ngay khi thực hiện xong
                    //tương tự với case của menu_friends và menu_settings
                    case R.id.menu_friends:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.menu_settings:
                        viewPager.setCurrentItem(2);
                        break;
                }
                return true; // vì hàm onNavigationItemSelected phải trả về kiểu boolean nên ta có thể trả về true hay false đều được
            }
        });
    }

    //hàm updateStatus dùng để cập nhật trạng thái hoat động của tài khoản (đang online hay offline)
    //hàm cần truyền vào tham số trạng thái và tham số này sẽ có 2 giá trị: "online" & "offline"
    private void updateStatus(String status) {
        //khởi tạo một hashmap mới để lưu dữ liệu
        HashMap<String, Object> hashMap = new HashMap<>();
        //dùng hàm put để set dữ liệu vào biến
        //truyền 2 thams số vào hàm: tên biến (tự chọn),giá trị truyền vào
        hashMap.put("status", status);
        //vì ở activity trước ta đã truyền dữ liệu qua activity này nên ta sẽ dùng getIntent().getExtras() để lấy dữ liệu
        //cụ thể ở đây ta lấy số điện thoại của tài khoản đăng nhập để biet671 là tài khoản nào đang hoạt động
        uPhone = getIntent().getExtras().getString("phone");
        //có được số điện thoại của tài khoản đang hoạt động ta tiến hành update trạng thái của tài khoản đó
        ref_2.child(uPhone).updateChildren(hashMap);
    }


    //khi tài khoản hoạt động thì ta sẽ set status = "online"
    @Override
    protected void onResume() {
        super.onResume();
        //gán giá trị cho biến Status = "online"
        Status = "online";
        //gọi tới hàm updateStatus để cập nhật dữ liệu trạng thái của tài khoản trên database
        updateStatus(Status);
    }

    //khi người dùng chuyển qua một ứng dụng khác hoặc tắt app thì set status ở trạng thái "offline"
    @Override
    protected void onPause() {
        super.onPause();
        //gán giá trị của biến Status = "offline"
        Status = "offline";
        // gọi hàm updateStatus để cập nhật dữ liệu trạng thái của tài khoản trên database
        updateStatus(Status);
    }
}
