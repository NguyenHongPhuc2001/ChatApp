package hcmute.edu.vn.zalo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.edu.vn.zalo.model.User;

public class Activity_UserProfile extends AppCompatActivity {
    // khai báo các biến sử dụng
    private EditText edtUsername, edtUserPhone, edtUserAge, edtUserSex, edtUserDoB; //các edittext để điền thông tin user
    private ImageView imgUserCover; //ảnh bìa user
    private CircleImageView imgAvatar;  //ảnh nền của user
    private ImageButton imgCalendar, imgBack;   //nút chọn ngày từ lịch, nút trở về activity trước đó
    private Button btnUpdate;   //update thông tin lên datbase
    private String ImageUser, ImageCover, uPhone, pass, Status; //string chứa thông tin của user + hình ảnh sau khi mã hóa
    private AlertDialog dialog; //dialog để hiển thị khi đang trong quá trình update

    //khởi tạo đường dẫn đến database
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference ref = db.child("User");       //chỉ đường dẫn đến nút User

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set layout khi activity start là page_userinfor
        setContentView(R.layout.page_userinfor);

        //get dữ liệu được truyền từ activity trước đó
        uPhone = getIntent().getExtras().getString("phone");
        pass = getIntent().getExtras().getString("pass");


        //ánh xạ các biến sử dụng đến thành phần trong layout được set ban đầu
        edtUsername = findViewById(R.id.edittext_PageUserInfor_Username);
        edtUserAge = findViewById(R.id.edittext_PageUserInfor_Age);
        edtUserPhone = findViewById(R.id.edittext_PageUserInfor_Phone);
        edtUserSex = findViewById(R.id.edittext_PageUserInfor_Sex);
        edtUserDoB = findViewById(R.id.edittext_PageUserInfor_DoB);
        imgUserCover = findViewById(R.id.img_PageUserInfor_CoverImage);
        imgAvatar = findViewById(R.id.circleimg_PageUserInfor_Avatar);
        imgCalendar = findViewById(R.id.imgbutton_PageUserInfor_Calendar);
        imgBack = findViewById(R.id.imgbutton_PageUserInfor_Back);
        btnUpdate = findViewById(R.id.button_PageUserInfor_Update);


        //khơi tạo một biến calendar mới để get ngày tháng năm
        Calendar calendar = Calendar.getInstance();
        //dùng method get để lấy ra  ngày tháng năm tương từ calendar
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        //gọi hàm setData() để binding dữ liệu từ database đến các edittext
        setData();

        //khi click vào hình ảnh calendar thì hiện hộp thoại để chọn ngày tháng năm
        imgCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //khởi tạo hộp thoại chọn ngày tháng năm truyền vào activity đang đứng, sự kiện ondataset và 3 biến ngày tháng năm
                DatePickerDialog datePickerDialog = new DatePickerDialog(Activity_UserProfile.this,
                        new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month +1; //month + 1 vì nếu không cộng thì mặc định khi chọn tháng sẽ bị lùi một tháng
                        String date = day + "/" + month + "/" + year;//khởi tạo biến date kiểu string để lưu ngày tháng năm sau khi chọn trên hộp thoai
                        edtUserDoB.setText(date);   //sau đó set biến date cho edtuserDoB
                    }
                }, year, month, day);
                datePickerDialog.show(); //dùng method show() dể hiển thị dialog lên màn hình
            }
        });

        //khi click vào hình ảnh nút back sẽ trở về activity vừa đứng trước đó
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //gọi hàm onBackPressed để trở về activity trước đó
                onBackPressed();
            }
        });


        //khi click vào ảnh đại diện sẽ cho user chọn ảnh để thay ảnh cũ
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check xem đã có quyền truy cập vào thư viện hay chưa, nếu chưa thì thực hiện gửi request xin cấp quyền
                if (ContextCompat.checkSelfPermission(
                        Activity_UserProfile.this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Activity_UserProfile.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {//nếu đã có quyền thì gọi hàm selectAvatar để chọn hình ảnh từ gallery.
                    selectAvatar();
                }
            }
        });

        //khi click vào ảnh bài sẽ cho user chọn ảnh để thay ảnh cũ
        imgUserCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check xem đã có quyền truy cập vào thư viện hay chưa, nếu chưa thì thực hiện gửi request xin cấp quyền
                if (ContextCompat.checkSelfPermission(
                        Activity_UserProfile.this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Activity_UserProfile.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                } else {//nếu đã có quyền thì gọi hàm selectCover để chọn hình ảnh từ gallery.
                    selectCover();
                }
            }
        });

        //khi click button update sẽ thực hiện cập nhật lại dữ liệu lên database theo dữ liệu mởi đã được chọn bởi người dùng
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tạo biến check để khi lỗi thì không thực hiện gì cả
                int check = 0;

                //khởi tạo các biến giá trị và lấy giá trị từ các edittext
                String phone = edtUserPhone.getText().toString();
                String name = edtUsername.getText().toString();
                //ảnh đại diện và ảnh bìa sẽ được gán bằng giá trị của 2 biến được khai báo từ đầu
                String img = ImageUser;
                String cover = ImageCover;
                String age = edtUserAge.getText().toString();
                String DoB = edtUserDoB.getText().toString();
                String sex = edtUserSex.getText().toString();
                String status = "online";

                //nếu phone không có giá trị thì seterror và rasie lên message ngay edtUserPhone
                if (phone.isEmpty()) {
                    edtUserPhone.setError("Vui lòng nhập số điện thoại !");
                    edtUserPhone.requestFocus(); //đặt con trỏ trở lại ngay edtUserPhone
                    check = 1; // gán giá trị biến check = 1 để khi kiểm tra không thể thực hiện tiếp
                }

                //nếu name không có giá trị thì seterror và rasie lên message ngay edtUsername
                if (name.isEmpty()) {
                    edtUsername.setError("Vui lòng nhập tên !");
                    edtUsername.requestFocus();//đặt con trỏ trở lại ngay edtUsername
                    check = 1;// gán giá trị biến check = 1 để khi kiểm tra không thể thực hiện tiếp
                }

                //nếu age không có giá trị thì seterror và rasie lên message ngay edtUserAge
                if (age.isEmpty()) {
                    edtUserAge.setError("Vui lòng điền tuổi !");
                    edtUserAge.requestFocus();//đặt con trỏ trở lại ngay edtUserAge
                    check = 1;// gán giá trị biến check = 1 để khi kiểm tra không thể thực hiện tiếp
                }

                //nếu age có giá trị nhỏ hơn hoặc bằng 0 thì seterror và rasie lên message ngay edtUserAge
                if (Integer.parseInt(age) <= 0) {
                    edtUserAge.setError("Tuổi không hợp lệ !");
                    edtUserAge.requestFocus(); //đặt con trỏ trở lại ngay edtUserAge
                    check = 1;// gán giá trị biến check = 1 để khi kiểm tra không thể thực hiện tiếp
                }

                //nếu DoB không có giá trị thì seterror và rasie lên message ngay edtUserDoB
                if (DoB.isEmpty()) {
                    edtUserDoB.setError("Vui lòng nhập năm sinh !");
                    edtUserDoB.requestFocus(); //đặt con trỏ trở lại ngay edtUserDoB
                    check = 1; // gán giá trị biến check = 1 để khi kiểm tra không thể thực hiện tiếp
                }

                //nếu sex không có giá trị thì seterror và rasie lên message ngay edtUserSex
                if (sex.isEmpty()) {
                    edtUserSex.setError("Vui lòng điền giới tính !");
                    edtUserSex.requestFocus();//đặt con trỏ trở lại ngay edtUserSex
                    check = 1;// gán giá trị biến check = 1 để khi kiểm tra không thể thực hiện tiếp
                }

                //nếu độ dài của phone không phải bằng 10 thì seterror và rasie lên message ngay edtUserPhone
                if (phone.length() < 10 || phone.length() > 10) {
                    edtUserPhone.setError("Số điện thoại không chính xác !");
                    edtUserPhone.requestFocus();//đặt con trỏ trở lại ngay edtUserPhone
                    check = 1;// gán giá trị biến check = 1 để khi kiểm tra không thể thực hiện tiếp
                }

                //kiểm tra biến check để quyết định có thực hiện tiếp hay không
                if (check == 0) {
                    //khi check = 0 => không có lỗi thì hiển thị dialog chờ để xử lý dữ liệu
                    DialogProcessing();
                    //khởi tạo User bằng giá trị từ các edittext
                    User user = new User(phone, pass, name, img, cover, Integer.parseInt(age), DoB, sex, status);

                    //khởi tạo new eventListener để thực hiện update
                    ValueEventListener valueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ref.child(uPhone).setValue(user);//từ đường dẫn tới nút User sử dụng child() để đi đến nút của user và dùng
                            //setValue để cập nhật lại dữ liệu
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };
                    //từ đường dẫn tới nút User sừ dụng child() để chỉ đến nút User và add các method để lắng nghe sự thay đổi của dữ liệu
                    //để load lại khi trở về các activity khác
                    ref.child(uPhone).addListenerForSingleValueEvent(valueEventListener);
                    ref.child(uPhone).addValueEventListener(valueEventListener);
                    //sau khi lắng nghe xong thì xóa event đó đi
                    ref.child(uPhone).removeEventListener(valueEventListener);
                    //tắt dialog
                    dialog.cancel();
                    //thông báo thành công
                    Toast.makeText(Activity_UserProfile.this, "Update thông tin thành công !", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    //hàm selectAvatar() đùng để lựa chọn ảnh đại diện từ gallery
    private void selectAvatar() {
        //khởi tạo intent mới đến màn hình gallery
        //Intent.ACTION_PICK: chọn một mục từ storage và trả về đường dẫn đến mục đó
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //kiểm tra xem kết quả có trả về đường dẫn hay không
        if (intent.resolveActivity(Activity_UserProfile.this.getPackageManager()) != null) {
            //nếu có thì dùng hàm startActivityForResult truyền vào biến intent và requestCode để thực hiện truyền dữ liệu đến onActivityResult để xử lý
            startActivityForResult(intent, 1);
        }
    }


    //hàm selectCover() đùng để lựa chọn ảnh bìa từ gallery
    private void selectCover() {
        //khởi tạo intent mới đến màn hình gallery
        //Intent.ACTION_PICK: chọn một mục từ storage và trả về đường dẫn đến mục đó
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //kiểm tra xem kết quả có trả về đường dẫn hay không
        if (intent.resolveActivity(Activity_UserProfile.this.getPackageManager()) != null) {
            //nếu có thì dùng hàm startActivityForResult truyền vào biến intent và requestCode để thực hiện truyền dữ liệu đến onActivityResult để xử lý
            startActivityForResult(intent, 2);
        }
    }


    //hàm onRequestPermissionResult dùng để xử lý khi có request từ app
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //kiểm tra requestCode và grandResult  (kết quả cấp quyền) nếu > 0 => thực hiện kiểm tra đó là quyền gì
        if (requestCode == 1 && grantResults.length > 0) {
            //nếu grantResults ở vị trí 0 = PERMISSION_GRANTED => cho phép thực hiện
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //gọi tới hàm selectAvatar() để thực hiện chọn avatar
                selectAvatar();
            } else {//nếu không thì thông báo Permission Denied !
                Toast.makeText(Activity_UserProfile.this, "Permission Denied !", Toast.LENGTH_SHORT).show();
            }
        } else {// nếu requestCode không bằng 1 thì thực hiện kiểm tra PERMISSION_GRANTED
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {// nếu  = PERMISSION_GRANTED => cho phép truy cập
                selectCover();//gọi hàm selectCover() thực hiện chọn ảnh bìa
            } else {//nếu không thì thông báo Permission Denied !
                Toast.makeText(Activity_UserProfile.this, "Permission Denied !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //hàm onActivityResult sẽ xử lý khi chọn ảnh từ gallery
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //kiểm tra requestCode để xem là chọn từ cho ảnh đại diện hay ảnh bìa và kiểm tra resultCode có bằng RESULT_OK không
        //nếu bằng thì thực hiện tiếp
        if (requestCode == 1 && resultCode == RESULT_OK) {
            //kiểm tra data có rỗng hay không
            if (data != null) {
                //nếu không khởi tạo biến Uri để get đường dẫn tới mục đã chọn
                Uri selectedImage = data.getData();
                //kiểm tra đường dẫn có rỗng hay không
                if (selectedImage != null) {
                    //nếu không thực hiện get Bitmap của hình ảnh và conver về dạng String
                    try {
                        //khởi tạo InputStream để lấy dường dẫn đến mục đó
                        InputStream inputStream = Activity_UserProfile.this.getContentResolver().openInputStream(selectedImage);
                        //Khởi tạo biến Bitmap và dùng BitmapFactory để decode
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        //khởi tạo ByteArrayOutputStream để đưa Bitmap về byte[]
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        //sử dụng hàm compress truyền vào tpye format, chất lượng và ByteArrayOutputStream để chứa kết quả sau khi compress
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        // khởi tạo biến byte[] dùng method toByteArray() để đưa về dạng byte[]
                        byte[] byteArray = stream.toByteArray();
                        //tiếp tục dùng Base64.encodeToString để chuyển đổi từ btye[] sang String, truyền vào byte[] muốn encode, độ lại của byte[] và flag để kiểm soát đầu ra
                        ImageUser = Base64.encodeToString(byteArray, 0, byteArray.length, 0);
                        //dùng setImageBitmap để set hình ảnh cho ảnh đại diện
                        imgAvatar.setImageBitmap(bitmap);
                    } catch (Exception er) {
                        Toast.makeText(Activity_UserProfile.this, er.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else {// nếu requestCode != 1 => chọn ảnh cho ảnh bìa
            //nếu data không rỗng
            if (data != null) {
                //nếu không khởi tạo biến Uri để get đường dẫn tới mục đã chọn
                Uri selectedImage = data.getData();
                //kiểm tra đường dẫn có rỗng hay không
                if (selectedImage != null) {
                    try {
                        //khởi tạo InputStream để lấy dường dẫn đến mục đó
                        InputStream inputStream = Activity_UserProfile.this.getContentResolver().openInputStream(selectedImage);
                        //Khởi tạo biến Bitmap và dùng BitmapFactory để decode
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        //khởi tạo ByteArrayOutputStream để đưa Bitmap về byte[]
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        //sử dụng hàm compress truyền vào tpye format, chất lượng và ByteArrayOutputStream để chứa kết quả sau khi compress
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        // khởi tạo biến byte[] dùng method toByteArray() để đưa về dạng byte[]
                        byte[] byteArray = stream.toByteArray();
                        //tiếp tục dùng Base64.encodeToString để chuyển đổi từ btye[] sang String, truyền vào byte[] muốn encode, độ lại của byte[] và flag để kiểm soát đầu ra
                        ImageCover = Base64.encodeToString(byteArray, 0, byteArray.length, 0);
                        //dùng setImageBitmap để set hình ảnh cho ảnh bìa
                        imgUserCover.setImageBitmap(bitmap);
                    } catch (Exception er) {
                        Toast.makeText(Activity_UserProfile.this, er.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    //khởi tạo hàm setData() để tự động binding dữ liệu từ database vào các edittext
    private void setData() {
        //khởi tạo một eventListener mới để thực hiện binding
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //khởi tạo User mới để chưa dữ liệu từ database
                User user = snapshot.getValue(User.class);
                if (user != null) {// nếu có tồn tại
                    //thực hiện gán giá trị cho edittext theo đúng giá trị có trong user
                    edtUserPhone.setText(user.getUserPhone());
                    edtUsername.setText(user.getUserName());
                    edtUserAge.setText(String.valueOf(user.getUserAge()));
                    edtUserSex.setText(user.getUserSex());
                    edtUserDoB.setText(user.getUserDateofBirth());
                    String img = user.getUserImage(); //ảnh đại diện
                    String cover = user.getUserCover(); //ảnh bìa
                    if (img != null) { //nếu ảnh đại diện khác null
                        //dùng Base64.decode để conver ra lai byte[] và dùng BitmapFactory để convert byte[] -> Bitmap
                        byte[] arr = Base64.decode(img, 0);
                        Bitmap bmp = BitmapFactory.decodeByteArray(arr, 0, arr.length);
                        //sau khi có Bitmap set cho image để hiển thị lên màn hình
                        imgAvatar.setImageBitmap(bmp);
                        //gán cho giá trị img cho ImageUser
                        ImageUser = img;
                    } else {
                        //nếu == null thì set ảnh mặc định
                        imgAvatar.setImageResource(R.drawable.upload);
                    }


                    if (cover != null) {//nếu ảnh bài khác null
                        //dùng Base64.decode để conver ra lai byte[] và dùng BitmapFactory để convert byte[] -> Bitmap
                        byte[] arrcover = Base64.decode(cover, 0);
                        Bitmap bmpcover = BitmapFactory.decodeByteArray(arrcover, 0, arrcover.length);
                        //sau khi có Bitmap set cho image để hiển thị lên màn hình
                        imgUserCover.setImageBitmap(bmpcover);
                        //gán cho giá trị img cho ImageCover
                        ImageCover = cover;
                    } else {
                        //nếu == null thì set ảnh mặc định
                        imgUserCover.setImageResource(R.drawable.upload);
                    }

                } else {
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        //từ đường dẫn đến nut User dùng child() để chỉ đến nút user và dùng addListenerForSingleValueEvent() để load lại thông tin
        ref.child(uPhone).addListenerForSingleValueEvent(valueEventListener);
        //sau khi load xong thì xóa event đó đi
        ref.child(uPhone).removeEventListener(valueEventListener);
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
        ref.child(uPhone).updateChildren(hashMap);
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
