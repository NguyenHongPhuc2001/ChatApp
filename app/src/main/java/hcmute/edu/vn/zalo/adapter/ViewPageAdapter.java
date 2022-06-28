package hcmute.edu.vn.zalo.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import hcmute.edu.vn.zalo.fragment_Chats;
import hcmute.edu.vn.zalo.fragment_Friends;
import hcmute.edu.vn.zalo.fragment_Settings;


public class ViewPageAdapter extends FragmentStatePagerAdapter {
    //ta khởi tạo constructor cho class ViewPagerAdapter
    //có 2 biến thuộc tính cần truyền vào: FragmentManager và behavior
    //FragmentManager để get những fragment con bên trong nó, behavior để set trạng thái của fragment
    public ViewPageAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    //hàm getItem sẽ trả về fragment tại vị trí được chọn, truyền vào biến vị trí
    public Fragment getItem(int position) {
        //sử dụng switch để trả về fragment tương ứng với vị trí
        switch (position) {
            case 0: // nếu vị trí bằng 0 thì trả về fragment_Chats
                return new fragment_Chats();
            case 1: // nếu vị trị bằng 1 thì trả về fragment_Friends
                return new fragment_Friends();
            case 2:// nếu vị trí bằng 2 thì trả về fragment_Settings
                return new fragment_Settings();
            default: // default là vị trí mặc định sẽ trả về khi activity được bắt đầu, ở đây nhóm em trả về fragment_Chats
                return new fragment_Chats();
        }
    }

    @Override
    //hàm getCount sẽ trả về số lượng fragment
    public int getCount() {
        return 3;
    }//ở đây nhóm em sử dụng 3 fragment nên sẽ return về 3

}
