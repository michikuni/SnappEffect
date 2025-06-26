package com.mpcorporation.snapeffect.Filters;

import com.mpcorporation.snapeffect.Model.BottomNavItem;
import com.mpcorporation.snapeffect.R;

import java.util.ArrayList;
import java.util.List;

public class BottomNavItemFactory {
    public static List<BottomNavItem> create(){
        List<BottomNavItem> items = new ArrayList<>();
        items.add(new BottomNavItem(R.drawable.bot_nav_crop, "Cắt"));
//        items.add(new BottomNavItem(R.drawable.bot_nav_blend, "Trộn ảnh"));
        items.add(new BottomNavItem(R.drawable.bot_nav_adjust, "Chỉnh ảnh"));
        items.add(new BottomNavItem(R.drawable.bot_nav_art, "Nghệ thuật"));
        items.add(new BottomNavItem(R.drawable.bot_nav_distor, "Biến dạng"));
//        items.add(new BottomNavItem(R.drawable.bot_nav_blur, "Làm mờ"));
//        items.add(new BottomNavItem(R.drawable.bot_nav_threshold, "Ngưỡng hóa"));
//        items.add(new BottomNavItem(R.drawable.bot_nav_transform, "Biến đổi"));
        return items;
    }
}
