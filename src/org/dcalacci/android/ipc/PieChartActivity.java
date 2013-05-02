/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dcalacci.android.ipc;

import org.dcalacci.android.ipc.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Displays an Android spinner widget backed by data in an array. The
 * array is loaded from the strings.xml resources file.
 */
public class PieChartActivity extends Activity {

    /**
     * Fields to contain the current position and display contents of the spinner
     */
    protected int mPos;
    protected String mSelection;

    /**
     * The name of a properties file that stores the position and
     * selection when the activity is not loaded.
     */
    public static final String PREFERENCES_FILE = "SpinnerPrefs";


    /**
     * Initializes the application and the activity.
     * 1) Sets the view
     * 2) Reads the spinner's backing data from the string resources file
     * 3) Instantiates a callback listener for handling selection from the
     *    spinner
     * Notice that this method includes code that can be uncommented to force
     * tests to fail.
     *
     * This method overrides the default onCreate() method for an Activity.
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        
    }

    @Override
    public void onResume() {

        super.onResume();

    }


    @Override
    public void onPause() {

        super.onPause();

    }
    
}
