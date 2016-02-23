package com.ticwear.design.demo;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.ticwear.design.demo.fragments.DialogsFragment;
import com.ticwear.design.demo.fragments.TransitionsFragment;

/**
 * Created by tankery on 1/12/16.
 *
 * Activity for details.
 */
public class DetailsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Fragment detailFragment;
        switch (getIntent().getStringExtra("case")) {
            case "Dialogs":
                detailFragment = new DialogsFragment();
                break;
            case "Widgets":
                detailFragment = null;
                break;
            case "Showcase":
                detailFragment = new TransitionsFragment();
                break;
            default:
                detailFragment = null;
                break;
        }

        if (detailFragment != null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, detailFragment)
                    .commitAllowingStateLoss();
        } else {
            finish();
        }
    }

}
