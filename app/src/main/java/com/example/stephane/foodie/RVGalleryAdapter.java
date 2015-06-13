package com.example.stephane.foodie;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.lucasr.twowayview.TwoWayLayoutManager;
import org.lucasr.twowayview.widget.SpannableGridLayoutManager;
import org.lucasr.twowayview.widget.StaggeredGridLayoutManager;
import org.lucasr.twowayview.widget.TwoWayView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stéphane on 07/05/2015.
 */

public class RVGalleryAdapter extends RecyclerView.Adapter<RVGalleryAdapter.SimpleViewHolder> {

    private final Context mContext;
    private final TwoWayView mRecyclerView;
    private final List<ImageGallery> mItems;

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final ImageView image;

        public SimpleViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.imageView_image);

        }
    }

    public RVGalleryAdapter(Context context, TwoWayView recyclerView) {
        mContext = context;
        mItems = new ArrayList<ImageGallery>();
        mRecyclerView = recyclerView;
    }

    public void addItem(ImageGallery im) {
        mItems.add(im);
        notifyItemInserted(mItems.size());
        notifyDataSetChanged();
    }

    public void removeItem(int position) {

        mItems.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.item_gallery, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {
        if (mItems.get(position).getUrl() != null) { // details
            MyUtils.getPicasso().load(mItems.get(position).getUrl()).into(holder.image);
        } else { // add
            holder.image.setImageURI(mItems.get(position).getUri());
        }
        //holder.image.getLayoutParams().width = MyUtils.getWidthImageGallery(MyUtils.getSpanImageGallery());
        //holder.image.getLayoutParams().height = MyUtils.getHeightImageGallery(MyUtils.getSpanImageGallery());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public List<ImageGallery> getGallery() { return  mItems; };
}