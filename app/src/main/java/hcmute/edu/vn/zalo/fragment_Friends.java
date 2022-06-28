package hcmute.edu.vn.zalo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;

import hcmute.edu.vn.zalo.adapter.fragmentFriendsAdapter;
import hcmute.edu.vn.zalo.model.Contact;
import hcmute.edu.vn.zalo.model.User;

public class fragment_Friends extends Fragment {
    private ListView lsv;   //tạo ListView lưu dữ liệu Friends sau khi so sánh
    private fragmentFriendsAdapter adapter;
    private ArrayList<User> accountArray1;   //Khai báo ArrayList có dịnh dạng là User lưu dữ liệu từ DB
    private ArrayList<User> accountArray2;   //Khai báo ArrayList có dịnh dạng là User lưu dữ liệu sau khi đã so sánh với danh bạ
    private ArrayList<Contact> contactList = new ArrayList<>(); //Khai báo ArrayList có dịnh dạnh Contact để lưu danh sách account từ điện thoại
    //PORJECTION là một mảng dạng String chứa các cột ta cần lấy trong danh bạ điện thoại
    // ở đây ta có CONTACT_ID, DISPLAY_NAME và NUMBER sẽ tương ứng với ID của SDT, tên user được lưu trong danh bạ và SĐT của user đó.
    private static final String[] PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };
    private String uPhone, uPass;


    //Khởi tạo dường dẫn tới firebase realtime database sử dụng DadabaseReference
    //getInstance().getReference() sẽ tạo đường link tới database
    //còn .child("User") sẽ tạo đường dẫn tới nút có tên là User (vì firebase tạo database theo mô hình cây)
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference ref = db.child("User"); //Lấy DB table User

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Khởi tạo view ánh xạ tới layout fragment__friends
        View view = inflater.inflate(R.layout.fragment__friends, container, false);

        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS}, PackageManager.PERMISSION_GRANTED);
        }

        // lấy userphone và password từ activity trước đó với key là: userphone là "phone"
        //                                                        password là: "pass"
        uPhone = getActivity().getIntent().getExtras().getString("phone");
        uPass = getActivity().getIntent().getExtras().getString("pass");

        //khởi tạo 2 aarraylist mới
        accountArray1 = new ArrayList<>();
        accountArray2 = new ArrayList<>();

        //Ánh xạ lsv được khai báo tới listview trong fragmentFriends
        lsv = view.findViewById(R.id.lsv_fragmentFriend_ListFriends);


//        while(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_CONTACTS)
//                != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(getActivity(),
//                    new String[]{Manifest.permission.READ_CONTACTS}, PackageManager.PERMISSION_GRANTED);
//        }

        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            setData();
        }

        //Khởi tạo một fragmentFriendAdatper mới và truyền vào các tham số: Context, Layout, ArrayList<User>
        //đọc thêm chi tiết trong fragmentFriendAdapter.
        adapter = new fragmentFriendsAdapter(getActivity(), R.layout.dong_friends, accountArray2);
        //Sau khi đã có apdater thực hiện xử lý dữ liệu ta set adapter cho listview để hiển thị những dữ liệu đó lên.
        lsv.setAdapter(adapter);

        //set phương thức onItemLick cho listview để khi click vào 1 người nào trong listview sẽ dẫn đến PageChat với người đó.
        lsv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Khởi tạo một intent mới để chuyển từ fragmentFriend sang ActivityChatting.
                //Biến đầu tiên sẽ là activity mà ta đang đứng (CurrentActivity), biến tiếp theo chính là Activity mà ta muốn chuyển đến.
                Intent intent = new Intent(getActivity(), Activity_Chatting.class);

                //Ở trong hàm onItemClick sẽ cung cấp cho chúng ta position và dựa vào position ta dùng hàm get() để lấy ra phần tử tương
                //ứng khi ta click trên listview.
                //Vì ArrayList ta truyền vào adapter là User nên các phần tử trên listview sẽ là User -> khởi tạo một User để nhận giá trị từ listview.
                User user = (User) parent.getAdapter().getItem(position);

                //Sau khi đã có dữ liệu của User được chọn ta sẽ đưa một vài thông tin gồm: userPhone, opponentName, opponentPhone, và userPassword
                //để sử dụng bên Activity mà ta muốn chuyển đến.
                intent.putExtra("opponet_phone", user.getUserPhone());
                intent.putExtra("opponent_name", user.getUserName());
                intent.putExtra("phone", uPhone);
                intent.putExtra("pass", uPass);
                //Lệnh startActivity dùng để bắt đầu quá trình chuyển từ fragmentFriend sang ActivityChatting.
                startActivity(intent);
            }
        });

        return view;
    }

    private void setData() {
        //gọi hàm getContactList để load danh bạ từ điện thoại
        getContactList();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Clear array sau mỗi lần chạy để tránh dữ liệu bị lặp lại
                accountArray1.clear();
                accountArray2.clear();
                //Khơi tạo vòng lặp để get tất cả các user có trong User trên database.
                //DataSnapshot là bản sao của dữ liệu. Khi ta gọi lên firebase để get data thì data
                // sẽ được truyền về dưới dạng DataSnapshot.
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //Khởi tạo 1 user để gán giá trị được lấy trên db xuống
                    //User.class: định dạng model phù hợp để truyền dữ liệu phù hợp từ db.
                    User user = dataSnapshot.getValue(User.class);
                    //Sau khi đã get được user ta truyền vào arraylist đã tạo, và vòng lặp sẽ chạy đến khi hết user có trên database.
                    accountArray1.add(user);
                }

                // so sánh SĐT từ danh bạ và DB. Nếu có trùng thì hiển thị là bạn trên app
                //chạy 2 vòng lặp để so sánh danh sách trong điện thoại với danh sách account có trên database.
                //Nếu trùng thì thêm vào arraylist thứ 2 để xuất lên listview.
                // chạy vòng lặp trên tổng account trên db, accountArray1.size() để lấy ra số lượng item có trong arraylist
                for (int i = 0; i < accountArray1.size(); i++) {
                    //tương tự chạy vòng lặp thứ 2 nằm trong vòng lặp thứ nhất để so sánh các số điện thoại
                    // lấy từ danh bạ với số điện thoại trên database
                    for (int j = 0; j < contactList.size(); j++) {
                        String phoneDB = "", phoneContact = "", userPhone = "";

                        //format số điện thoại từ danh bạ và db để so sánh
                        //sử dụng hàm get() để lấy ra số điện thoại, defaultCountryIso chính là mã vùng muốn format(có thể tra google để biết mã vùng)
                        phoneDB = PhoneNumberUtils.formatNumber(accountArray1.get(i).getUserPhone(), "VN");
                        phoneContact = PhoneNumberUtils.formatNumber(contactList.get(j).getPhoneNumber(), "VN");
                        userPhone = PhoneNumberUtils.formatNumber(uPhone, "VN");
                        //sử dụng hàm get() và truyền vào tham số vị trí để lấy ra item tương ứng ở vị trí đó
                        //.equal(condition): dùng để chứa điều kiện so sánh, nếu bằng thì add vào arraylist không thì không làm gì cả

                        if (phoneDB.equals(phoneContact)) {
                            if (phoneDB.equals(userPhone)) {
                            } else {
                                accountArray2.add(accountArray1.get(i)); // vì là ArrayList<User> nên item truyền vào phải thuộc lớp User.
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        //từ đường dẫn đến nút User thêm một valueEventLister để thực hiện trên nút User
        ref.addListenerForSingleValueEvent(valueEventListener);
        //sau khi thực hiện thì xóa sự kiện valueEventListener đi
        ref.addValueEventListener(valueEventListener);
    }


    //khai báo hàm getContactList() để load danh bạ từ thiết bị
    private void getContactList() {
        //check thử app đã được quyền truy cập vào danh bạ cua thiết bị hay chưa
        //sử dụng hàm checkSelfPermission và truyền vào các tham số: context hiện tại, quyền muốn check(ở đây là quyền truy cập vào danh bạ)
        //***LƯU Ý: phải khai báo các quyền sử dụng trong file AndroidManifest.xml
        //nếu quyền chưa có trong package thì sẽ gửi yêu cầu cấp quyền để add vào trong package
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            //sử dụng hàm requestPermission truyền vào 3 tham số: activity hiện tại, quyền cần cấp, package nơi lưu trữ quyền
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS}, PackageManager.PERMISSION_GRANTED);
        }

        //khởi tạo một ContentResolver để get dữ liệu từ thiết bị
        ContentResolver cr = getActivity().getContentResolver();
        //khai báo một con trỏ để chứa dữ liệu mà ta cần
        //sử dụng method query truyền vào 5 tham số nhưng ở đây chúng ta chỉ cần 2 tham số chính:
        //1.ContactsContract.CommonDataKinds.Phone.CONTENT_URI: để get đường dẫn đến nơi lưu dữ liệu của danh bạ
        //2. PROJECTION: là những cột dữ liệu mà ta muốn lấy ra.
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);
        //kiểm tra xem con trỏ có dữ liệu hay không, nếu có thì thực hiện tiếp
        if (cursor != null) {
            //khởi tạo một HashSet kiểu String để chứa dữ liệu load từ danh bạ
            HashSet<String> mobileNoSet = new HashSet<String>();
            try {
                // lấy ra index của cột name và số điện thoại trong danh bạ
                final int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                final int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                //khai báo biến tên và số điện thoại kiều string để lưu dữ liệu
                String name, number;
                //trong khi  dữ liệu trong con trỏ còn thì sẽ thực hiện gán giá trị name và số điện thoại vào 2 biến vừa khởi tạo
                while (cursor.moveToNext()) {
                    // dựa vào index đã lấy sử dụng hàm getString(index) và truyền vào index của cột cần lấy, ở đây sẽ là: cột name và cột số điện thoại
                    name = cursor.getString(nameIndex);
                    number = cursor.getString(numberIndex);
                    //dùng hàm replace để thay thế khoảng trắng trong số điện thoại
                    number = number.replace(" ", "");
                    //kiểm tra xem số điện thoại đã có trong hashset hay chưa bằng hàm contains truyền vào giá trị muốn kiểm tra (ở đây là số điện thoại)
                    //nếu giá trị truyền vào (số điện thoại) chưa có thì add vào arraylist
                    if (!mobileNoSet.contains(number)) {
                        //dùng phương thức add để thêm số điện thoại vào arraylist
                        contactList.add(new Contact(name, number));
                        //dùng phương thức add để thêm số điện thoại vào hashset
                        mobileNoSet.add(number);
                    }
                }
            } finally {
                //sau khi thực hiện xong thì đóng
                cursor.close();
            }
        }
    }
}