package hcmute.edu.vn.zalo.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import hcmute.edu.vn.zalo.R;
import hcmute.edu.vn.zalo.model.Chat;
import hcmute.edu.vn.zalo.model.User;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    //khai báo MSG_TYPE_LEFT và MSG_TYPE_RIGHT để xét tin nhắn của người gửi và người nhận để xử lý khi load tin nhắn
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    //khởi tạo MediaPlayer để chạy file âm thanh
    private MediaPlayer mediaPlayer = new MediaPlayer();
    //Handler và Runnable để xử lý seekbar khi chạy file âm thanh
    private Handler handler;
    private Runnable runnable;

    //khai báo các biến để tạo constructor cho MessageAdapter
    private Context context;
    private List<Chat> lsChat;
    private User user, opponent;
    private int check_message_image;

    //tạo constructor cho MessageAdapter
    public MessageAdapter(Context context, List<Chat> lsChat, User user, User opponent) {
        //gán giá trị của các biến được truyền vào constructor cho các biến được khai báo ở class này
        this.context = context;
        this.lsChat = lsChat;
        this.user = user;
        this.opponent = opponent;
    }


    //khởi tạo class ViewHolder để khai báo và xử lý các biến thành phần trong layout cần xử lý để hiển thị lên recycleview
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtShowChat;
        private ImageView imgChats, imgPlayRecord;
        public SeekBar seekbar;
        public Chronometer chronometer;
        public ConstraintLayout layoutRecord;

        //khởi tạo hàm ViewHolder(truyền vào View để ánh xạ đến thành phần trong view đó)
        public ViewHolder(View itemView) {
            super(itemView);
            //ánh xạ các biến tới thành phần trong view
            txtShowChat = itemView.findViewById(R.id.message);
            imgChats = itemView.findViewById(R.id.img_Chats_Image);
            seekbar = itemView.findViewById(R.id.seekbar_chat);
            chronometer = itemView.findViewById(R.id.chronometer_chat);
            imgPlayRecord = itemView.findViewById(R.id.img_playrecord);
            layoutRecord = itemView.findViewById(R.id.layout_chat);
        }

    }


    //onCreateViewHolder() thực hiện set layout sẽ thao tác
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_LEFT) {//nếu bằng MSG_TYPE_LEFT - nghia là tin nhắn bên người nhận
            //thực hiện tạo view truyền vào layout dùng để xử lý hiện tin nhắn bên người nhận
            View view = LayoutInflater.from(context).inflate(R.layout.dong_chat_item_left, parent, false);
            //trả vể view chưa layout để thao tác
            return new MessageAdapter.ViewHolder(view);
        } else {//nếu bằng MSG_TYPE_RIGHT - nghia là tin nhắn bên người gửi
            //thực hiện tạo view truyền vào layout dùng để xử lý hiện tin nhắn bên người gửi
            View view = LayoutInflater.from(context).inflate(R.layout.dong_chat_item_right, parent, false);
            //trả vể view chưa layout để thao tác
            return new MessageAdapter.ViewHolder(view);
        }
    }

    //onBindViewHolder() dùng để binding dữ liệu lên layout để hiển thị lên màn hình
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //khởi tạo 1 chat để chứa dữ liệu từ lsChat
        Chat chat = lsChat.get(position);
        //check thử dạng tin nhắn là gì
        check_message_image = chat.getIsImageMessage();
        if (check_message_image == 0) {//nếu = 0 nghia là tin nhắn dạng text
            //ẩn đi thành phần để show tin nhắn dạng âm thanh và hình ảnh đi, chỉ show thành phần hiển thị tin nhắn dạng text
            holder.imgChats.setVisibility(View.GONE);
            holder.txtShowChat.setVisibility(View.VISIBLE);
            //từ chat get ra message rồi set cho thành phần hiển thị tin nhắn text
            holder.txtShowChat.setText(chat.getMessage());
        } else if (check_message_image == 1) {//= 1 nghĩa là tin nhắn dạng hình ảnh
            //ẩn đi thành phần để show tin nhắn dạng âm thanh và text đi, chỉ show thành phần hiển thị tin nhắn dạng hình ảnh
            holder.imgChats.setVisibility(View.VISIBLE);
            holder.txtShowChat.setVisibility(View.GONE);
            //get dữ liệu hình ảnh từ chat
            String img = chat.getMessage();
            //dùng Base64.decode để chuyển về dạng byte[]
            byte[] arr = Base64.decode(img, 0);
            //dùng BitmapFactory để convert về Bitmap
            Bitmap bmp = BitmapFactory.decodeByteArray(arr, 0, arr.length);
            //set Bitmap vừa convert cho thành phần hiển thị tin nhắn dạng hình ảnh
            holder.imgChats.setImageBitmap(bmp);
        } else {//trường hợp còn lại là tin nhắn dạng âm thanh
            //ẩn đi thành phần để show tin nhắn dạng text và hình ảnh đi, chỉ show thành phần hiển thị tin nhắn dạng âm thanh
            holder.layoutRecord.setVisibility(View.VISIBLE);
            holder.txtShowChat.setVisibility(View.GONE);
            holder.imgChats.setVisibility(View.GONE);
            //tạo sự kiện trên button play trong thành phần hiển thị tin nhắn âm thanh
            holder.imgPlayRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //thay đổi icon -> playing khi nhấn vào button
                    holder.imgPlayRecord.setImageResource(R.drawable.pause_icon);
                    try {
                        //get dữ liệu âm thanh từ chat
                        String mess = chat.getMessage();
                        //dùng Base64.decode để  convert về dạng byte[]
                        byte[] retrive = Base64.decode(mess, 0);
                        //khởi tạo một File mới để lưu dữ liệu vào file này
                        File tempMp3 = File.createTempFile("temp", "mp3");
                        //xóa dữ liệu trong file hiện tại, để khi chèn dữ liệu âm thanh mới vào file thì sẽ không bị lỗi
                        tempMp3.deleteOnExit();
                        //khởi tạo FileOutputStream truyền vào file vừa tạo để cbi ghi dữ liệu vào file
                        FileOutputStream fos = new FileOutputStream(tempMp3);
                        //dùng write để chèn dữ liệu từ message vào file
                        fos.write(retrive);
                        //sau khi xong thì close
                        fos.close();


                        //khởi tạo FileInputStream để tạo file đầu vào  truyền vào file vừa được chèn dữ liệu âm thanh
                        FileInputStream fis = new FileInputStream(tempMp3);
                        mediaPlayer.setDataSource(fis.getFD());//trả về ouput của file

                        //dùng prepare chuẩn bị phat dữ liệu
                        mediaPlayer.prepare();
                        //bắt đầu phát file âm thanh
                        mediaPlayer.start();

                        //set đơn vị la mili giây
                        holder.chronometer.setBase(SystemClock.elapsedRealtime());
                        //khởi động đồng hồ
                        holder.chronometer.start();
                        //gọi sự kiện setOnCompletionListener() để xử lý sau khi file được phát xong.
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                //dừng đồng hồ
                                holder.chronometer.stop();
                                //set icon về icon play
                                holder.imgPlayRecord.setImageResource(R.drawable.play_icon);
                                //độ dài seekbar chỉnh về 0
                                holder.seekbar.setMax(0);
                                //set đơn vị là mili giây
                                holder.chronometer.setBase(SystemClock.elapsedRealtime());
                            }
                        });

                        //set độ dài seekbar thay đồi theo thời lượng của file đang chạy
                        holder.seekbar.setMax(mediaPlayer.getDuration());
                        //khởi tao handler mới
                        handler = new Handler();
                        runnable = new Runnable() {//khởi tạo Runnable mới
                            @Override
                            public void run() {//trong khi chạy
                                //thay dổi độ dài seekbar theo thời vị trí phát hiện tại của file
                                holder.seekbar.setProgress(mediaPlayer.getCurrentPosition());
                                //để delay là 0
                                handler.postDelayed(runnable, 0);
                            }
                        };
                        handler.postDelayed(runnable, 0);//để delay là 0
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    //getItemCount() trả về số lượng tin nhắn
    @Override
    public int getItemCount() {
        return lsChat.size();
    }

    //getItemViewType() truyền vào biến vị trí để kiểm tra xem đó tin nhắn đó là của người gửi hay người nhận.
    @Override
    public int getItemViewType(int position) {
        if (lsChat.get(position).getSender().equals(user.getUserPhone())) {// nếu người nhận trong tin nhắn là tài khoản đang đăng nhập thì trả về biến
            //để chọn layout xử cho tin nhắn dó
            return MSG_TYPE_RIGHT;
        } else {
            //nếu không phải là tài khoản đang đăng nhập thì return về MSG_TYPE_LEFT để xử lý layout
            return MSG_TYPE_LEFT;
        }
    }
}
