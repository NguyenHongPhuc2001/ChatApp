package hcmute.edu.vn.zalo;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.edu.vn.zalo.model.User;

public class fragment_Settings extends Fragment {
    //khai báo các biến cần thiết
    private CircleImageView imgUser;
    private ConstraintLayout layoutSettingLogout, layoutSettingLoginHistory, layoutSettingUser;
    private TextView txtName, txtPhone;
    private ImageView imgLogout, imgLoginHistory;
    private Button btnLogout, btnLoginHistory;
    private String phone, pass;

    //khai báo đường dẫn tới database
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference ref = db.child("User"); //chỉ đường dẫn tới nút User

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__settings, container, false);

        //Vì fragmentSetting nằm trong ActivityHome nên dùng getIntent().getExtras() để get dữ liệu.
        if (getActivity().getIntent().getExtras().getString("phone") != null) {
            phone = getActivity().getIntent().getExtras().getString("phone");
            pass = getActivity().getIntent().getExtras().getString("pass");
        } else {
            //đối với việc truyền dữ liệu từ activity khác sang fragment thì ta dùng getArguments()
            //cách dùng tương tự getExtras(): truyền key để lấy dữ liệu được truyền vào trong key đó.
            phone = getArguments().getString("phone");
            pass = getArguments().getString("pass");
        }

        //Ánh xạ các biến tới layout phù hợp
        layoutSettingUser = view.findViewById(R.id.layout_fragmentSetting_User);
        layoutSettingLogout = view.findViewById(R.id.layout_fragmentSetting_Logout);
        txtName = view.findViewById(R.id.textview_fragmentSetting_Username);
        txtPhone = view.findViewById(R.id.textview_fragmentSetting_PhoneNumber);
        btnLogout = view.findViewById(R.id.button_fragmentSetting_Logout);
        layoutSettingLoginHistory = view.findViewById(R.id.layout_fragmentSetting_LoginHistory);
        imgUser = view.findViewById(R.id.circleimg_fragmentSetting_UserImage);
        imgLogout = view.findViewById(R.id.img_fragmentSetting_Logout);
        imgLoginHistory = view.findViewById(R.id.img_fragmentSetting_LoginHistory);
        btnLoginHistory = view.findViewById(R.id.button_fragmentSetting_LoginHistory);

        //khi click vào layout noi chứa User thì sẽ đưa đến Activity_UserProfile để cập nhật dữ liệu
        layoutSettingUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Khởi tạo intent và truyền vào context hiện tại đang ở và activity đích.
                Intent intent = new Intent(getActivity(), Activity_UserProfile.class);
                //truyền 2 tham số là userphone và userpass để dùng trong activity đích
                intent.putExtra("phone", phone);
                intent.putExtra("pass", pass);
                //lệnh bắt đầu chuyển activity
                startActivity(intent);
            }
        });

        //khi click vào layout nơi chứa
        layoutSettingLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Khởi tạo một dialog listener để thực hiện nếu người dùng chọn 1 trong 2 button: positive hoặc negative.
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //trong hàm onClick sử dụng switch để get lựa chọn
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE: //nhấp vào button Yes
                                //khởi tạo intent về trang Login
                                Intent intent = new Intent(getActivity(), Activity_Login.class);
                                startActivity(intent);
                                //sau khi thực hiện chuyển về trang Login thì thoát khỏi switch.
                                break;

                            case DialogInterface.BUTTON_NEGATIVE: //nhấn vào button No
                                //tắt dialog yes/no
                                dialog.cancel();
                                //thoát khỏi switch
                                break;
                        }
                    }
                };
                //sử dụng method Builder để tạo dialog ở Activity hiện tại
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                //set text xuất hiện và set text cho 2 button positive & negative
                //dialogClickListener() để lắng nghe xem button nào được click.
                //.show() để hiện dialog.
                builder.setMessage("Bạn có chắc muốn đăng xuất chứ ?").setPositiveButton("Đồng ý", dialogClickListener)
                        .setNegativeButton("Không", dialogClickListener).show();
            }
        });

        //khi click vào layout này sẽ dẫn đến Activity_LoginHistory
        layoutSettingLoginHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //khởi tạo intent với context là activity/fragment đang đứng và Activity_LoginHistory là đích đến
                Intent intent = new Intent(getActivity(), Activity_LoginHistory.class);

                //truyền 2 tham số qua activity đích: có 2 biến: 1 là key - dùng để getvalue ở activity đích
                //                                               2 là value
                intent.putExtra("phone", phone);
                intent.putExtra("pass", pass);
                //thực hiện intent chuyển sang activity mới
                startActivity(intent);
            }
        });

        //khi click vào sẽ thực hiện chức năng đăng xuất
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //khởi tạo 1 dialog listener để thực hiện nếu người dùng chọn button nào (positive hoặc negative)
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE: //nếu là button yes thì trở về trang Login
                                Intent intent = new Intent(getActivity(), Activity_Login.class);
                                startActivity(intent);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE: //nếu là không thì tắt dialog đi
                                dialog.cancel();
                                break;
                        }
                    }
                };
                //khởi tạo builder để xác định activity cần build dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                //set text xuất hiện và text của 2 button
                builder.setMessage("Bạn có chắc muốn đăng xuất chứ ?").setPositiveButton("Đồng ý", dialogClickListener)
                        .setNegativeButton("Không", dialogClickListener).show();

            }
        });

        //khi click vào button này thì sẽ dẫn đến trang LoginHistory
        btnLoginHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //khởi tạo intent dẫn đến trang LoginHistory
                Intent intent = new Intent(getActivity(), Activity_LoginHistory.class);
                //truyền 2 tham số để 1 chút trờ về từ activity đó thì vẫn có dữ liệu để get UserInfo.
                intent.putExtra("phone", phone);
                intent.putExtra("pass", pass);
                //bắt đầu thực hiện intent chuyển qua trang LoginHistory
                startActivity(intent);
            }
        });
        //Khi click vào hình ảnh trong layout Logout cũng sẽ thực hiện chức năng Logout
        imgLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //khởi tạo dialog listener để thực hiện nếu người dùng chọn button nào
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE://đồng ý thì trở về trang Login
                                //Yes button clicked
                                Intent intent = new Intent(getActivity(), Activity_Login.class);
                                startActivity(intent);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE://từ chối thì tắt dialog.
                                //No button clicked
                                dialog.cancel();
                                break;
                        }
                    }
                };
                //khởi tạo builder xác định activity build dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                //set text xuất hiện và text của 2 button
                builder.setMessage("Bạn có chắc muốn đăng xuất chứ ?").setPositiveButton("Đồng ý", dialogClickListener)
                        .setNegativeButton("Không", dialogClickListener).show();
            }
        });

        //khi click vào image trong layout LoginHistory cũng sẽ thực hiện chuyển đến trang LoginHistory
        imgLoginHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Khởi tạo intent  đến trang LoginHistory
                Intent intent = new Intent(getActivity(), Activity_LoginHistory.class);
                //truyền 2 tham số để một chút có thể truyền về để get UserInfo
                intent.putExtra("phone", phone);
                intent.putExtra("pass", pass);
                //bắt đầu intent chuyển đến trang LoginHistory
                startActivity(intent);
            }
        });


        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                txtName.setText(user.getUserName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref.child(phone).addListenerForSingleValueEvent(valueEventListener);
        ref.child(phone).addValueEventListener(valueEventListener);
        ref.child(phone).removeEventListener(valueEventListener);

        setData();
        return view;
    }


    private void setData() {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    txtPhone.setText(user.getUserPhone());
                    txtName.setText(user.getUserName());
                    String img = user.getUserImage();
                    if (img == null) {
                        imgUser.setImageResource(R.drawable.anh_dai_dien);
                    } else {
                        byte[] arr = Base64.decode(img, 0);
                        Bitmap bmp = BitmapFactory.decodeByteArray(arr, 0, arr.length);
                        imgUser.setImageBitmap(bmp);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref.child(phone).addListenerForSingleValueEvent(valueEventListener);
        ref.child(phone).addValueEventListener(valueEventListener);
    }

}