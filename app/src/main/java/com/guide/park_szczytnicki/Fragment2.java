package com.guide.park_szczytnicki;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

public class Fragment2 extends Fragment
{
    int[] IMAGES = new int[] {R.drawable.cat_photo_nature, R.drawable.cat_photo_history, R.drawable.cat_photo_culture};
    public Fragment2() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment2, container, false);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        ListView categories = getView().findViewById(R.id.list_view_categories);
    }

    class CategoryItem extends BaseAdapter
    {

        @Override
        public int getCount()
        {   return IMAGES.length;   }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return null;
        }
    }
}
