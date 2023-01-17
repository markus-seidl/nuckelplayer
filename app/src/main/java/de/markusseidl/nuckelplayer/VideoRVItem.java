package de.markusseidl.nuckelplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VideoRVItem extends RecyclerView.Adapter<VideoRVItem.ViewHolder> {

    private final Context context;

    private final VideoClickInterface videoClickInterface;

    public VideoRVItem(Context context, VideoClickInterface videoClickInterface) {
        this.context = context;
        this.videoClickInterface = videoClickInterface;
    }


    @NonNull
    @Override
    public VideoRVItem.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_rv_item, parent, false);

//        GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) itemView.getLayoutParams();
//        lp.height = parent.getMeasuredHeight() / 4;
//        itemView.setLayoutParams(lp);

        return new ViewHolder(itemView);
    }

    private VideoInformation getVideo(int position) {
        return VideoRepository.getInstance().getVideoInformations().get(position);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoRVItem.ViewHolder holder, int position) {
        VideoInformation videoInfo = getVideo(position);
        if (videoInfo.getThumbnail() != null) {
            holder.thumbnailIV.setVisibility(View.VISIBLE);
            holder.thumbnailIV.setImageBitmap(videoInfo.getThumbnail());
        } else {
            holder.thumbnailIV.setVisibility(View.GONE);
            holder.thumbnailIV.setImageBitmap(null);
        }
        holder.title.setText(videoInfo.getVideoName());
        holder.itemView.setOnClickListener(view -> videoClickInterface.onVideoClick(position));
    }

    @Override
    public int getItemCount() {
        return VideoRepository.getInstance().getVideoInformations().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailIV;
        private final TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailIV = itemView.findViewById(R.id.idIVThumbnail);
            title = itemView.findViewById(R.id.idTVVideoTitle);
        }
    }

    public interface VideoClickInterface {
        void onVideoClick(int position);
    }
}
