package com.exact.twitch.ui.games;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.exact.twitch.R;
import com.exact.twitch.model.game.Game;
import com.exact.twitch.ui.adapter.BindableRecyclerViewHolder;
import com.exact.twitch.ui.adapter.PagedListRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GamesRecyclerViewAdapter extends PagedListRecyclerViewAdapter<Game, GamesRecyclerViewAdapter.ViewHolder, GamesFragment.OnGameSelectedListener> {

    private static final String KEY_VIEWERS = "viewers";
    
    private static final DiffUtil.ItemCallback<Game> DIFF_CALLBACK = new DiffUtil.ItemCallback<Game>() {
        @Override
        public boolean areItemsTheSame(Game oldItem, Game newItem) {
            return oldItem.getInfo().getId().equals(newItem.getInfo().getId());
        }

        @Override
        public boolean areContentsTheSame(Game oldItem, Game newItem) {
            return oldItem.getViewers().equals(newItem.getViewers());
        }

        @Override
        public Object getChangePayload(Game oldItem, Game newItem) {
            Bundle diffBundle = new Bundle();
            if (!oldItem.getViewers().equals(newItem.getViewers())) {
                diffBundle.putInt(KEY_VIEWERS, newItem.getViewers());
            }
            return diffBundle;
        }
    };

    public GamesRecyclerViewAdapter(GamesFragment.OnGameSelectedListener listener, Runnable retryCallback) {
        super(DIFF_CALLBACK, R.layout.fragment_games_list_item, ViewHolder.class, listener, retryCallback);
    }

    @Override
    protected void onItemUpdate(ViewHolder holder, int position, @NonNull List<Object> payloads) {
        holder.update(getItem(position));
    }

    class ViewHolder extends BindableRecyclerViewHolder<Game> {

        private CardView cardView;
        private ImageView ivImage;
        private TextView tvName;
        private TextView tvViewers;

        private Context context;

        ViewHolder(View view) {
            super(view);
            context = view.getContext();
            cardView = view.findViewById(R.id.fragment_games_card_view);
            ivImage = view.findViewById(R.id.fragment_games_image);
            tvName = view.findViewById(R.id.fragment_games_tv_name);
            tvViewers = view.findViewById(R.id.fragment_games_tv_viewers);
        }

        @Override
        protected void bindTo(Game game) {
            Game.Info info = game.getInfo();
            Picasso.get().load(info.getBox().getSmall()).fit().into(ivImage);
            tvName.setText(info.getName());
            int viewersCount = game.getViewers();
            int viewersRes;
            if (viewersCount != 1) {
                viewersRes = R.string.viewers;
            } else {
                viewersRes = R.string.viewer;
            }
            tvViewers.setText(context.getString(viewersRes, viewersCount));
            cardView.setOnClickListener(v -> itemClickListener.findStreamsByGame(game));
        }

        void update(Game game) {
            tvViewers.setText(String.valueOf(game.getViewers()));
        }
    }
}
