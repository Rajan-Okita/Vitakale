package com.example.vitakale;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class HomePage extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private ImageView imageView;
    private TextView classifiedText;
    private TextView resultText;
    private TextView treatmentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Button takePictureButton = findViewById(R.id.button);
        Button launchGalleryButton = findViewById(R.id.button2);
        imageView = findViewById(R.id.imageView);
        classifiedText = findViewById(R.id.classified);
        resultText = findViewById(R.id.result);
        treatmentText = findViewById(R.id.treatment);

        // Handle "Take Picture" button
        takePictureButton.setOnClickListener(view -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(HomePage.this, "Camera not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle "Launch Gallery" button
        launchGalleryButton.setOnClickListener(view -> {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
        });

        // Handle "Log Out" Button
        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomePage.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(photo);
                if (isLeafImage(photo)) {
                    predictImage(photo);
                } else {
                    resultText.setText("No leaf detected. Please upload a leaf image.");
                    treatmentText.setText("");
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                Uri selectedImageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    imageView.setImageBitmap(bitmap);
                    if (isLeafImage(bitmap)) {
                        predictImage(bitmap);
                    } else {
                        resultText.setText("No leaf detected. Please upload a leaf image.");
                        treatmentText.setText("");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Action canceled", Toast.LENGTH_SHORT).show();
        }
    }

    private void predictImage(Bitmap bitmap) {
        try {
            Interpreter tflite = new Interpreter(loadModelFile());

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
            ByteBuffer inputBuffer = convertBitmapToByteBuffer(resizedBitmap);

            float[][] output = new float[1][3];

            tflite.run(inputBuffer, output);

            int maxIndex = 0;
            for (int i = 1; i < output[0].length; i++) {
                if (output[0][i] > output[0][maxIndex]) {
                    maxIndex = i;
                }
            }

            String[] classLabels = {"Downy Mildew", "Healthy", "Powdery Mildew"};

            String[] treatmentSuggestions = {
                    "Use fungicides containing metalaxyl or phosphorous acid. Improve air circulation by spacing plants.",
                    "No treatment needed. Continue regular care and monitoring.",
                    "Apply sulfur-based fungicides. Remove infected leaves and avoid overhead watering."
            };

            // Update UI with prediction result and treatment suggestion
            resultText.setText(classLabels[maxIndex]);
            resultText.setTextColor(getResources().getColor(android.R.color.black));
            classifiedText.setText("Classified as:");
            treatmentText.setText(treatmentSuggestions[maxIndex]);
            treatmentText.setTextColor(getResources().getColor(android.R.color.black));

        } catch (Exception e) {
            e.printStackTrace();
            resultText.setText("Error during prediction");
            treatmentText.setText("");
        }
    }

    private boolean isLeafImage(Bitmap bitmap) {
        int greenPixels = 0;
        int totalPixels = bitmap.getWidth() * bitmap.getHeight();

        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int pixel = bitmap.getPixel(x, y);
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                if (green > red && green > blue) {
                    greenPixels++;
                }
            }
        }


        return ((float) greenPixels / totalPixels) > 0.1;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 200 * 200 * 3);
        buffer.order(ByteOrder.nativeOrder());

        for (int y = 0; y < 200; y++) {
            for (int x = 0; x < 200; x++) {
                int pixel = bitmap.getPixel(x, y);
                buffer.putFloat(((pixel >> 16) & 0xFF) / 255.0f);
                buffer.putFloat(((pixel >> 8) & 0xFF) / 255.0f);
                buffer.putFloat((pixel & 0xFF) / 255.0f);
            }
        }
        return buffer;
    }

    private ByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd("projectModel.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}