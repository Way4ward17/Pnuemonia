package com.wayvytech.pneumonia;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wayvytech.pneumonia.R;
import com.wayvytech.pneumonia.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Formatter;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView result, level, details, right, fact;
    ImageView clickhere, dogImage;
    int imageSize = 256;
    Random r;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        r = new Random();
        result = findViewById(R.id.result);
        fact = findViewById(R.id.fact);
        level = findViewById(R.id.confidenceLevel);
        clickhere = findViewById(R.id.clickhere);
        dogImage = findViewById(R.id.dogImage);
        details = findViewById(R.id.detail);
        right = findViewById(R.id.right);
        imageView = findViewById(R.id.imageView);
        Random r = new Random();
        int randomInt = r.nextInt(5) + 1;
        getFact(randomInt);


    }


    public void classifyImage(Bitmap image) {
        try {
            // Initialize the model
            Model model = Model.newInstance(getApplicationContext());
            // Create inputs for reference
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            // Resize the image if necessary
            Bitmap resizedImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, true);

            int[] intValues = new int[imageSize * imageSize];
            resizedImage.getPixels(intValues, 0, resizedImage.getWidth(), 0, 0, resizedImage.getWidth(), resizedImage.getHeight());

            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255)); // R
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255));  // G
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255));         // B
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Run model inference and get results
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            //float[] confidences = outputFeature0.getFloatArray();
            float[] results = outputFeature0.getFloatArray();
            String classificationResult = interpretResult(results);
            result.setText(classificationResult);

            Random r = new Random();
            int randomInt = r.nextInt(13) + 1;
            getFact(randomInt);
            model.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String interpretResult(float[] results) {
        // Handle invalid output cases
        if (results.length < 1) {
            return "Invalid model output";
        }

        // Handle binary classification outputs
        if (results.length == 1) {
            float pneumoniaProb = results[0];
            float normalProb = 1 - pneumoniaProb; // Complementary probability
            return pneumoniaProb > 0.5
                    ? String.format("Prediction: Pneumonia (%.2f%%)", pneumoniaProb * 100)
                    : String.format("Prediction: Normal (%.2f%%)", normalProb * 100);
        } else if (results.length == 2) {
            float normalProb = results[0];
            float pneumoniaProb = results[1];
            return normalProb > pneumoniaProb
                    ? String.format("Prediction: Normal (%.2f%%)", normalProb * 100)
                    : String.format("Prediction: Pneumonia (%.2f%%)", pneumoniaProb * 100);
        }

        return "Invalid model output structure";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == 3){
                Bitmap image = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView.setImageBitmap(image);
                dogImage.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }else{
                Uri dat = data.getData();
                Bitmap image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(image);
                dogImage.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void openGallery(View view) {
        clear();
        clickhere.setVisibility(View.GONE);
        Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(cameraIntent, 1);
    }

    public void openCamera(View view) {
        clear();
        clickhere.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 3);
            } else {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 100);
            }
        }
    }


    public void openResult(View view) {
        Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
        intent.putExtra("dog_name", result.getText().toString());
        startActivity(intent);
    }


    private void getFact(int i){
        if(i == 1){
            fact.setText("Pneumonia is an infection of the lung");
        }else if(i == 2){
            fact.setText("Pneumonia is the world’s leading cause of death among " +
                    "children under 5 years of age, accounting for 16% of all " +
                    "deaths of children under 5 years old killing approximately " +
                    "2,400 children a day in 2015");
        }else if(i == 3){
            fact.setText("In the US, pneumonia is less often fatal for children, but " +
                    "it is still a big problem");
        }else if(i == 4){
            fact.setText("For US adults, pneumonia is the most common cause " +
                    "of hospital admissions other than women giving birth");
        }else if(i == 5){
            fact.setText("While young healthy adults have less risk of pneumonia " +
                    "than the age extremes, it is always a threat");
        }else if(i == 6){
            fact.setText("Older people have higher risk of getting pneumonia, " +
                    "and are more likely to die from it if they do");
        }else if(i == 7){
            fact.setText("Pneumonia is the most common cause of sepsis and " +
                    "septic shock, causing 50% of all episodes");
        }else if(i == 8){
            fact.setText("Pneumonia can develop in patients already in the hospital " +
                    "for other reasons");
        }else if(i == 9){
            fact.setText("Pneumonia can be caused by lots of different types of " +
                    "microbes, and no single one is responsible for as many as " +
                    "10% of pneumonia cases");
        }else if(i == 10){
            fact.setText("Vaccines are available for some but not many causes of " +
                    "pneumonia");
        }else if(i == 11){
            fact.setText("Antibiotics can be effective for many of the bacteria " +
                    "that cause pneumonia");
        }else if(i == 12){
            fact.setText("Antibiotic resistance is growing amongst the bacteria " +
                    "that cause pneumonia");
        }else if(i == 13){
            fact.setText("Being on a ventilator raises especially high risk for " +
                    "serious pneumonia");
        }

    }


    private void clear(){
        right.setText("");
        result.setText("");
        imageView.setImageURI(null);
        dogImage.setImageURI(null);
        level.setText("");
        details.setText("");
    }
}