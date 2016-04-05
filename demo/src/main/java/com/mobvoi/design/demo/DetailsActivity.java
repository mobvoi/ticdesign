package com.mobvoi.design.demo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.mobvoi.design.demo.fragments.DialogsFragment;
import com.mobvoi.design.demo.fragments.ListFragment;
import com.mobvoi.design.demo.fragments.SpecFragment;
import com.mobvoi.design.demo.fragments.TransitionsFragment;
import com.mobvoi.design.demo.fragments.WidgetsFragment;
import com.ticwear.design.demo.R;

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
        int titleRes = getIntent().getIntExtra("case", -1);
        switch (titleRes) {
            case R.string.category_dialog_title:
                detailFragment = new DialogsFragment();
                break;
            case R.string.category_widgets_title:
                detailFragment = new WidgetsFragment();
                break;
            case R.string.category_settings_title:
                // Use a settings activity instead of details activity.
                startActivity(new Intent(this, SettingsActivity.class));
                detailFragment = null;
                break;
            case R.string.category_showcase_title:
                detailFragment = new TransitionsFragment();
                break;
            case R.string.category_spec_title:
                detailFragment = new SpecFragment();
                break;
            default:
                detailFragment = null;
                break;
        }

        if (detailFragment instanceof ListFragment) {
            ((ListFragment) detailFragment).setTitle(getString(titleRes));
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
