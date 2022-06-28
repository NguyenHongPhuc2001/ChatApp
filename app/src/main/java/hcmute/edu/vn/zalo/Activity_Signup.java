package hcmute.edu.vn.zalo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
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

import hcmute.edu.vn.zalo.model.User;


public class Activity_Signup extends AppCompatActivity {
    //khai báo các biến sử dụng
    private EditText edtPhoneNB, edtPassword, edtConfirm, edtUsername;
    private Button btnSignup, btnLogin;
    private AlertDialog dialog;


    // khởi tạo đường dẫn tới database
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference ref = db.child("User"); //chỉ đường dẫn tới nút User

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set layout khi activity bắt đầu, truyền vào layout muốn hiển thị
        setContentView(R.layout.page_signup);

        // ánh xạ các biến sử dụng đến file layout ở trong hàm setContentView
        btnSignup = findViewById(R.id.button_PageSignup_DangKy);
        btnLogin = findViewById(R.id.button_PageSignup_Dangnhap);
        edtPhoneNB = findViewById(R.id.edittext_PageSignup_Sodienthoai);
        edtPassword = findViewById(R.id.edittext_PageSignup_Matkhau);
        edtConfirm = findViewById(R.id.edittext_PageSignup_Confirm);
        edtUsername = findViewById(R.id.edittext_PageSignup_Username);


        //khi click vào button login thì sẽ chuyển đến trang đăng nhập để người dùng có thể đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //khởi tạo intent mới với 2 tham số truyền vào là activity đang đứng và activity đích đến
                Intent intent = new Intent(Activity_Signup.this, Activity_Login.class);
                //dùng hàm startActivity và truyền vào biến intent vừa khỏi tạo để chuyển đến activity đích
                startActivity(intent);
            }
        });


        //khi nhấn vào button signup thì sẽ thực hiện chức năng đăng ký tài khoản
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //gọi đến hàm Signup() để thực hiện đăng ký
                Singup();
            }
        });
    }

    // khai báo hàm Signup để thực hiện chức năng đăng ký
    private void Singup() {
        //khởi tạo các biến và gán giá trị bằng giá trị của các edittext
        String phone = edtPhoneNB.getText().toString();
        String password = edtPassword.getText().toString();
        String confirm = edtConfirm.getText().toString();
        String username = edtUsername.getText().toString();
        //khởi tạo biến check=0 để kiểm tra điều kiện của các edittext, nếu các biến edittext k thỏa thì không thể đăng ký
        int check = 0;


        //kiểm tra xem edittext có rỗng hay không
        if (phone.isEmpty()) {
            //nếu edtPhoneNB rỗng thì set error tại edtPhoneNB  và hiển thị message trên edtPhoneNB
            edtPhoneNB.setError("Vui lòng nhập số điện thoại !"); // set message hiển thị
            edtPhoneNB.requestFocus(); // dùng method requestFocus() để cho con trỏ quay trở lại edtPhoneNB
            check = 1; // gán biến check = 1
        }
        if (phone.length() < 10 || phone.length() > 10) {
            edtPhoneNB.setError("Số điện thoại không chính xác !"); // set message hiển thị
            edtPhoneNB.requestFocus(); // dùng method requestFocus() để cho con trỏ quay trở lại edtPhoneNB
            check = 1; // gán biến check = 1
        }
        //tương tự với edtPassword, edtConfirm và edtUsername
        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng mật khẩu !");
            edtPassword.requestFocus();
            check = 1;
        }
        if (confirm.isEmpty()) {
            edtConfirm.setError("Vui lòng nhập lại mật khẩu !");
            edtConfirm.requestFocus();
            check = 1;
        }
        if (username.isEmpty()) {
            edtUsername.setError("Vui lòng nhập tên !");
            edtUsername.requestFocus();
            check = 1;
        }

        //kiếm tra độ dài kí tự của edtPassword có nhỏ hơn 6 kí tự hay không
        if (password.length() < 6) {
            //nếu có thì sẽ raise error với message
            edtPassword.setError("Mật khẩu phải có 6 ký tự trở lên !"); //set message hiển thị
            edtPassword.requestFocus(); //cho con trỏ trở về edtPassword
            check = 1; // gán giá trị biến check = 1
        }

        //khởi tạo 1 User mới, truyền vào 9 biến nhưng ở đây vì đăng ký nên chỉ có 3 biến phone, password, username sẽ được lấy giá trị từ các edittext
        //còn biến status sẽ cho mặc định là offline còn những biến còn lại thì sẽ gán giá trị là null và người dùng sẽ cập nhập sau
        User user = new User(phone, password, username, null, null,
                0, null, null, "offline");

        //kiểm tra giá trị trong edtConfirm có giống với edtPassword hay không nếu có thì thực hiện không thì raise error trên edtConfirm
        if (confirm.equals(password)) {
            //kiểm tra biến check có bằng 0 hay không
            //nếu có thì thực hiện tiếp các câu lệnh để đăng ký tài khoản.
            if (check == 0) {
                //gọi đến hàm DialogProcessing(), hàm này sẽ hiện ra Dialog khi đang lưu dữ liệu lên databse
                DialogProcessing();
                //khởi tạo một valueEventLister mới để thực hiện đăng ký
                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //khởi tạo biến check_account để kiểm tra xem số điện thoại đã tồn tại trong database hay chưa
                        int check_account = 0;
                        //khởi tạo vòng lặp để kiểm tra hết tất cả tài khoản có trong database
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            //Khởi tạo một User mới và mỗi lần lặp thì gán giá trị của user trên database vào User vừa khởi tạo
                            User check = dataSnapshot.getValue(User.class);
                            //kiểm tra xem số điện thoại của User lấy từ edittext có trùng với số điện thoại lấy từ database hay không
                            //nếu có gán giá trị biến check_account = 1
                            if (user.getUserPhone().equals(check.getUserPhone())) {
                                //gán giá trị biến check_account = 1
                                check_account = 1;
                            }
                        }
                        //kiểm tra giá trị biến check_account
                        //bằng 0 thì lưu dữ liệu vào database
                        //bằng 1 thì thông báo "số điện thoại này đẵ được đăng ký"
                        if (check_account == 0) {
                            //đường dẫn đã chỉ tới nút User, dùng method child() truyền vào số điện thoại để làm nút con và cũng là khóa chính để nhận diện tài khoản
                            //sau đó dùng method setValue và truyền vào User để lưu thông tin lên database.
                            ref.child(phone).setValue(user);
                            //sau khi lưu thành công thì set những edittext về lại rỗng
                            edtPhoneNB.setText("");
                            edtPassword.setText("");
                            edtConfirm.setText("");
                            edtUsername.setText("");
                            edtPhoneNB.clearFocus();//cho con trỏ quay trở lại edtPhoneNB
                            //thông báo đăng ký thành công
                            Toast.makeText(Activity_Signup.this, "Đăng ký tài khoản thành công !", Toast.LENGTH_SHORT).show();
                            //tắt dialog
                            dialog.cancel();
                        } else {
                            dialog.cancel();
                            //set message hiển thị
                            edtPhoneNB.setError("Số điện thoại này đã được đăng ký !");
                            //cho con trỏ trờ về edtPhoneNB
                            edtPhoneNB.requestFocus();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                //add valueEventLister vào dường dẫn đến database cần xử lý
                ref.addListenerForSingleValueEvent(valueEventListener);
                //xóa valueEventLister sau khi hoàn thành
                ref.removeEventListener(valueEventListener);
            }
        } else {
            //set message hiển thị
            edtConfirm.setError("Mật khẩu không chính xác !");
            //cho con trỏ trở về edtConfirm
            edtConfirm.requestFocus();
        }
    }

    //khởi tạo hàm DialogProcessing() để hiển thị dialog khi thực hiện đăng ký
    private void DialogProcessing() {
        //khởi tạo builder, truyền vào context ở activity đang đứng
        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Signup.this);
        //khởi tạo LayoutInflater
        LayoutInflater inflater = this.getLayoutInflater();
        //set layout để dialog hiển thị, truyền vào 2 tham số: layout và root = null
        //root = null vì ta chỉ ánh xạ tới layout đó chứ không hiển thị ngay lập tức lên activity đang đứng
        builder.setView(inflater.inflate(R.layout.dialog_processing, null));
        //setCancleable = true để có thể tắt dialog bằng cách bấm nút trở về trên điện thoại.
        builder.setCancelable(true);

        //dùng create() để tạo dialog
        dialog = builder.create();
        //dùng show() đẻ hiển thị dialog lên màn hình
        dialog.show();
    }
}
