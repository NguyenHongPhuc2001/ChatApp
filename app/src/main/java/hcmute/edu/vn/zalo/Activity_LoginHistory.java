package hcmute.edu.vn.zalo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import hcmute.edu.vn.zalo.adapter.fragmentLoginHistoryAdapter;
import hcmute.edu.vn.zalo.model.LoginHistory;

public class Activity_LoginHistory extends AppCompatActivity {
    private ArrayList<LoginHistory> arrayLoginHistory, arrayLoginHistoryReverse; //khai báo biến arraylist để chưa dữ liệu từ database.
    private fragmentLoginHistoryAdapter adapter;  //khai báo adapter để xử lý và binding dữ iệu.
    private ListView lsHistory; //khai báo listview dùng để show dữ liệu.
    private ImageButton btnBack;
    private String Status, uPhone, uPass;

    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();   //khai báo đường dẫn kết nối tới firebase realtime database
    private DatabaseReference ref = db.child("History Login");  //hướng đường dẫn đến node tên "History Login"
    private DatabaseReference ref_2 = db.child("User");     //hướng đường dẫn đến node tên "User"


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_loginhistory);

        uPhone = getIntent().getExtras().getString("phone");
        uPass = getIntent().getExtras().getString("pass");

        //Ánh xạ listview & bntBack tới listview và imagebutton trong file layout.
        lsHistory = findViewById(R.id.lsv_fragmentLoginHistory_ListDevice);
        btnBack = findViewById(R.id.imgbutton_fragmentLoginHistory_Back);

        //Khởi tạo arraylist rỗng để chứa dữ liệu từ database và dữ liệu sau khi xử lý.
        arrayLoginHistory = new ArrayList<>();
        arrayLoginHistoryReverse = new ArrayList<>();
        //gọi hàm setdata để đẩy dữ liệu từ database vào arraylist.
        setData();

        //Khởi tạo adapter và truyền vào 3 tham số: context, layout, arraylist<LoginHistory>
        // chi tiết trong fragmentLoginHistoryAdapter.
        adapter = new fragmentLoginHistoryAdapter(this, R.layout.dong_loginhistory, arrayLoginHistoryReverse);

        //Set adapter cho listview.
        lsHistory.setAdapter(adapter);

        //bắt sự kiện onCLick của btnBack để trở về fragmentSetting.
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Khởi tạo bundle để truyền dữ liệu từ activityLoginHistory về fragmentSetting.
                Bundle bundle = new Bundle();

                //truyền giá trị với key (bất cứ tên nào cũng được).
                bundle.putString("phone", uPhone);
                bundle.putString("pass", uPass);

                //Khởi tạo fragmentSetting để làm nơi truyền dữ liệu về.
                fragment_Settings fragmentSettings = new fragment_Settings();

                //dùng phương thức setArguments và truyền tham số bundle để gửi dữ liệu qua fragmentSetting.
                fragmentSettings.setArguments(bundle);

                //gọi method onBackPressed() để trở về activity/fragment trước đó.
                onBackPressed();
            }
        });
    }

    private void setData() {
        //Khởi tạo sự kiện valueEventListener để get data từ firebase.
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear arraylist sau mỗi lần get data để tránh bị trùng dữ liệu.
                arrayLoginHistory.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Với mỗi phần tử từ database ta đưa về model LoginHistory để các giá trị của chứa phần tử đó.
                    LoginHistory history = dataSnapshot.getValue(LoginHistory.class);

                    //add vào arraylist sau khi đã có giá trị từ firebase
                    arrayLoginHistory.add(new LoginHistory(history.getDeviceName(), history.getTime(),history.getUser()));
                }

                //khởi tạo vòng lặp chạy ngược từ phần tử cuối đến phần tử đầu của arrayLoginHistory
                for (int i = arrayLoginHistory.size() - 1; i >= 0; i--) {

                    if(arrayLoginHistory.get(i).getUser().equals(uPhone)){
                        //add các phần tử lần lượt từ phần tử cuối -> đầu của arrayLoginHistory vào arrayLoginHistoryReverse
                        //để lấy phần tử ở vị trí i ta dùng phương thức: .get(i).
                        arrayLoginHistoryReverse.add(arrayLoginHistory.get(i));
                    }
                }
                //lắng nghe sự thay đổi của database để thay đổi giá trị trên listview
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        //từ đường dẫn tới database dùng phương thức addListenerForSingleValueEvent &  addValueEventListener để lắng nghe sự thay đổi của
        //dữ liệu và removeEventListener để xóa sự kiện lắng nghe.
        ref.addListenerForSingleValueEvent(valueEventListener);
        ref.addValueEventListener(valueEventListener);
        ref.removeEventListener(valueEventListener);
    }

    //khởi tạo hàm updateStatus để update trạng thái của tài khoản: online -> offline và ngược lại.
    private void updateStatus(String status) {
        //khởi tạo hashmap
        HashMap<String, Object> hashMap = new HashMap<>();
        //chèn giá trị status khi tài khoản on hoặc off
        hashMap.put("status", status);
        //update lên firebase
        ref_2.child(uPhone).updateChildren(hashMap);
    }

    //khi activity  tiếp tục hoạt động thì set trạng thái là online và update lên firebase
    @Override
    protected void onResume() {
        super.onResume();
        //set trạng thái -> online
        Status = "online";
        //update lên firebase
        updateStatus(Status);
    }

    //khi activity kết thúc thì set trạng thái là offline và update lên firebase
    @Override
    protected void onPause() {
        super.onPause();
        //set trạng thái -> offline
        Status = "offline";
        //update lên firebase
        updateStatus(Status);
    }
}