package com.exact.twitch.ui.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.exact.twitch.R;
import com.exact.twitch.repository.LoadingState;

class NetworkStateItemViewHolder extends RecyclerView.ViewHolder {

    private TextView tvMessage;
    private ProgressBar progressBar;
    private Button btnRetry;

    private final Runnable retryCallback;

    private NetworkStateItemViewHolder(View view, Runnable retryCallback) {
        super(view);
        tvMessage = view.findViewById(R.id.network_state_tv_message);
        progressBar = view.findViewById(R.id.network_state_progressbar);
        btnRetry = view.findViewById(R.id.network_state_btn_retry);
        this.retryCallback = retryCallback;
    }

    void bindTo(LoadingState loadingState) {
        tvMessage.setVisibility(toVisibility(loadingState == LoadingState.FAILED));
        progressBar.setVisibility(toVisibility(loadingState == LoadingState.LOADING));
        btnRetry.setVisibility(toVisibility(loadingState == LoadingState.FAILED));
        btnRetry.setOnClickListener(v -> retryCallback.run());
    }

    public static NetworkStateItemViewHolder create(ViewGroup parent, Runnable retryCallback) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.network_state_item, parent, false);
        return new NetworkStateItemViewHolder(view, retryCallback);
    }

    private int toVisibility(boolean condition) {
        return condition ? View.VISIBLE : View.GONE;
    }
}
