package com.mobabur94.polify;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.mobabur94.polify.util.Polification;

import java.io.InputStream;

public class FragmentTaskManager extends Fragment {

    public interface IPhotoResponse {
        void photoLoaded(Bitmap photo);
        void photoPolified(Bitmap polification);
    }

    static FragmentTaskManager taskManager;

    public static FragmentTaskManager getTaskManager() {
        if (taskManager == null) {
            taskManager = new FragmentTaskManager();
        }
        return taskManager;
    }

    TaskLoadPhoto taskLoadPhoto;
    TaskPolifyPhoto taskPolifyPhoto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // retain this fragment on a state change
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // cancel any running tasks
        if (taskLoadPhoto != null) {
            taskLoadPhoto.cancel(true);
        }
        if (taskPolifyPhoto != null) {
            taskPolifyPhoto.cancel(true);
        }
    }

    public void loadPhoto(Uri uri, Activity activity) {
        taskLoadPhoto = new TaskLoadPhoto(activity);
        taskLoadPhoto.execute(uri);
    }

    public void polify(Bitmap bitmap, int complexity, boolean stroke, Activity activity) {
        taskPolifyPhoto = new TaskPolifyPhoto(complexity, stroke, activity);
        taskPolifyPhoto.execute(bitmap);
    }

    class TaskLoadPhoto extends AsyncTask<Uri, Integer, Bitmap> {

        IPhotoResponse photoHandler;
        ContentResolver resolver;

        int screenPixels;

        public TaskLoadPhoto(Activity activity) {
            // get the photo handler and a content resolver
            this.photoHandler = (IPhotoResponse) activity;
            this.resolver = activity.getContentResolver();

            // get the screen dimensions
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            this.screenPixels = metrics.heightPixels * metrics.widthPixels;
        }

        @Override
        protected Bitmap doInBackground(Uri... params) {
            try {
                // get an input stream for the image
                Uri uri = params[0];
                InputStream inputStream = resolver.openInputStream(uri);

                // process metadata
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                int photoPixels = options.outHeight * options.outWidth;

                // reset input stream
                if (inputStream.markSupported()) {
                    inputStream.reset();
                } else {
                    inputStream = resolver.openInputStream(uri);
                }

                // update options with the optimized sample size
                options.inSampleSize = (int) Math.round(((double) photoPixels) / ((double) screenPixels));
                options.inJustDecodeBounds = false;

                return BitmapFactory.decodeStream(inputStream, null, options);
            } catch (Exception e) {
                // TODO: replace huge catch with something that makes more sense
                Log.e(getClass().getName(), "load photo exception", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            // handle the result photo using the photo handler
            if (photoHandler != null) {
                photoHandler.photoLoaded(bitmap);
            }
        }

    }

    class TaskPolifyPhoto extends AsyncTask<Bitmap, Integer, Bitmap> {

        IPhotoResponse photoHandler;

        int complexity;
        boolean stroke;

        public TaskPolifyPhoto(int complexity, boolean stroke, Activity activity) {
            // get the complexity and stroke
            this.complexity = complexity;
            this.stroke = stroke;

            // get the photo handler
            this.photoHandler = (IPhotoResponse) activity;
        }

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            // get the image
            Bitmap photo = params[0];

            // create the polification
            Polification polification = new Polification(photo, complexity, stroke);

            return polification.getResult();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            // handle the result photo using the photo handler
            if (photoHandler != null) {
                photoHandler.photoPolified(bitmap);
            }
        }

    }

}
