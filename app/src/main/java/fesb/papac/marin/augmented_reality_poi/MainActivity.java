package fesb.papac.marin.augmented_reality_poi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ViewMain Content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout ViewPane = (FrameLayout) findViewById(R.id.ar_view_pane);

        DisplayView displayView = new DisplayView(getApplicationContext(), this);
        ViewPane.addView(displayView);

        Content = new ViewMain(getApplicationContext());
        ViewPane.addView(Content);


    }
    @Override
    protected void onPause() {
        Content.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        Content.onResume();
        super.onResume();
    }
}
