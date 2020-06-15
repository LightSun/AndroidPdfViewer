package com.github.barteksc.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.heaven7.core.util.Toaster;


public class TestStickerViewActivity extends AppCompatActivity {

    StickerView mStickerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_test_sticker);

        mStickerView = findViewById(R.id.sticker_view);

        mStickerView.setSticker(R.drawable.ic_launcher);
        mStickerView.setOnClickTextListener(new StickerView.OnClickTextListener() {
            @Override
            public void onClickTextArea(StickerView view) {
                Toaster.show(view.getContext(), "Text is clicked.");
            }
        });
    }
}
