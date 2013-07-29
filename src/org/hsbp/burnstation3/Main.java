package org.hsbp.burnstation3;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;

public class Main extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        String[] test = {"foo", "bar", "baz"};
        int[] widgets = {R.id.albums, R.id.tracks, R.id.playlist};
        for (int widget : widgets) {
            ListView lv = (ListView)findViewById(widget);
            lv.setAdapter(new ArrayAdapter<String>(
                        this, android.R.layout.simple_list_item_1, test));
        }
    }
}
