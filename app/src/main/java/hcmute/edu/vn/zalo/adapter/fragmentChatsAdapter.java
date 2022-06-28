package hcmute.edu.vn.zalo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.edu.vn.zalo.R;
import hcmute.edu.vn.zalo.model.Chat;
import hcmute.edu.vn.zalo.model.User;

public class fragmentChatsAdapter extends BaseAdapter {
    //khai báo các biến dùng để khỏi tạo constructor cho class fragmentChatsAdapter
    public Context context; //context dùng để get context hiện tại
    public int layout;  //id của layout muốn hiển thị lên listview
    public List<User> lsUser;   //list User dùng để chứa list những User có nhắn tin với tài khoản đang đăng nhập
    public List<Chat> lsChat;   //list tất cả các tin nhắn để lọc ra tin nhắn của tài khoản đang đăng nhập với user khác
    public User user;   //chưa thông tin của tài khoản đang đăng nhập

    //khởi tạo constructor của fragmentChatsAdapter gồm 5 biến: context, layout, list<User>, list<Chat>, user
    public fragmentChatsAdapter(Context context, int layout, List<User> lsUser, List<Chat> lsChat, User user) {
        //gán giá trị của các biến được truyền vào constructor cho các biến được khai báo ở class này
        this.context = context;
        this.layout = layout;
        this.lsUser = lsUser;
        this.lsChat = lsChat;
        this.user = user;
    }

    //hàm getCount trả về số lượng của User
    @Override
    public int getCount() {
        return lsUser.size();
    }

    //hàm getItem truyền vào vị tri để trả về user ở vị trí đó
    @Override
    public Object getItem(int position) {
        return lsUser.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    //khởi tạo class ViewHolder để khai báo các biến sử dụng trong layout được truyền ở constructor
    public class ViewHolder {
        private CircleImageView imgFriend;
        private ImageView imgStatus;
        private TextView txtFriendName, txtLastMessage;
    }


    //hàm getView trả về view sau khi đã được xử lý để hiển thị lên listview
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //khai báo biến ViewHolder
        ViewHolder holder;
        //nếu View == null có nghĩa là chưa chạy lần đầu thì thực hiện tiếp
        if (convertView == null) {
            //khởi tạo new ViewHolder
            holder = new ViewHolder();
            //khởi tạo 1 inflater mới để set layout sẽ thao tác
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //dùng phương thức inflate truyền vào layout cần dùng và root = null: chỉ gọi ra chứ không hiển thị ngay lập tức
            convertView = inflater.inflate(layout, null);
            //từ holder đã khai báo ánh xạ đến các thành phần trong layout đã gọi ra
            holder.imgFriend = convertView.findViewById(R.id.img_dongChatFriends_ImageFriend);
            holder.txtLastMessage = convertView.findViewById(R.id.textview_dongChatFriends_LastMessage);
            holder.txtFriendName = convertView.findViewById(R.id.textview_dongChatFriends_FriendName);
            holder.imgStatus = convertView.findViewById(R.id.img_dongChatFriends_Status);
            //sau khi đã ánh xạ dùng setTag truyền vào class holder để lưu trạng thái cho những lần chạy sau
            convertView.setTag(holder);
        } else {// nếu view không null nghĩa là không phải chạy lần đầu => dùng getTag để lấy trạng thái đã lưu trước đó
            holder = (ViewHolder) convertView.getTag();
        }

        //khởi tạo biến User bằng cách lấy user từ lsUser được truyền vào trong Constructor
        User u = lsUser.get(position);
        //khởi tạo biến chat mới dể chứa message cuối cùng giữa tài khoản đang đăng nhập và user khác
        Chat lastmess = new Chat();

        //khởi tạo vòng for để chạy hết tất cả các tin nhắn để kiểm tra xem user nào đang nhắn tin với tài khoản đang đăng nhập
        for (Chat chat : lsChat) {
            //nếu tài khoản đang đăng nhập là ngưởi hoặc người nhận và user khác là người gửi hoặc người nhận thì tiến hành lấy message cuối cùng
            if (user.getUserPhone().equals(chat.getReceiver()) && u.getUserPhone().equals(chat.getSender())
                    || user.getUserPhone().equals(chat.getSender()) && u.getUserPhone().equals(chat.getReceiver())) {
                lastmess = chat;
            }
        }

        //từ biến holder gọi đến txtFriend và set tên cho user nhắn tin với tài khoản đang đăng nhập
        holder.txtFriendName.setText(u.getUserName());
        //kiểm tra xem message đó là loại nào
        if (lastmess.getIsImageMessage() == 0) {//bằng 0 nghĩa là dạng text
            //kiểm tra người gửi là ai, nếu là tài khoản đang đăng nhập thì set message tương ứng
            if (lastmess.getSender().equals(user.getUserPhone())) {
                holder.txtLastMessage.setText("Bạn: " + lastmess.getMessage());
            } else {//nếu không phải thì get tên của user đó + với lastmessage
                //khởi tạo mảng string dùng method split() để tách tên của user đó
                String[] arr = u.getUserName().split(" ");
                //lấy độ dài của mảng
                int index = arr.length;
                //lấy ra phần tử cuối cùng trong mảng cũng chính là tên của user và set lên màn hình
                holder.txtLastMessage.setText(arr[index - 1] + ": " + lastmess.getMessage());
            }

        } else if (lastmess.getIsImageMessage() == 1) {//bằng 1 là dạng file hình ảnh
            //kiểm tra người gửi là ai, nếu là tài khoản đang đăng nhập thì set message tương ứng
            if (lastmess.getSender().equals(user.getUserPhone())) {
                //khác với message dạng text đối với hình ảnh chỉ cần hiển thị [Username] + đã gửi 1 file hình ảnh
                holder.txtLastMessage.setText("Bạn đã gửi 1 file hình ảnh");
            } else {//nếu không thì thực hiện tách tên và set vào textview để hiển thị lên màn hình
                String[] arr = u.getUserName().split(" ");
                int index = arr.length;
                holder.txtLastMessage.setText(arr[index - 1] + " đã gửi 1 file hình ảnh");
            }
        } else {//còn lại chính là file dạng âm thanh
            //kiểm tra người gửi là ai, nếu là tài khoản đang đăng nhập thì set message tương ứng
            if (lastmess.getSender().equals(user.getUserPhone())) {
                //đối với file âm thanh cũng chỉ hiển thị đã gửi 1 file âm thanh
                holder.txtLastMessage.setText("Bạn đã gửi 1 file âm thanh");
            } else {//nếu người gửi không phải tài khoản đang đăng nhập thì tách tên và set lên textview
                String[] arr = u.getUserName().split(" ");
                int index = arr.length;
                holder.txtLastMessage.setText(arr[index - 1] + " đã gửi 1 file âm thanh");
            }
        }
        //khởi tạo biến str để get hình ảnh của user nhắn tin với tài khoan đang đăng nhập
        String img = u.getUserImage();
        //nếu có thì thực hiện conver về dạng byte[]
        if (img != null) {
            //sử dụng thư viện Base64 của android.util để chuyển về byte[]
            //sử dụng method decode() truyền vào biến cần convert và flag để kiểm soát cac tính năng của đầu ra
            byte[] arr = Base64.decode(img, 0);
            // khởi tạo biến Bitmap, dùng BitmapFactory để giải mã, truyền vào 3 tham số: byte[], offset: nơi bắt đầu giải mã trong byte[], độ dài của byte[]
            Bitmap bmp = BitmapFactory.decodeByteArray(arr, 0, arr.length);
            //sau khi đưa về Bitmap thì dùng hàm setImageBitmap truyền vào Bitmap vừa giải mã để hiển thị hình ảnh lên imageview
            holder.imgFriend.setImageBitmap(bmp);
        } else {// nếu không có hình thì gán bằng hình mặc định
            holder.imgFriend.setImageResource(R.drawable.anh_dai_dien);
        }

        //khởi tạo vòng for trong list User để set trạng thái on/off
        for (User u_2 : lsUser) {
            //nếu biến status = "online" thì set icon status sang màu xanh (biểu thị online)
            if (u_2.getStatus().equals("online")) {
                holder.imgStatus.setImageResource(R.drawable.status_icon_2);
            } else {//còn không thì set icon status màu xám (biểu thị offline)
                holder.imgStatus.setImageResource(R.drawable.status_icon);
            }
        }

        // trả về biến view để hiển thị lên màn hình
        return convertView;
    }

}
