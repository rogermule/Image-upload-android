package com.rogermuler.uploadimagetest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.kosalgeek.android.photoutil.CameraPhoto;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageBase64;
import com.kosalgeek.android.photoutil.ImageLoader;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.EachExceptionsHandler;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getName();

    ImageView ivCamera, ivGallery, ivUpload, ivImage;

    CameraPhoto cameraPhoto;
    GalleryPhoto galleryPhoto;

    final int CAMERA_REQUEST = 13323;
    final int GALLERY_REQUEST = 22131;

    String selectedPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        cameraPhoto = new CameraPhoto(getApplicationContext());
        galleryPhoto = new GalleryPhoto(getApplicationContext());

        ivImage = (ImageView)findViewById(R.id.ivImage);
        ivCamera = (ImageView)findViewById(R.id.ivCamera);
        ivGallery = (ImageView)findViewById(R.id.ivGallery);
        ivUpload = (ImageView)findViewById(R.id.ivUpload);

        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    startActivityForResult(cameraPhoto.takePhotoIntent(), CAMERA_REQUEST);
                    cameraPhoto.addToGallery();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(),
                            "Something Wrong while taking photos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ivGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(galleryPhoto.openGalleryIntent(), GALLERY_REQUEST);
            }
        });

        ivUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(selectedPhoto == null || selectedPhoto.equals("")){
                    Toast.makeText(getApplicationContext(), "No Image Selected.", Toast.LENGTH_SHORT).show();
                    return;
                }


                try {
                    Bitmap bitmap = ImageLoader.init().from(selectedPhoto).requestSize(1024, 1024).getBitmap();
                    String encodedImage = ImageBase64.encode(bitmap);
                    Log.d(TAG, encodedImage);

                    HashMap<String, String> postData = new HashMap<String, String>();
                    postData.put("User_Name", "916417951");
                    postData.put("User_Pass", "passben");
                    postData.put("Service", "Upload_User_Pic");
                    postData.put("Param", "[]");
                    postData.put("Country", "68");

                    //postData.put("image", encodedImage);
                    postData.put("FileUpload", encodedImage);

                    PostResponseAsyncTask task = new PostResponseAsyncTask(MainActivity.this, postData, new AsyncResponse() {
                        @Override
                        public void processFinish(String s) {
                            Log.d(TAG, s);
                            if(s.contains("uploaded_success")){
                                Toast.makeText(getApplicationContext(), "Image Uploaded Successfully." + s,
                                        Toast.LENGTH_LONG).show();
                                Log.i("response", s);
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Error while uploading.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    //task.execute("http://" +ip + "/uploadImage/upload.php");

//                    task.execute("http://192.168.0.17/uploadImage/upload.php");
                    task.execute("http://192.168.0.20/DeepLife_Final/public/deep_api");

                    task.setEachExceptionsHandler(new EachExceptionsHandler() {
                        @Override
                        public void handleIOException(IOException e) {
                            Toast.makeText(getApplicationContext(), "Cannot Connect to Server.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleMalformedURLException(MalformedURLException e) {
                            Toast.makeText(getApplicationContext(), "URL Error.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleProtocolException(ProtocolException e) {
                            Toast.makeText(getApplicationContext(), "Protocol Error.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleUnsupportedEncodingException(UnsupportedEncodingException e) {
                            Toast.makeText(getApplicationContext(), "Encoding Error.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });


                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(),
                            "Something Wrong while encoding photos", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == CAMERA_REQUEST){
                String photoPath = cameraPhoto.getPhotoPath();
                selectedPhoto = photoPath;
                Bitmap bitmap = null;
                try {
                    bitmap = ImageLoader.init().from(photoPath).requestSize(512, 512).getBitmap();
                    ivImage.setImageBitmap(getRotatedBitmap(bitmap, 90));
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(),
                            "Something Wrong while loading photos", Toast.LENGTH_SHORT).show();
                }

            }
            else if(requestCode == GALLERY_REQUEST){
                Uri uri = data.getData();

                galleryPhoto.setPhotoUri(uri);
                String photoPath = galleryPhoto.getPath();
                selectedPhoto = photoPath;
                try {
                    Bitmap bitmap = ImageLoader.init().from(photoPath).requestSize(512, 512).getBitmap();
                    ivImage.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(),
                            "Something Wrong while choosing photos", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private Bitmap getRotatedBitmap(Bitmap source, float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap bitmap1 = Bitmap.createBitmap(source,
                0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return bitmap1;
    }
}

