package com.example.civilagency;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private Context mContext;
    private List<Upload> mUploads;
    private OnItemClickListener mListener;
    public int prog = 0;

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public enum Progress {
        Reported,
        Processing,
        Midway,
        Completed
    }

    public ImageAdapter(Context context,List<Upload> uploads){
        mContext = context;
        mUploads = uploads;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Upload uploadCurrent = mUploads.get(position);
        holder.textViewPotholeType.setText(uploadCurrent.getmPotholeType());
        holder.textViewLandmark.setText(uploadCurrent.getmLandmark());
        holder.date.setText(uploadCurrent.getmDate());
        Picasso.get()
                .load(uploadCurrent.getImageUrl())
                .fit()
                .centerInside()
                .into(holder.imageView);
        //TODO: change "Processing" with the actual status of that pothole form database.
        setProgressBar(Progress.Processing, holder.mprogressBar, holder.potholeStaus);
    }

    public void setProgressBar(Progress progress, ProgressBar mprogressBar, TextView potholeStaus){
        switch (progress) {
            case Reported:
                mprogressBar.setProgress(25);
                mprogressBar.setProgressTintList(ColorStateList.valueOf(0xFFf44336));
                break;
            case Processing:
                mprogressBar.setProgress(50);
                mprogressBar.setProgressTintList(ColorStateList.valueOf(0xFFff9800));
                break;
            case Midway:
                mprogressBar.setProgress(75);
                mprogressBar.setProgressTintList(ColorStateList.valueOf(0xFFffeb3b));
                break;
            default:
                mprogressBar.setProgress(100);
                mprogressBar.setProgressTintList(ColorStateList.valueOf(0xFF4caf50));
                break;
        }
        potholeStaus.setText(progress.toString());
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }
    public class ImageViewHolder extends RecyclerView.ViewHolder implements
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        public TextView textViewPotholeType;
        public TextView textViewLandmark;
        public ImageView imageView;
        public ProgressBar mprogressBar;
        public TextView potholeStaus;
        public TextView date;
        public TextView phone;


        public ImageViewHolder(View itemView) {
            super(itemView);

            textViewPotholeType = itemView.findViewById(R.id.text_view_pothole_type);
            textViewLandmark = itemView.findViewById(R.id.text_view_pothole_landmark);
            imageView = itemView.findViewById(R.id.image_view_upload);
            mprogressBar = itemView.findViewById(R.id.progress_bar_pothole);
            potholeStaus = itemView.findViewById(R.id.text_view_pothole_status);
            date = itemView.findViewById(R.id.text_view_pothole_date);
            phone = itemView.findViewById(R.id.potholes_phonenumber);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (mListener != null){
                int position = getAdapterPosition();
                if (position!= RecyclerView.NO_POSITION){
                    mListener.onDeleteClick(position);
                    return true;
                }
            }

            return false;
        }


        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            MenuItem delete = contextMenu.add(Menu.NONE,1,1,"Cancel this Report");

            delete.setOnMenuItemClickListener(this);
        }
    }




}

