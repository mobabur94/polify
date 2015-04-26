package com.mobabur94.polify;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

public class ActivityEdit extends Activity implements FragmentTaskManager.IPhotoResponse, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener, View.OnTouchListener {

    public Intent upIntent;
    public ImageView imageView;
    public ProgressBar progressBar;
    public SeekBar complexityBar;
    public CheckBox strokeToggle;

    public FragmentManager fragmentManager;
    public FragmentTaskManager taskManager;

    public Bitmap original;
    public Bitmap altered;

    public int complexity;
    public boolean stroke;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // use the edit layout
        setContentView(R.layout.activity_edit);

        // set the action bar back button
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // get the views in the layout
        imageView = (ImageView) findViewById(R.id.canvas);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        complexityBar = (SeekBar) findViewById(R.id.complexity);
        strokeToggle = (CheckBox) findViewById(R.id.stroke_toggle);

        // get the calling intent
        upIntent = getIntent();

        // get the uri for the image to edit
        Uri uri = (Uri) ((upIntent.getExtras()).get("photoUri"));

        // get the fragment manager
        fragmentManager = getFragmentManager();

        // start the task manager
        taskManager = FragmentTaskManager.getTaskManager();
        if (fragmentManager.findFragmentByTag("task_manager") == null) {
            fragmentManager.beginTransaction().add(taskManager, "task_manager").commit();
        }

        // load the photo using the task manager
        taskManager.loadPhoto(uri, this);

        // handle runtime configuration change
        if (savedInstanceState != null) {
            // restore the complexity
            complexity = savedInstanceState.getInt("complexity", 4);

            // restore the stroke
            stroke = savedInstanceState.getBoolean("stroke", false);
        } else {
            // default complexity to 4
            complexity = 4;

            // default stroke to false
            stroke = false;
        }

        // initialize the complexity bar (0 -> 9 inclusive)
        complexityBar.setProgress(complexity * 9);
        complexityBar.setMax(81);
        complexityBar.setOnSeekBarChangeListener(this);

        // initialize the stroke toggle
        strokeToggle.setChecked(stroke);
        strokeToggle.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // save the complexity
        outState.putInt("complexity", complexity);

        // save the stroke
        outState.putBoolean("stroke", stroke);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void photoLoaded(Bitmap photo) {
        // save the photo
        original = photo;

        // update the image view
        imageView.setImageBitmap(photo);

        // hide the progress bar
        progressBar.setVisibility(View.INVISIBLE);

        // polify the photo
        polifyPhoto();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // update the complexity (0 -> 9 inclusive)
        complexity = (int) Math.round(progress / 9.0);

        // update the seek bar to the rounded value
        seekBar.setProgress(complexity * 9);

        // polify the photo
        polifyPhoto();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // update the stroke option
        stroke = isChecked;

        // polify the photo
        polifyPhoto();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int id = view.getId();

        if (id == R.id.canvas) {
            int action = motionEvent.getAction();

            if (action == MotionEvent.ACTION_DOWN) {
                // replace with the original photo
                imageView.setImageBitmap(original);

                return true;
            } else if (action == MotionEvent.ACTION_UP) {
                // replace with the altered photo
                imageView.setImageBitmap(altered);

                return true;
            }
        }

        return false;
    }

    void polifyPhoto() {
        // show the progress bar
        progressBar.setVisibility(View.VISIBLE);

        // polify the photo using the task manager
        taskManager.polify(original, complexity, stroke, this);
    }

    @Override
    public void photoPolified(Bitmap polification) {
        // save the polification
        altered = polification;

        // update the image view and set the touch listener
        imageView.setImageBitmap(polification);
        imageView.setOnTouchListener(this);

        // hide the progress bar
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // use a menu to add a save button to the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (shouldUpRecreateTask(upIntent)) {
                // TODO: figure out what to do here
                Toast.makeText(getApplicationContext(), "Hold your horses, pal", Toast.LENGTH_SHORT).show();
            } else {
                // go home if the back button was pressed in the action bar
                navigateUpTo(upIntent);
            }
            return true;
        }

        if (id == R.id.action_save) {
            // TODO: save the altered image to storage
            Toast.makeText(getApplicationContext(), "Polification saved", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

}
