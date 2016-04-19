package com.mobvoi.design.demo;

import android.app.Activity;
import android.os.Bundle;

import com.mobvoi.design.demo.fragments.MainFragment;
import com.ticwear.design.demo.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new MainFragment())
                .commitAllowingStateLoss();
    }

}
