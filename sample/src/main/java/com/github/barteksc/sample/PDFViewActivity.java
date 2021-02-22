/**
 * Copyright 2016 Bartosz Schiller
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.barteksc.sample;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.heaven7.android.pdfium.PDFWriterImpl;
import com.heaven7.android.util2.LauncherIntent;
import com.heaven7.core.util.MainWorker;
import com.heaven7.core.util.Toaster;
import com.heaven7.java.pc.schedulers.Schedulers;
import com.shockwave.pdfium.PdfDocument;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.List;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.options)
public class PDFViewActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {

    private static final String TAG = PDFViewActivity.class.getSimpleName();

    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;

    public static final String SAMPLE_FILE = "sample.pdf";
    //public static final String SAMPLE_FILE = "1.pdf";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    @ViewById
    PDFView pdfView;

    @ViewById(R.id.iv)
    ImageView mIv_iv;

    @NonConfigurationInstance
    Uri uri;

    @NonConfigurationInstance
    Integer pageNumber = 0;

    String pdfFileName;

    @OptionsItem(R.id.pickFile)
    void pickFile() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{WRITE_EXTERNAL_STORAGE},
                    PERMISSION_CODE
            );

            return;
        }

        launchPicker();
    }

    @OptionsItem(R.id.test_sticker)
    void testSticker() {
        new LauncherIntent.Builder()
                .setClass(this, TestStickerViewActivity.class)
                .build()
                .startActivity();
    }

    void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    @AfterViews
    void afterViews() {
        pdfView.setBackgroundColor(Color.LTGRAY);
        if (uri != null) {
            displayFromUri(uri);
        } else {
            displayFromAsset(SAMPLE_FILE);
        }
        setTitle(pdfFileName);

        mIv_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfView.getPageRect();
            }
        });
        // SimpleTouchListener.attach(pdfView, mIv_iv);
    }

    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;
        pdfView.setMaxZoom(200);

        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        final Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                //.scrollHandle(new DefaultScrollHandle(this))
                //.spacing(10) // in dp
                .onPageError(this)
                //.fitEachPage(true)
                .autoSpacing(true)
                .pageFitPolicy(FitPolicy.BOTH)
                .pageFling(true)
                .pageSnap(true)
                /*.onRender(new OnRenderListener() {
                    @Override
                    public void onInitiallyRendered(int nbPages) {
                        MainWorker.postDelay(20, new Runnable() {
                            @Override
                            public void run() {
                                pdfView.fitToWidth(pdfView.getCurrentPage());
                            }
                        });
                    }
                })*/
                .onDraw(new OnDrawListener() {
                    Rect rect = new Rect(0, 0, 100, 100);
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
                        canvas.drawBitmap(bitmap, srcRect, rect , null);
                    }
                })
                .load();
    }

    private void displayFromUri(Uri uri) {
        pdfFileName = getFileName(uri);

        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .onDraw(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
                        canvas.drawBitmap(bitmap, 0, 0 , null);
                    }
                })
                .spacing(10) // in dp
                .onPageError(this)
                .load();
    }

    @OnActivityResult(REQUEST_CODE)
    public void onResult(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            uri = intent.getData();
            displayFromUri(uri);
        }
    }

    @Override
    public void onPageChanged(final int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
        Runnable task = new Runnable() {
            @Override
            public void run() {
                System.out.println(String.format("pdfView.w = %d, size = %.2f",
                        pdfView.getWidth(), pdfView.getPageSize(page).getWidth()));
                pdfView.zoomCenteredTo((pdfView.getWidth()) * 1.0f / pdfView.getPageSize(page).getWidth(),
                        new PointF(pdfView.getWidth() * 1.0f / 2, pdfView.getHeight() * 1.0f / 2));
                pdfView.loadPageByOffset();
                pdfView.performPageSnap();

                //testAddImage(page);
            }
        };
        runOnUiThread(task);
    }

    private void testAddImage(final int page) {
        Schedulers.io().newWorker().schedule(new Runnable() {
            @Override
            public void run() {
                String path = Environment.getExternalStorageDirectory() + "/test1.pdf";
                PDFWriterImpl writer = new PDFWriterImpl(new File(path));

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                Matrix matrix = new Matrix();
                pdfView.getPdfFile().addImage(page, bitmap, matrix);
                pdfView.getPdfFile().savePdf(writer, true);
                Toaster.show(getApplicationContext(), "add image is called");
            }
        });
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        Log.e(TAG, "title = " + meta.getTitle());
        Log.e(TAG, "author = " + meta.getAuthor());
        Log.e(TAG, "subject = " + meta.getSubject());
        Log.e(TAG, "keywords = " + meta.getKeywords());
        Log.e(TAG, "creator = " + meta.getCreator());
        Log.e(TAG, "producer = " + meta.getProducer());
        Log.e(TAG, "creationDate = " + meta.getCreationDate());
        Log.e(TAG, "modDate = " + meta.getModDate());

        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    /**
     * Listener for response to user permission request
     *
     * @param requestCode  Check that permission request code matches
     * @param permissions  Permissions that requested
     * @param grantResults Whether permissions granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchPicker();
            }
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page);
    }
}
