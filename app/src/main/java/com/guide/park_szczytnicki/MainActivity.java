package com.guide.park_szczytnicki;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity
{
    public static final int[] MENU_BUTTONS = new int[] {R.id.button_map, R.id.button_categories, R.id.button_routs, R.id.button_achievements};
    public static final SparseArray<Fragment> MAP_OF_FRAGMENTS = new SparseArray<>();
    static
    {
        MAP_OF_FRAGMENTS.put(R.id.button_map,            new FragmentMainMap());
        MAP_OF_FRAGMENTS.put(R.id.button_categories,     new Fragment2());
        MAP_OF_FRAGMENTS.put(R.id.button_routs,          new Fragment3());
        MAP_OF_FRAGMENTS.put(R.id.button_achievements,   new Fragment4());
    }

    public View currentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int id : MENU_BUTTONS)
            findViewById(id).setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {    ChangeFragment(view);     }
            });
        currentButton = findViewById(R.id.button_map);
        currentButton.setBackgroundColor(getResources().getColor(R.color.white));
    }

    public void ChangeButton(View view)
    {
        currentButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        currentButton = view;
        currentButton.setBackgroundColor(getResources().getColor(R.color.white));
    }

    public void ChangeFragment(View view)
    {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container, MAP_OF_FRAGMENTS.get(view.getId())).commit();
        ChangeButton(view);
    }
}