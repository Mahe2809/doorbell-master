/*
 * Copyright 2016, The Android Open Source Project
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
package com.example.androidthings.doorbell;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //private DoorbellEntryAdapter mAdapter;
    //private FirebaseStorage mFirebaseStorage;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button btnTTS = findViewById(R.id.btnTTS);

        final TextToSpeech tts = new TextToSpeech(this, (TextToSpeech.OnInitListener) this);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("logs/OCRIMAGE");


        final ImageView imageView = findViewById(R.id.imageViewOCRIMAGE);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {

                    String imageURL = snapshot.child("image").getValue().toString();

                    imageURL = imageURL.replaceAll("\\]","");
                    imageURL = imageURL.replaceAll("\\[","");


                    Picasso.with(MainActivity.this)
                            .load(imageURL)
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                                    /* Save the bitmap or do something with it here */

                                    // Set it in the ImageView
                                    imageView.setImageBitmap(bitmap);

                                    ocrTextRecognization(bitmap);

                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.v("Error at onCancelled", databaseError.toString());

            }
        });

        btnTTS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(text.equals("") || text.equals("N/A"))
                {
                    tts.setLanguage(Locale.ENGLISH);

                    tts.speak("NO Text. Error!",TextToSpeech.QUEUE_FLUSH,null,null);
                }
                else
                {
                    tts.setLanguage(Locale.ENGLISH);

                    tts.speak(text,TextToSpeech.QUEUE_FLUSH,null, null);
                }

            }
        });


    }

    public void ocrTextRecognization(Bitmap bitmap)
    {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        /*FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
*/
        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setWidth(480)   // 480x360 is typically sufficient for
                .setHeight(360)  // image recognition
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .build();

        FirebaseVisionDocumentTextRecognizer detector = FirebaseVision.getInstance()
                .getCloudDocumentTextRecognizer();

        final TextView textView = findViewById(R.id.textOCR);

        Log.d("TEXT OCR", "TEXT CHECK - 1");

        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() {
                    @Override
                    public void onSuccess(FirebaseVisionDocumentText result) {
                        // Task completed successfully
                        // ...

                        //String text;

                        for(FirebaseVisionDocumentText.Block block : result.getBlocks())
                        {
                            text = block.getText();
                            textView.setText(text);

                            for (FirebaseVisionDocumentText.Paragraph paragraph : block.getParagraphs())
                            {
                                text = paragraph.getText();
                                textView.setText(text);

                                for (FirebaseVisionDocumentText.Word word : paragraph.getWords())
                                {
                                    text = word.getText();
                                    textView.setText(text);

                                    for (FirebaseVisionDocumentText.Symbol symbol : word.getSymbols())
                                        text = symbol.getText();;
                                        textView.setText(text);

                                }

                            }
                        }



                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                    }
                });




    }

    @Override
    public void onStart() {
        super.onStart();

       /* // Initialize Firebase listeners in adapter
        mAdapter.startListening();

        // Make sure new events are visible
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
            }
        });*/
    }

    @Override
    public void onStop() {
        super.onStop();

        // Tear down Firebase listeners in adapter
        //mAdapter.stopListening();
    }
}
