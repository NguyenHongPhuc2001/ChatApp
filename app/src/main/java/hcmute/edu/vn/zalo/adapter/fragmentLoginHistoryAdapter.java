package hcmute.edu.vn.zalo.adapter;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import hcmute.edu.vn.zalo.R;
import hcmute.edu.vn.zalo.model.LoginHistory;

public class fragmentLoginHistoryAdapter extends BaseAdapter {
    //khởi tạo các biến dể tạo constructor
    private Context context;
    private int layout;
    private List<LoginHistory> lsLoginHistory;

    //khởi tạo constructor cho fragmentLoginHistoryAdapter()
    public fragmentLoginHistoryAdapter(Context context, int layout, List<LoginHistory> lsLoginHistory) {
        //gán giá trị của các biến được truyền vào constructor cho các biến được khai báo ở class này
        this.context = context;
        this.layout = layout;
        this.lsLoginHistory = lsLoginHistory;
    }

    //hàm getCount() trả về số lượng của LoginHistory
    @Override
    public int getCount() {
        return lsLoginHistory.size();
    }

    //hàm getItem trả về LoginHistory ở vị trí truyền vào
    @Override
    public Object getItem(int position) {
        return lsLoginHistory.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    //khởi tạo class ViewHolder để khai báo biến cần xử lý trong layout truyền vào listview
    public class ViewHolder {
        private TextView txtDeviceName, txtTime;
    }

    //getView trả về view để set lên listview
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //khai báo biến holder
        ViewHolder holder;
        if (convertView == null) {//nếu view = null nghĩa là chạy lân đầu
            holder = new ViewHolder();//khởi tạo new ViewHolder
            //dùng LayoutInflater get layout cần thao tác
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, null);
            //ánh xạ dến thành phần trong layout vừa inflate
            holder.txtDeviceName = convertView.findViewById(R.id.textview_LoginHistory_DeviceName);
            holder.txtTime = convertView.findViewById(R.id.textview_LoginHistory_Time);
            //lưu trạng thái của view
            convertView.setTag(holder);
        } else {//nếu không phải chạy lần đầu thì dùng getTag để lấy ra trạng thái cuối cùng của view
            holder = (ViewHolder) convertView.getTag();
        }

        //khởi tạo LoginHistory
        LoginHistory loginHistory = lsLoginHistory.get(position);
        //set dư liệu cho các thành phần layout bằng dữ liệu LoginHistory vừa mới get từ lsLoginHistory
        holder.txtDeviceName.setText(loginHistory.getDeviceName());
        holder.txtTime.setText(loginHistory.getTime());

        //trả về view
        return convertView;
    }
}
