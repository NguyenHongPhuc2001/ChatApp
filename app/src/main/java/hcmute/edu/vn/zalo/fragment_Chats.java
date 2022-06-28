package hcmute.edu.vn.zalo;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Phaser;

import hcmute.edu.vn.zalo.adapter.fragmentChatsAdapter;
import hcmute.edu.vn.zalo.model.Chat;
import hcmute.edu.vn.zalo.model.User;


public class fragment_Chats extends Fragment {
    //khai báo các biến sử dụng
    private fragmentChatsAdapter adapter; //adapter xử lý những người đang chat với tài khoản đăng nhập và hiển thị lên listview
    private ListView lsvChats;  //listview để chứa danh sách những người đã chat
    private List<String> lsIdUser; //List chứa số điện thoại của tất cả các tài khoản là người nhận hoặc người gửi trừ tài khoản đang đăng nhập
    private List<User> lsUser;  //list User chứa những User nhắn tin với tài khoản đang đăng nhập
    private List<Chat> lsChat;  //listchat chứa tất cả các tin nhắn trong database
    private String uPhone, pass;    //uPhone: lưu số điện thoại của tài khoản đang đăng nhập, pass: chưa mật khẩu của tài khoản đang đăng nhập
    private User user;  //user để chứa dữ liệu của tài khoản đang đăng nhập

    //khởi tạo đường dẫn đến database
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference ref = db.child("Chats");  //chỉ đường dẫn tới nút Chats
    private DatabaseReference ref_2 = db.child("User"); //chỉ đường dẫn tới nút User

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //set layout xuất hiện cho fragment_chats.
        View view = inflater.inflate(R.layout.fragment__chats, container, false);

        //get dữ liệu được truyền từ activity trước đó
        //sử dụng hàm getIntent().getExtras() để lấy dữ liệu
        uPhone = getActivity().getIntent().getExtras().getString("phone");
        pass = getActivity().getIntent().getExtras().getString("pass");

        //ánh xạ các biến đến file layout dược set trong fragment_chats
        lsvChats = view.findViewById(R.id.lsv_fragmentChat_ListChats);

        //Khởi tạo ArrayList
        lsIdUser = new ArrayList<>();
        lsUser = new ArrayList<>();
        lsChat = new ArrayList<>();

        //nếu click vào người mình muốn chat sẽ đưa tới Activity_Chatting để chat với người đó
        lsvChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //khởi tạo intent mới để chuyển đến Activity_Chatting, truyền vào 2 tham số: activity đang đứng và activity đích đến
                Intent intent = new Intent(getActivity(), Activity_Chatting.class);
                //khởi tạo User mới, sử dụng hàm getItem() truyền vào vị trí của item trên listview để lấy dữ liệu của item đó và gán vào User mới tạo
                User user = (User) parent.getAdapter().getItem(position);
                //sau khi có dữ liệu của người muốn chat, truyền dữ liệu qua Activity đích đến sử dụng method putExtra() truyền vào 2 tham số: tên biến và giá trị truyền vào
                intent.putExtra("opponet_phone", user.getUserPhone());
                intent.putExtra("opponent_name", user.getUserName());
                intent.putExtra("phone", uPhone);
                intent.putExtra("pass", pass);
                //sử dụng hàm startActivity() truyền vào biến intent để chuyển đến activity mới.
                startActivity(intent);
            }
        });

        //khởi tạo một valueEventListener mới để lấy thông tin của tài khoản đang đăng nhập
        ValueEventListener valueEventListener_2 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //khởi tạo một User mới để chứa dữ liệu từ db
                User u = snapshot.getValue(User.class);
                //gán giá trị từ db cho biến user đã khởi tạo từ trước
                user = u;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        //từ đường dẫn tới nút User dùng method child() để đi đến nút của số điện thoai truyền vào (ở đây chính là tài khoản đang đăng nhập)
        //và add eventListener để thực hiện công việc trong event đó
        ref_2.child(uPhone).addListenerForSingleValueEvent(valueEventListener_2);
        //dùng method addValueListener để liên tục lắng nghe sự thay đổi của database.
        ref_2.child(uPhone).addValueEventListener(valueEventListener_2);

        // khởi tạo 1 valueEventListener mới để thực hiện lấy ra list những số điện thoại nhắn tin với tài khoản đang đăng nhập
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list mỗi khi chạy để không bị trùng lặp dữ liệu
                lsIdUser.clear();
                //khởi tạo biến check để kiểm tra rằng tài khoản đăng nhập có chat với ai hay không
                //nếu không thì không load gì lên cả
                int check = 0;
                //chạy vòng lặp để get tất cả dữ liệu từ nút Chats
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //khởi tạo 1 biến chat để lưu dữ liệu từ trên database
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    //kiểm tra xem người gửi có phải là tài khoản đang đăng nhập hay không
                    //nếu phải thì thực hiên tiếp
                    if (chat.getSender().equals(uPhone)) {
                        //khởi tạo biến count_1 check rằng có người nhắn tin với tài khoản đang đăng nhập
                        int count_1 = 0;
                        //kiểm tra xem danh sách số điện thoai đã có dữ liệu hay chưa
                        if (lsIdUser.size() != 0) {
                            //khởi tạo vòng lặp trong list số điện thoại
                            for (String id : lsIdUser) {
                                //nếu người nhận nằm trong list số điện thoại thì tăng biến count_1 lên 1, có nghĩa là người đó đã có trong lsIdUser rồi không cần add lại nữa
                                if (chat.getReceiver().equals(id)) {
                                    count_1++;
                                }
                            }
                            //nếu count_1 =0 có nghĩa là người đó vẫn chưa được thêm vào trong lsIdUser
                            if (count_1 == 0) {
                                lsIdUser.add(chat.getReceiver());
                            }
                        } else {//nếu lsIdUser = 0 thì add trực tiếp
                            lsIdUser.add(chat.getReceiver());
                        }
                        //gán biến check = 1 vì tài khoản đang đăng nhập có nhắn tin
                        check = 1;
                    }
                    //nếu người nhận là tài khoản đang đăng nhập thì thực hiện tiếp
                    if (chat.getReceiver().equals(uPhone)) {
                        //khởi tạo biến count_2 = 0 để check xem người gửi có trùng với những số điện thoai đã add trước đó hay không
                        int count_2 = 0;
                        //khởi tạo vòng for để check
                        for (String id : lsIdUser) {
                            //nếu người gửi trùng với số điện thoại trong lsIdUser thì tăng biến count lên 1
                            if (chat.getSender().equals(id)) {
                                count_2++;
                            }
                        }
                        //nếu biến count_2 =0 có nghĩa là người gửi vẫn chưa có trong list thì thực hiện thêm vào
                        if (count_2 == 0) {
                            lsIdUser.add(chat.getSender());
                        }
                        //vì tài khoản đang đăng nhập có nhắn tin nên gán giá trị biến check = 1
                        check = 1;
                    }
                }
                //nếu biến check = 1 có nghĩa tài khoản đang đăng nhập có nhắn tin nên gọi đến hàm readChats để load tin nhắn lên
                if (check == 1) {
                    //gọi hàm readChats() để load tin nhắn
                    readChats();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        //từ đường dẫn tới nút Chats add eventListener để thực hiện công việc
        ref.addListenerForSingleValueEvent(valueEventListener);
        //sử dụng hàm addValueEventListener để liên tục lắng nghe sự thay đổi của dữ liệu
        ref.addValueEventListener(valueEventListener);

        //trả về view để hiển thị lên màn hình
        return view;
    }

    //khởi tạo hàm readChats() để load lên những tin nhắn của tài khoản đang đăng nhập
    private void readChats() {
        //khởi tạo eventListener để thực hiện hiển thị những đoạn chat lên màn hình
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear lsUser để tránh dữ liệu bị trùng lặp sau mỗi lần lập
                lsUser.clear();
                //khởi tạo vòng for để kiểm tra hết tất cả User có trong database
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //khởi tạo User mới để chứa dữ liệu của User từ database
                    User user = dataSnapshot.getValue(User.class);
                    //khởi tạo vòng for trong lsIdUser
                    for (String id : lsIdUser) {
                        //nếu user nào trên db có số điện thoại trùng trong lsIdUser thì add user đó vào trong lsUser
                        if (user.getUserPhone().equals(id)) {
                            //truyền vào 1 new User với những thuộc tinh được khai báo trong class User
                            lsUser.add(new User(user.getUserPhone(), pass, user.getUserName(),
                                    user.getUserImage(), user.getUserCover(), user.getUserAge(),
                                    user.getUserDateofBirth(), user.getUserSex(), user.getStatus()));
                        }
                    }
                }
                //từ đường dẫn tới nút Chats sử dụng hàm addListenerForSingleValueEvent để get data từ nút Chats trên database
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear listchat mỗi lần chạy lại để dữ liệu không bị trùng
                        lsChat.clear();
                        //khởi tạo vòng for để add tất cả tin nhắn trong nút Chats vào lsChat
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            //khơi tạo chat mới để chứa dữ liệu từ trên database
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            //dùng method add để add biến chat vừa khởi tạo vào lsChat
                            lsChat.add(chat);
                        }
                        //sau khi có tất cả các listChat thì khởi tạo adapter mới (fragmentChatsAdapter) để xử lý và hiển thị dữ liệu lên màn hình
                        //truyền vào 6 tham số: context, layout hiển thị lên listview, list User, list Chat và user(tài khoản đang đăng nhập)
                        //chi tiết xem trong class fragmentChatsAdapter
                        adapter = new fragmentChatsAdapter(getContext(), R.layout.dong_chat_userchatwith, lsUser, lsChat, user);
                        //sau khi xư lý dữ liệu thì dùng method setAdapter() và truyền vào adapter để hiển thị lên listview
                        lsvChats.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        //từ đường dẫn tới nút User dùng addListenerForSingleValueEvent() truyền vào biến valueEventListener để thực hiện công việc được
        //định nghĩa trong biến valueEventListener
        ref_2.addListenerForSingleValueEvent(valueEventListener);
        //dùng addValueEventListener để liên tục sử thay đổi của dữ liệu và thực hiện công việc trong valueEventListener ngay lập tức
        ref_2.addValueEventListener(valueEventListener);
    }


}