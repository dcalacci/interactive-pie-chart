package edu.madcourse.circletouch;

import android.app.Activity;
import android.os.Bundle;

public class circletouchActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final circle c = (circle) this.findViewById(R.id.circle);
    }
}
