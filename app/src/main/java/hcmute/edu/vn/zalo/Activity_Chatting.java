package hcmute.edu.vn.zalo;


import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.BitmapCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.edu.vn.zalo.adapter.MessageAdapter;
import hcmute.edu.vn.zalo.model.Chat;
import hcmute.edu.vn.zalo.model.User;

public class Activity_Chatting extends AppCompatActivity {
    //khai báo các biến sử dụng
    private TextView txtOpponentPhone, isRecording, notRecord, txtHello;
    private CircleImageView imgOpponet;
    private ImageButton btnSend, btnGallery, btnCamera, btnAudio;
    private ImageView btnRecord, btnBack, btnRecordSend, btnRecordCancle;
    private EditText edtMess;
    private MessageAdapter adapter; //xử lý các dạng tin nhắn và hiển thị len recycleview
    private RecyclerView rcvChats; //hiển thị tin nhắn
    private List<Chat> lsChat;  //chứa tin nhắn từ database

    private User uUser, opponent;
    private Dialog dialog, dialog_2;
    private int check_image_message = 0;    //biến kiểm tra xem tin nhắn gửi đi là tin nhắn dạng gì
    private String img_message, uPhone, recordFile, oName, oPhone, uPass, Status;
    private boolean isRecord = false;   //kiểm tra có đang ghi âm hay không
    private MediaRecorder recorder;
    private Chronometer chronometer;

    //tạo đường dẫn tới database
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference ref = db.child("Chats");      //chỉ đường dẫn tới nút Chats
    private DatabaseReference ref_2 = db.child("User");     //chỉ đường dẫn tới nút User

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set layout khi activity bắt đầu
        setContentView(R.layout.page_chat);

        //ánh xạ các biến sử dụng tới layout được set
        rcvChats = findViewById(R.id.rcv_PageChat_ListMessages);
        btnGallery = findViewById(R.id.img_PageChat_Gallery);
        txtOpponentPhone = findViewById(R.id.textview_PageChat_OpponentName);
        btnSend = findViewById(R.id.imageButton_PageChat_Send);
        edtMess = findViewById(R.id.edittext_PageChat_TypeChats);
        btnBack = findViewById(R.id.img_PageChat_BackIcon);
        btnAudio = findViewById(R.id.img_PageChat_Record);
        imgOpponet = findViewById(R.id.circleimg_PageChat_imgOpponent);
        txtHello = findViewById(R.id.textview_PageChat_Hello);

        //khởi tạo LinearLayoutManager truyền vào context đang đứng
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);  //setStackFromEnd để tin nhắn được hiển thị từ dưới lên
        rcvChats.setLayoutManager(linearLayoutManager);// set layout cho recycleview


        //get dữ liệu được gửi từ activity trước đó
        oPhone = getIntent().getExtras().getString("opponet_phone");
        oName = getIntent().getExtras().getString("opponent_name");
        uPhone = getIntent().getExtras().getString("phone");
        uPass = getIntent().getExtras().getString("pass");

        //khởi tạo ValueEventListener để thực hiện set ảnh đại diện của đối phương
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //khởi tạo user chưa thông tin từ database
                User user = snapshot.getValue(User.class);
                //kiểm tra đối phương có ảnh đại diện hay không
                if (user.getUserImage() != null) {
                    //nếu có dùng Base64.decode() để convert về byte[]
                    byte[] arr = Base64.decode(user.getUserImage(), 0);
                    //dùng BitmapFactory truyền vào byte[] vừa convert, sau đó set Bitmap cho image của đối phương
                    Bitmap bmp = BitmapFactory.decodeByteArray(arr, 0, arr.length);
                    imgOpponet.setImageBitmap(bmp);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        //chỉ đường dẫn tới child() của đối phương và add sự kiện valueEventListener để thực hiện
        ref_2.child(oPhone).addListenerForSingleValueEvent(valueEventListener);
        ref_2.child(oPhone).addValueEventListener(valueEventListener); // dùng addValueEventListener để lắng khi dữ liệu thay đổi
        ref_2.child(oPhone).removeEventListener(valueEventListener); //xóa sự kiện sau khi thực hiện xong


        //khi click vào button Gallery cho phép người dùng chọn ảnh từ thư viện
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //gọi tới hàm DialogGalleryOption() để thực hiện chọn ảnh từ thư viện
                DialogGalleryOption();
            }
        });

        //khi click vào button audio sẽ thực hiện ghi âm
        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //kiểm tra xem có quyền truy cập chức năng RECORD hay chưa
                if (!checkRecordPermission()) {
                    //nếu chưa thì gửi request yêu cầu cấp quyền
                    requestRecordPermission();
                }
                //gọi hàm DialogRecordAudio() để thực hiện ghi âm
                DialogRecordAudio();
            }
        });

        //khi click button back thì sẽ trở về activity_home
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //khởi tạo intent truyền vào activity hiện tại và activity_home
                Intent intent = new Intent(Activity_Chatting.this, Activity_Home.class);
                //truyền dữ liệu để sử dụng bên activity_home
                intent.putExtra("phone", uPhone);
                intent.putExtra("pass", uPass);
                //startActivity() để chuyển đến trang activity_home
                startActivity(intent);
            }
        });

        //khi click button send sẽ gửi tin nhắn (tin nhắn dạng text)
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //khởi tạo biến mess chứa tin nhắn nhập từ edittext
                String mess = edtMess.getText().toString();
                //kiểm tra nếu edittext có giá trị và biến kiểm tra loại tin nhắn = 0 (0 là tin nhắn dạng text)
                //thì thực hiện gửi tin nhắn qua cho đối phương và lưu lại trên database
                if (!mess.equals("") && check_image_message == 0) {
                    //khởi tạo ngày mới để lưu thời gian lúc gửi
                    Date date = new Date();
                    //khởi tạo SimpleFormat để đưa thời gian về dạng mà ta muốn
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    //dùng method format() truyền vào ngày muốn format
                    String time = format.format(date);
                    //gọi hàm SendMessage() truyền vào 4 tham số: người gửi, người nhận, tin nhắn và thời gian
                    //hàm SendMessage() sẽ thực hiện lưu tin nhắn lên database cùng với những giá trị của các biến đã được truyền vào
                    SendMessage(uPhone, oPhone, mess, time);
                    //hàm ReadMessage() dùng để load lại tin nhắn và hiển thị lên màn hình ngay sau khi gửi, truyền vào 2 tham số: người gửi và người nhận
                    ReadMessage(uPhone, oPhone);
//                } else if (check_image_message == 1) {
//                    Date date = new Date();
//                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//                    String time = format.format(date);
//                    SendMessage(uPhone, oPhone, img_message, time);
//                    ReadMessage(uPhone, oPhone);
                } else {// nếu edittext rỗng thì thông báo vui lòng nhập tin nhắn muôn gửi
                    Toast.makeText(Activity_Chatting.this, "Vui lòng nhập tin nhắn muốn gửi !", Toast.LENGTH_SHORT).show();
                }
                //sau khi gửi tin nhắn xong thì set edittext về lại rỗng
                edtMess.setText("");
                adapter.notifyDataSetChanged(); //notifyDataSetChanged để lắng nghe khi database có sự thay đổi để thay đổi trên recycleview
            }
        });

        //set tên đối phương từ dữ liệu đã lấy từ activity trước đó
        txtOpponentPhone.setText(oName);
        //gọi hàm ReadMessage() truyền vào 2 tham số là người gửi và người nhận để load lại tin nhắn của 2 người
        ReadMessage(uPhone, oPhone);
    }

    //hàm SendMessage() dùng để lưu tin nhắn lên database
    //truyền vào 4 tham số: người gửi, người nhận, tin nhắn và thời gian gửi.
    private void SendMessage(String sender, String receiver, String message, String messtime) {
        //khởi tạo hashmap để lưu dữ liệu
        HashMap<String, Object> hashMap = new HashMap<>();
        //dùng method put() truyền vào 2 tham số: tên biến và giá trị truyền vào biến
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("time", messtime);
        //kiểm tra biến check_image_message để xem tin nhắn ở dạng nào
        if (check_image_message == 1) {// = 1 nghĩa là dạng hình ảnh
            hashMap.put("isImageMessage", 1); //thêm một giá trị vào hashnmap là biến check_image_message
            ref.push().setValue(hashMap);//dùng hàm push() + setValue để lưu dữ liệu lên database
        } else if (check_image_message == 0) {  //= 0 nghĩa là tin nhắn dạng text
            hashMap.put("isImageMessage", 0);   //thêm một giá trị vào hashnmap là biến check_image_message
            ref.push().setValue(hashMap); //dùng hàm push() + setValue để lưu dữ liệu lên database
        } else {//còn trường hợp còn lại là tin nhắn dạng audio
            hashMap.put("isImageMessage", 2);  //thêm một giá trị vào hashnmap là biến check_image_message
            ref.push().setValue(hashMap);  //dùng hàm push() + setValue để lưu dữ liệu lên database
        }
        check_image_message = 0;// sau khi lưu tin nhắn thì đặt biến check_image_message về = 0 để mặc định tin nhắn sẽ ở dạng chat
        //gọi lại hàm ReadMessage() để load lại tin nhắn
        ReadMessage(sender, receiver);
    }


    //hàm ReadMessage() truyền vào 2 tham số: người gửi và người nhận dùng để hiển thị tin nhắn giữa tài khoản đang đăng nhập và người mà tài khoản đó nhắn tin
    private void ReadMessage(String myid, String user) {
        //khởi tạo arraylist mới dùng để lưu tin nhắn trên database
        lsChat = new ArrayList<>();
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear mỗi lần chạy lại để không bị lặp dữ liệu
                lsChat.clear();
                //khởi tạo vòng for để load hết tất cả tin nhắn
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //khởi tạo biến chat chứa dữ liệu từ database
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    //nếu người nhận hoặc người gửi là tài khoản đăng nhập hay người gửi và người nhận là đối phương thì add vào lsChat
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(user) ||
                            chat.getReceiver().equals(user) && chat.getSender().equals(myid)) {
                        lsChat.add(chat);
                    }
                }
                //nếu lsChat rỗng thì hiển thị lên màn hình câu Hãy gửi lời chào đến đối phương
                if (lsChat.size() == 0) {
                    txtHello.setVisibility(View.VISIBLE);//set visibility cho textview  = VISIBLE để hiển thị lên màn hình
                } else {//nếu đã có nhắn tin rồi thì ẩn textview đó đi
                    txtHello.setVisibility(View.GONE);
                }
                //khởi tạo adapter mới truyền vào 4 tham số: context, lsChat, user và đối tượng nhắn tin
                adapter = new MessageAdapter(Activity_Chatting.this, lsChat, uUser, opponent);
                //sau khi xư lý thì setadapter vào recycle view để hiển thị lên màn hình tin nhắn của 2 người
                rcvChats.setAdapter(adapter);
                adapter.notifyDataSetChanged(); //notifyDataSetChanged để khi có sự thay đổi thì recycle view cũng sẽ thay đổi theo
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        //khởi tạo ValueEventListener_2 để lấy thông tin của người dùng và đối tượng nhắn tin
        ValueEventListener valueEventListener_2 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //khởi tạo User mới để chứa thông tin từ database
                    User user = dataSnapshot.getValue(User.class);
                    if (user.getUserPhone().equals(uPhone)) { //kiểm tra nếu sdt  = người dùng thì gán cho biến uUser
                        uUser = user;
                    }
                    if (user.getUserPhone().equals(oPhone)) { //nếu = đối tượng nhắn tin thì gán cho biến opponent
                        opponent = user;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        //ref_2 là đường dẫn đến nút User để get thông tin user và đối tượng nhắn tin
        ref_2.addListenerForSingleValueEvent(valueEventListener_2); //addListenerForSingleValueEvent để chạy 1 lần qua database
        ref_2.addValueEventListener(valueEventListener_2);  //addValueEventListener liên tục lắng nghe sự thay đổi đến từ database và thực hiện mỗi lần có sự thay đổi
        //ref là đường dẫn đến nút Chats để get lsChat giữa 2 người
        ref.addListenerForSingleValueEvent(valueEventListener); //addListenerForSingleValueEvent để chạy 1 lần qua database
        ref.addValueEventListener(valueEventListener);//addValueEventListener liên tục lắng nghe sự thay đổi đến từ database và thực hiện mỗi lần có sự thay đổi
    }

    //hàm requestStoragePermission() dùng để gửi yêu cầu cấp quyền đến bộ nhớ local của thiết bị
    private void requestStoragePermission() {
        //gọi hàm requestPermissions() truyền vào tên quyền và requestCode để phân biệt quyền nào đang được yêu cầu
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }

    //hàm requestCameraPermission() dùng để gửi yêu cầu cấp quyền đến camera để sử dụng
    private void requestCameraPermission() {
        //gọi hàm requestCameraPermission() để yêu cầu cấp quyền sử dụng camera, requestCode để phân biệt quyền nào đang được yêu cầu
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
    }

    //hàm requestRecordPermission() dùng để gửi yêu cầu cấp quyền đề sử dụng chức năng record
    private void requestRecordPermission() {
        //gọi hàm requestRecordPermission() để yêu cầu cấp quyền record âm thanh, requestCode để phân biệt quyền nào đang được yêu cầu
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
    }


    //khởi tạo hàm checkCameraPermission() để kiểm tra xem quyền đã được cấp hay chưa, chỉ trường hợp cả 2 đều đúng thì return về true còn lại thì return về false
    private boolean checkCameraPermission() {
        //dùng hàm checkSelfPermission() truyền vào context hiện tại và quyền muốn kiểm tra, nếu nằm trong package PERMISSION_GRANTED => true
        //không thì = false
        boolean res1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean res2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return res1 && res2;
    }

    //khởi tạo hàm checkStoragePermission() để kiểm tra xem quyền đã được cấp hay chưa, nếu đã có trong package PERMISSION_GRANTED => true
    private boolean checkStoragePermission() {
        boolean res = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return res;
    }

    //khởi tạo hàm checkRecordPermission() để kiểm tra xem quyền đã được cấp hay chưa, nếu đã có trong package PERMISSION_GRANTED => true
    //nếu chưa => false
    private boolean checkRecordPermission() {
        boolean res = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
        boolean res2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        return res && res2;
    }

    //khởi tào hàm openGalerry() để chọn ảnh từ thư viện
    private void openGalerry() {
        //khởi tạo intent mới với ACTION_PICK để chọn ảnh và trả về đường dẫn tới ảnh đó
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //dùng hàm startActivityForResult() truyền vào intent và requestCode để biết được yêu cầu nào đang được thực thi
        startActivityForResult(intent, 101);
    }

    //khởi tạo hàm openCamera() để chụp hình từ camera và gửi cho đối phương
    private void openCamera() {
        //khởi tạo hàm intent với ACTION_IMAGE_CAPTURE để chụp hình và trả về đường dẫn đến hình ảnh đó
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //kiểm tra kết quả trả vể có tồn tại hay không, nếu có thực hiện gửi request thực thi
        if (intent.resolveActivity(getPackageManager()) != null) {
            //dùng hàm startActivityForResult() truyền vào intent và requestCode để biết được yêu cầu nào đang được thực thi
            startActivityForResult(intent, 102);
        }
    }


    //Hàm onRequestPermissionsResult() dùng để xác định request nào đang được gửi, check xem quyền đó đã được cấp hay chưa
    // và sẽ thực thi công việc phù hợp với request đó
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //kiểm tra requestCode và grantResults, nếu thỏa điều kiện thì thực hiện tiếp
        if (requestCode == 101 && grantResults.length > 0) {
            //nếu grantResults[0] là package PERMISSION_GRANTED có nghĩa là đã cấp quyền cho thiết bị => thực hiện hành động dựa vào requestCode nào đang được gửi
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //gọi tới hàm openCamera()
                openCamera();
            } else {
                //gọi tới hàm openGalerry()
                openGalerry();
            }
        }
    }


    //hàm onActivityResult() dùng để thực hiện yêu cầu tùy thuộc vào request và hiển thị kết quả len activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //kiểm tra requestCode và nếu resultCode = RESULT_OK => thực hiện công việc tùy vào requestCode nào đang được gửi tới
        if (requestCode == 102 && resultCode == RESULT_OK) {//requestCode = 102 nghĩa là chụp hình ảnh và gửi đi
            //khởi tạo Bitmap để get dữ liệu từ data được trả về trong hàm  onActivityResult()
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            //khởi tạo ByteArrayOutputStream
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //sủ dụng conpress để định dạng và gán vào ByteArrayOutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            //dùng method toByteArray() để chuyển hình ảnh về dang byte[]
            byte[] byteArray = stream.toByteArray();
            //dùng Base64.encodeToString() truyền vào bytep[], offset: nơi bắt đâu encode và độ dài của byte[]
            img_message = android.util.Base64.encodeToString(byteArray, 0, byteArray.length, 0);
            //gán cho biến check_image_message = 1 vì đây là dữ liệu dạng hình ảnh
            check_image_message = 1;
            //khởi tạo biến date lấy giá tri ngày tháng năm hiện tại
            Date date = new Date();
            //khởi tạo  SimpleDateFormat để đưa về định dạng mà ta muốn
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            //dùng method format() truyền vào 1 date để dịnh dạng
            String time = format.format(date);
            //sau khi định dạng thực hiện gọi hàm SendMessage() truyền vào 4 tham số: người gửi, người nhận, tin nhắn(hình ảnh sau khi mã hóa)
            // và thời gian hiện tại
            SendMessage(uPhone, oPhone, img_message, time);
            //tắt dialog chọn hình ảnh sau khi thực hiện xong.
            dialog.cancel();
        } else if (requestCode == 101 && resultCode == RESULT_OK) { // nếu requestCode = 101 nghĩa là chọn hình ảnh từ gallery và gửi đi
            //khởi tạo Uri mới để get đường dẫn tới mục được chọn
            Uri link = data.getData();
            //kiểm tra đường dẫn có tồn tại hay không, nếu có thực hiện tiếp các công việc tiếp theo
            if (link != null) {
                try {
                    //khởi tạo InputStream dùng hàm openInputStream() truyền vào đường dẫn tới thư mục để thực hiện get dữ liệu từ đường dẫn đó
                    InputStream inputStream = this.getContentResolver().openInputStream(link);
                    //khởi tạo Bitmap sử dụng BitmapFactory để decode từ file đã mở bằng InputStream
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    //khởi tạo ByteArrayOutputStream chứa kết quả sau khi compress
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    //dùn method compress để định dạnh hình ảnh và gán giá trị vào ByteArrayOutputStream
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    //sau khi có ByteArrayOutputStream dugn2 method  toByteArray() để thực hiện chuyền dạng byte[]
                    byte[] byteArray = stream.toByteArray();
                    //dùng Base64.decode() truyền vào byte[], offset: nội bắt đầu thực hiện mã hóa và độ dài của byte[]
                    img_message = android.util.Base64.encodeToString(byteArray, 0, byteArray.length, 0);
                    //gán giá tri biến check_image_message = 1 vì đây là dữ liệu dạng hình ảnh
                    check_image_message = 1;
                    //khởi tạo date mới để get date hiện tại
                    Date date = new Date();
                    //khởi tạo  SimpleDateFormat để đưa về định dạng mà ta muốn
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    //dùng method format() truyền vào 1 date để dịnh dạng
                    String time = format.format(date);
                    //sau khi định dạng thực hiện gọi hàm SendMessage() truyền vào 4 tham số: người gửi, người nhận, tin nhắn(hình ảnh sau khi mã hóa)
                    // và thời gian hiện tại
                    SendMessage(uPhone, oPhone, img_message, time);
                    //tắt dialog sau khi thực hiện chọn ảnh và gừi thành công
                    dialog.cancel();
                } catch (Exception er) {
                    Toast.makeText(this, er.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {//nếu request không tồn tại thì tắt dialog

            dialog.cancel();
        }
    }

    //khởi tạo hàm DialogGalleryOption() để thực hiện hiển thị option chọn ảnh để gửi (1. chọn từ thư viện, 2. chọn từ camera)
    private void DialogGalleryOption() {
        //khởi tạo một dialog mới, truyền vào activity hiện tại để hiển thị trên activity đó
        dialog = new Dialog(Activity_Chatting.this);
        //set layout hiển thị
        dialog.setContentView(R.layout.dialog_gallery_option);


        //ánh xạ các biến tới layout vừa set
        btnCamera = dialog.findViewById(R.id.img_Camera);
        btnGallery = dialog.findViewById(R.id.img_Gallery);


        //khi click vào button camera sẽ thực hiện mở camera để chụp hình và gửi đi
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //kiểm tra đã cấp quyền hay chưa, nếu chưa thì gửi request cấp quyền
                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {//nếu đã cấp quyền thì gọi tới hàm openCamera()n để mở camera và chọn ảnh gửi
                    openCamera();
                    //gán biến check_image_message = 1 vì tin nhắn gửi đi là dạng ảnh
                    check_image_message = 1;
                }
            }
        });

        //khi click vào button gallery hiển thị ra activity để chọn hình từ thư viện và gửi đi
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //kiểm tra xem có quyền truy cập hay chưa, nếu chưa thì xin cấp quyền
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {//nếu đã có quyền rồi thì gọi tới hàm openGalerry() để thực hiện mở gallery và thực hiện chọn ảnh gửi
                    openGalerry();
                    check_image_message = 1;// gán biến check_image_message = 1 vì đây là dữ liệu dạng ảnh
                }
            }
        });
        //hiển thị dialog để người dùng có thể chọn option mà mình muốn
        dialog.show();
    }

    //khởi tạo hàm DialogRecordAudio() để thực hiện ghi âm và gửi qua cho đối phương
    public void DialogRecordAudio() {
        //khởi tạo dialog mới
        dialog_2 = new Dialog(Activity_Chatting.this);
        //set layout hiển thị trên dialog này
        dialog_2.setContentView(R.layout.dialog_record);

        //ánh xạ các biến tới layout vừa được set ở trên
        btnRecordCancle = dialog_2.findViewById(R.id.img_Record_Cancle);
        btnRecordSend = dialog_2.findViewById(R.id.img_Record_Send);
        isRecording = dialog_2.findViewById(R.id.textview_StatusRecording);
        notRecord = dialog_2.findViewById(R.id.textview_StatusRecord);
        btnRecord = dialog_2.findViewById(R.id.img_ButtonRecrod);
        chronometer = dialog_2.findViewById(R.id.chronometer_RecordLength);

        //khi nhấn button record sẽ thực hiện record và nhấn 1 lần nữa để kết thúc ghi âm
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //kiểm tra biến isRecord để xem có đang ghi âm hay không nếu có thì dừng và đặt biến isRecord = false để khi nhấn 1 lần nữa sẽ rơi
                // vào trương họp chưa ghi âm và bắt đầu
                if (isRecord == true) {
                    isRecord = false;//set biến isRecord = false
                    stopRecording(); //gọi tới hàm stopRecording() để dừng ghi âm lại
                } else {//nếu isRecord = false => thực hiện ghi âm
                    isRecord = true; //set biến isRecord = true
                    startRecording();//gọi tới hàm startRecording() để bắt đầu ghi âm
                }
            }
        });

        //khi click button cancle thì sẽ tắt dialog record
        btnRecordCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecord = false;//gán biến isRecord = false để khi chạy lại hoặc trong khi ghi âm tắt thì vẫn sẽ không bị lỗi
                dialog_2.cancel();//tắt dialog
            }
        });

        //khi click button sendRecord sẽ gửi file âm thanh vừa ghi được qua cho đối phương
        btnRecordSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //khởi tạo biến path có kiểu String để get ra đường dẫn của file âm thanh vừa lưu
                String path = Environment.getExternalStorageDirectory().getPath() + "/Download/record.mp3";
                //khởi tạo thêm 1 biền mess để chứa dữ liệu của file âm thanh sau khi mã hóa
                String mess = "";
                try {
                    //dùng Files truyền vào đường dẫn vừa get được để mã hóa file âm thành vừa ghi được
                    byte[] arr = Files.readAllBytes(Paths.get(path));
                    //sau khi có file âm thanh đã được mã hóa thành byte[] => dùng Base64.decode để convert về String và lưu vào biến mess để lưu lên database
                    mess = Base64.encodeToString(arr, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //gán biến check_image_message = 2 vì đây là file âm thanh
                check_image_message = 2;
                //khởi tạo date mới để lấy thời gian hiện tại
                Date date = new Date();
                //khởi tạo hàm SimpleFormat để dịnh dạng ngày thang năm tùy theo nhu cầu
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                //sử dung method format để format date theo đinh dạng ta muốn
                String time = format.format(date);
                //gọi tới hàm SendMessage() để lưu tin nhắn lên database
                SendMessage(uPhone, oPhone, mess, time);
                //gọi tới hàm ReadMessage() để thực hiện load lại tin nhắn
                ReadMessage(uPhone, oPhone);
                //sau khi thực hiện gửi xong thì đóng dialog
                dialog_2.cancel();
            }
        });
        //show dialog để người dùng có thể thao tác ghi âm hoặc tắt
        dialog_2.show();
    }

    //khởi tạo hàm startRecording() để thực hiện ghi âm
    private void startRecording() {
        //bắt đầu đồng hồ đếm giờ
        chronometer.start();
        //set 2 button sendRecord và cancle khi bắt đầu ghi âm để khi hoàn thành ghi âm có thể gửi hoặc bỏ
        btnRecordSend.setVisibility(View.VISIBLE);
        btnRecordCancle.setVisibility(View.VISIBLE);
        //SystemClock.elapsedRealtime() trả về mili giây
        chronometer.setBase(SystemClock.elapsedRealtime());
        //ẩn textview notRecord
        notRecord.setVisibility(View.GONE);
        //hiện textview isRecording
        isRecording.setVisibility(View.VISIBLE);
        //set lại button thành icon màu khác để thể hiện đang ghi âm
        btnRecord.setImageResource(R.drawable.microphone_icon_2);
        try {
            //khởi tạo biến path để lấy đường dẫn đến thư mục lưu file
            String path = Environment.getExternalStorageDirectory().getPath() +
                    "/Download";
            //đặt tên file
            recordFile = "record.mp3";
            //khởi tạo MediaRecorder() để thực hiện record
            recorder = new MediaRecorder();
            //MediaRecorder.AudioSource.MIC dùng để set dữ liệu nguồn của file đến từ MICROPHONE
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //set output là dạng THREE_GPP
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            //set đường dẫn của file đầu ra (lưu ở thư muc đã khai báo)
            recorder.setOutputFile(path + "/" + recordFile);
            //set type encode là AMR_NB
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            //chuẩn bị cho máy ghi âm bắt đầu thu và mã hóa dữ liệu âm thanh đó
            recorder.prepare();
            //bắt đầu quá trình ghi âm và mã hoa dữ liệu
            recorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //khởi tạo hàm stopRecording() để dừng ghi âm
    private void stopRecording() {
        //cho dừng đồng hồ đếm giờ
        chronometer.stop();
        //ẩn isRecording
        isRecording.setVisibility(View.GONE);
        //hiển thị lại textview notRecord
        notRecord.setVisibility(View.VISIBLE);
        //set hình ảnh của btnRecord về hình chưa ghi âm
        btnRecord.setImageResource(R.drawable.microphone_icon);
        //dừng ghi âm
        recorder.stop();
        //xuất file âm thanh
        recorder.release();
        //khởi tạo lại giá trị của recorder = null
        recorder = null;
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
