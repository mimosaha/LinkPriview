package com.example.fblinkpreview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.fblinkpreview.library.LinkPreviewCallback;
import com.example.fblinkpreview.library.SourceContent;
import com.example.fblinkpreview.library.TextCrawler;
import com.example.fblinkpreview.libs.Preview;
import com.example.fblinkpreview.libs.PreviewCallback;
import com.example.fblinkpreview.libs.PreviewPicker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class PreviewActivity extends AppCompatActivity {

    private PreviewAdapter previewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        final EditText editText = findViewById(R.id.input);

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLinkFromLib(editText.getText().toString());
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        previewAdapter = new PreviewAdapter(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(previewAdapter);
    }

    private void getLinkFromLib(String link) {
        PreviewPicker.getInstance().startPicking(link, new PreviewCallback() {
            @Override
            public void onSuccess(Preview preview) {
                if (previewAdapter != null) {
                    previewAdapter.addItems(preview);
                }
                Log.v("MIMO_SAHA::", "Success: " + preview);
            }

            @Override
            public void onError() {
                Log.v("MIMO_SAHA::", "Error");
            }
        });
    }
}
