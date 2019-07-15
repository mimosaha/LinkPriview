package com.example.fblinkpreview;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.fblinkpreview.library.LinkPreviewCallback;
import com.example.fblinkpreview.library.SourceContent;
import com.example.fblinkpreview.library.TextCrawler;
import com.facebook.share.model.ShareLinkContent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText editText = findViewById(R.id.input);

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*String link = "https://en.wikipedia.org/";
                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(link))
                        .build();
                Log.v("MIMO_SAHA:", "Link: " + content);*/
//                String link = "https://en.wikipedia.org/";
                String link = editText.getText().toString();
                getLinkFromLib(link);
            }
        });

    }

    private void getLinkOp(String url) {
        try {
            Document document = Jsoup.connect(url).userAgent("Mozilla").get();
            Log.v("MIMO_SAHA:", "Link: " + document.location());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getLinkFromLib(String link) {
        new TextCrawler().makePreview(new LinkPreviewCallback() {
            @Override
            public void onPre() {

            }

            @Override
            public void onPos(SourceContent sourceContent, boolean isNull) {

            }
        }, link);
    }
}
