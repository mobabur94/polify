package com.mobabur94.polify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

public class ActivityPolify extends Activity implements View.OnClickListener {

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e("ActivityPolify", "Failed to load OpenCV");
        }
    }

    Button choosePhoto;
    int PHOTO_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // use the main layout
        setContentView(R.layout.activity_polify);

        // save the button in the layout and set the click listener
        choosePhoto = (Button) findViewById(R.id.choose_photo);
        choosePhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.choose_photo) {
            // get an intent to choose a photo and then start it
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, PHOTO_REQUEST);
            }
            Toast.makeText(getApplicationContext(), "Choose a photo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PHOTO_REQUEST) {
                // get the edit intent, attach the result photo uri, and then start it
                Intent editIntent = new Intent(getApplicationContext(), ActivityEdit.class);
                editIntent.putExtra("photoUri", data.getData());
                startActivity(editIntent);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
