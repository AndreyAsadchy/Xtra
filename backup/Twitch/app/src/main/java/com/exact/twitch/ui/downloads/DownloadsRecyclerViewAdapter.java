package com.exact.twitch.ui.downloads;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.cardview.widget.CardView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.exact.twitch.R;
import com.exact.twitch.model.OfflineVideo;
import com.exact.twitch.ui.adapter.BindableRecyclerViewHolder;
import com.exact.twitch.ui.adapter.PagedListRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DownloadsRecyclerViewAdapter extends PagedListRecyclerViewAdapter<OfflineVideo, DownloadsRecyclerViewAdapter.ViewHolder, DownloadsFragment.OnVideoSelectedListener> {


    private static final DiffUtil.ItemCallback<OfflineVideo> DIFF_CALLBACK = new DiffUtil.ItemCallback<OfflineVideo>() {
        @Override
        public boolean areItemsTheSame(OfflineVideo oldItem, OfflineVideo newItem) {
            return oldItem.getThumbnail().equals(newItem.getThumbnail());
        }

        @Override
        public boolean areContentsTheSame(OfflineVideo oldItem, OfflineVideo newItem) {
            return true;
        }

        @Override
        public Object getChangePayload(OfflineVideo oldItem, OfflineVideo newItem) {
            return null;
        }
    };

    public DownloadsRecyclerViewAdapter(DownloadsFragment.OnVideoSelectedListener listener, Runnable retryCallback) {
        super(DIFF_CALLBACK, R.layout.fragment_downloads_list_item, ViewHolder.class, listener, retryCallback);
    }

    @Override
    protected void onItemUpdate(ViewHolder holder, int position, @NonNull List<Object> payloads) {

    }

    class ViewHolder extends BindableRecyclerViewHolder<OfflineVideo> {

        private CardView cardView;
        private ImageView thumbnail;
        private ImageView streamerAvatar;
        private TextView streamerName;
        private TextView title;
        private TextView gameName;
        private TextView date;
        private TextView downloadDate;
        private TextView length;

        ViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.fragment_downloads_card_view);
            thumbnail = view.findViewById(R.id.fragment_downloads_iv_thumbnail);
            streamerAvatar = view.findViewById(R.id.fragment_downloads_iv_user_avatar);
            streamerName = view.findViewById(R.id.fragment_downloads_tv_username);
            title = view.findViewById(R.id.fragment_downloads_tv_title);
            gameName = view.findViewById(R.id.fragment_downloads_tv_game);
            date = view.findViewById(R.id.fragment_downloads_tv_date);
            downloadDate = view.findViewById(R.id.fragment_downloads_tv_download_date);
            length = view.findViewById(R.id.fragment_downloads_tv_length);
        }

        @Override
        protected void bindTo(OfflineVideo item) {
            cardView.setOnClickListener(v -> itemClickListener.startOfflineVideo(item));
            Picasso.get().load(item.getThumbnail()).fit().into(thumbnail);
            Picasso.get().load(item.getStreamerAvatar()).into(streamerAvatar);
            streamerName.setText(item.getChannel());
            title.setText(item.getName());
            gameName.setText(item.getGame());
            date.setText("Uploaded: " + item.getUploadDate());
            downloadDate.setText("Downloaded: " + item.getDownloadDate());
            length.setText(DateUtils.formatElapsedTime(item.getLength()));
        }

        void update(OfflineVideo video) {

        }
    }
}
