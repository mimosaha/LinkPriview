package com.example.fblinkpreview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.fblinkpreview.libs.Preview;

import java.util.ArrayList;
import java.util.List;


public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.ViewHolder> {

    private Context context;
    private List<Preview> previews;

    // RecyclerView recyclerView;
    public PreviewAdapter(Context context) {
        this.context = context;
        previews = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    public void addItems(Preview preview) {
        previews.add(preview);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Preview preview = previews.get(position);

        List<String> images = preview.getImage();

        if (images != null && images.size() > 0) {
            Glide.with(context)
                    .load(images.get(0))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.previewImage);
        }

        holder.previewLink.setText(preview.getLink());
        holder.previewTitle.setText(preview.getTitle());

    }


    @Override
    public int getItemCount() {
        return previews.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView previewImage;
        public TextView previewLink, previewTitle;


        public ViewHolder(View itemView) {
            super(itemView);
            this.previewImage = itemView.findViewById(R.id.link_image);
            this.previewLink = itemView.findViewById(R.id.link_text);
            previewTitle = itemView.findViewById(R.id.link_title);
        }
    }
}
