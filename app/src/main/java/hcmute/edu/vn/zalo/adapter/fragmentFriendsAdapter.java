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
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import hcmute.edu.vn.zalo.R;
import hcmute.edu.vn.zalo.model.User;


public class fragmentFriendsAdapter extends BaseAdapter {
    //khai báo các biến cần sử dụng
    public Context context;
    public int layout;
    public List<User> lsUser;

    //tạo constructor cho fragmentFriendsAdapter
    public fragmentFriendsAdapter(Context context, int layout, List<User> lsUser) {
        //gán giá trị của các biến được truyền vào constructor cho các biến được khai báo ở class này
        this.context = context;
        this.layout = layout;
        this.lsUser = lsUser;
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
        private ImageView imgFriend;
        private TextView txtFriendName, txtFriendPhone;
        private ListView lsvFriends;
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
            convertView = inflater.inflate(layout,null);
            //từ holder đã khai báo ánh xạ đến các thành phần trong layout đã gọi ra
            holder.imgFriend = convertView.findViewById(R.id.img_dongFriends);
            holder.txtFriendName = convertView.findViewById(R.id.textview_FriendName_dongFriends);
            holder.txtFriendPhone = convertView.findViewById(R.id.textview_FriendPhone_dongFriends);
            holder.lsvFriends = convertView.findViewById(R.id.lsv_fragmentFriend_ListFriends);
            //sau khi đã ánh xạ dùng setTag truyền vào class holder để lưu trạng thái cho những lần chạy sau
            convertView.setTag(holder);
        } else {// nếu view không null nghĩa là không phải chạy lần đầu => dùng getTag để lấy trạng thái đã lưu trước đó
            holder = (ViewHolder) convertView.getTag();
        }

        //khởi tạo biến User bằng cách lấy user từ lsUser được truyền vào trong Constructor
        User user = lsUser.get(position);

        //set dữ liệu cho thành phần trong layout xử lý bằng dữ liệu của user vừa get từ lsAccount
        holder.txtFriendPhone.setText(user.getUserPhone());
        holder.txtFriendName.setText(user.getUserName());


        //nếu hình ảnh của user đã có
        if(user.getUserImage()!=null)
        {
            //dùng Base64.decode convert về byte[]
            byte[] arr = Base64.decode(user.getUserImage(),0);
            //dùng BitmapFactory để conver về Bitmap
            Bitmap bmp = BitmapFactory.decodeByteArray(arr,0,arr.length);
            //set cho thành phần hiển thị hình ảnh
            holder.imgFriend.setImageBitmap(bmp);
        }else{//nếu không có thì set ảnh măc định
            holder.imgFriend.setImageResource(R.drawable.anh_dai_dien);
        }

        //trả về view
        return convertView;
    }
}
