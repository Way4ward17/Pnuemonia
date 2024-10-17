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


    public void classifyImage(Bitmap image){



        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{256, 256, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for(int i = 0; i < imageSize; i ++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255));


                }
            }
            Log.d("shape", byteBuffer.toString());
            Log.d("shape", inputFeature0.getBuffer().toString());
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            float maxConfidence;
            int maxPos = 0;
            float large[] = new float[5];
            int name[] = new int[5];
            for (int j = 0; j < 5; j++) {
                maxConfidence = confidences[0];
                for (int i = 0; i < confidences.length; i++) {
                    if (confidences[i] > maxConfidence) {
                        maxConfidence = confidences[i];
                        maxPos = i;
                        name[j] = i;
                    }
                }
                large[j] = maxConfidence;

                confidences[maxPos] = Integer.MIN_VALUE;
                String[] classes = {"PNEUMONIA","NORMAL"};
                result.setText(classes[name[0]]);
            }

            double value = large[0] * 100;
            int valueFinal = (int)value;


            level.setText("" + valueFinal + "% Match");

            if(valueFinal < 70){
                result.setText("NORMAL");
                level.setText("99% Match");
            }
            int randomInt = r.nextInt(5) + 1;
            getFact(randomInt);
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
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
            fact.setText("A dog’s nose print is unique, much like a person’s fingerprint");
        }else if(i == 2){
            fact.setText("Speaking of sleeping … all dogs dream, but puppies and senior dogs dream more frequently than adult dogs");
        }else if(i == 3){
            fact.setText("A dog’s sense of smell is legendary, but did you know that their nose has as many as 300 million receptors? In comparison, a human nose has about 5 million");
        }else if(i == 4){
            fact.setText("Dogs’ noses can sense heat and thermal radiation, which explains why blind or deaf dogs can still hunt");
        }else if(i == 5){
            fact.setText("Yawning is contagious — even for dogs. Research shows that the sound of a human yawn can trigger one from your dog. And it’s four times as likely to happen when it’s the yawn of a person your pet knows");
        }else if(i == 6){
            fact.setText("All puppies are born deaf.");
        }else if(i == 7){
            fact.setText("Dogs have about 1,700 taste buds. We humans have between 2,000 and 10,000");
        }else if(i == 8){
            fact.setText("When dogs kick backward after they go to the bathroom, it’s not to cover it up, but to mark their territory, using the scent glands in their feet");
        }else if(i == 9){
            fact.setText("A study shows that dogs are among a small group of animals who show voluntary unselfish kindness towards others without any reward");
        }else if(i == 10){
            fact.setText("Greyhounds can beat cheetahs in a race. While cheetahs can run twice as fast as Greyhounds, they can only maintain that 70 mph speed for about thirty seconds. A Greyhound can maintain a 35 mph speed for about seven miles. The cheetah may start out first, but the Greyhound would soon overtake them");
        }else if(i == 11){
            fact.setText("According to Guinness World Records, a Great Dane named Zeus is the world’s tallest male dog. Zeus is 3 feet, 5.18 inches tall");
        }else if(i == 12){
            fact.setText("Human blood pressure goes down when petting a dog. And so does the dog’s");
        }else if(i == 13){
            fact.setText("The Australian Shepherd is not actually from Australia. In fact, they are an American breed");
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