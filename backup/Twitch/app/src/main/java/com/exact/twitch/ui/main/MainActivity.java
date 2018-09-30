package com.exact.twitch.ui.main;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import com.exact.twitch.R;
import com.exact.twitch.model.OfflineVideo;
import com.exact.twitch.model.clip.Clip;
import com.exact.twitch.model.game.Game;
import com.exact.twitch.model.stream.Stream;
import com.exact.twitch.model.video.Video;
import com.exact.twitch.ui.downloads.DownloadsFragment;
import com.exact.twitch.ui.games.GamesFragment;
import com.exact.twitch.ui.pagers.ItemAwarePagerFragment;
import com.exact.twitch.ui.menu.MenuFragment;
import com.exact.twitch.ui.fragment.OnChannelClickedListener;
import com.exact.twitch.ui.fragment.RecyclerViewFragment;
import com.exact.twitch.ui.clips.BaseClipsFragment;
import com.exact.twitch.ui.player.BasePlayerFragment;
import com.exact.twitch.ui.player.clip.ClipPlayerFragment;
import com.exact.twitch.ui.player.offline.OfflinePlayerFragment;
import com.exact.twitch.ui.player.stream.StreamPlayerFragment;
import com.exact.twitch.ui.player.video.VideoPlayerFragment;
import com.exact.twitch.ui.streams.BaseStreamsFragment;
import com.exact.twitch.ui.videos.BaseVideosFragment;
import com.exact.twitch.ui.view.draggableview.DraggableListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.List;

import javax.inject.Inject;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class MainActivity extends AppCompatActivity implements GamesFragment.OnGameSelectedListener, BaseStreamsFragment.OnStreamSelectedListener, OnChannelClickedListener, BaseClipsFragment.OnClipSelectedListener, BaseVideosFragment.OnVideoSelectedListener, HasSupportFragmentInjector, DraggableListener, DownloadsFragment.OnVideoSelectedListener, MenuFragment.OnFragmentInteractionListener {

    private static final String PLAYER_TAG = "player";
    private static final String MAXIMIZED_PLAYER_TAG = "isPlayerMaximized";

    public NavController navController;
    private BottomNavigationViewEx navigationBar;
    private FragmentManager fragmentManager;
    private boolean isPlayerMaximized;

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navController = Navigation.findNavController(this, R.id.activity_main_fragment_container);
        //TODO add token validation
        String token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", null);
        boolean isFirstLaunch = getPreferences(MODE_PRIVATE).getBoolean("first_launch", true);
        if (isFirstLaunch) {
//            navController.navigate(R.id.activity_login, null, new NavOptions.Builder().setPopUpTo(R.id.fragment_top, false).build());
        }
        navigationBar = findViewById(R.id.activity_main_nav_bar);
        navigationBar.enableShiftingMode(false);
        navigationBar.setTextVisibility(false);
        navigationBar.setOnNavigationItemReselectedListener(item -> { //TODO add switch and customize for specific buttons
            NavHostFragment hostFragment = (NavHostFragment) fragmentManager.findFragmentById(R.id.activity_main_fragment_container);
            List<Fragment> fragments = hostFragment.getChildFragmentManager().getFragments();
            System.out.println(fragments.size());
            Fragment currentFragment = fragments.get(fragments.size() - 1); //2 because list contains host fragment as well (i guess)
            if (currentFragment instanceof ItemAwarePagerFragment) {
                Fragment fragment = ((ItemAwarePagerFragment) currentFragment).getCurrentFragment();
                tryScrollingToTop(fragment);
            } else {
                tryScrollingToTop(currentFragment);
            }
        });
        NavigationUI.setupWithNavController(navigationBar, navController);
        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            isPlayerMaximized = savedInstanceState.getBoolean(MAXIMIZED_PLAYER_TAG);
            if (isPlayerMaximized) {
                navigationBar.post(this::hideNavigationBar);
            }
        }
    }

    private void tryScrollingToTop(Fragment fragment) {
        if (fragment instanceof RecyclerViewFragment) {
            ((RecyclerViewFragment) fragment).smoothScrollToTop();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp();
    }

    @Override
    public void findStreamsByGame(Game game) {
        Bundle bundle = new Bundle(1);
        bundle.putString("game", game.getInfo().getName());
        navController.navigate(R.id.action_game_selected, bundle);
    }

    @Override
    public void startStream(Stream stream) {
        Bundle bundle = new Bundle(1);
        bundle.putParcelable("stream", stream);
        StreamPlayerFragment fragment = new StreamPlayerFragment();
        fragment.setArguments(bundle);
        startPlayer(fragment);
    }

    @Override
    public void startVideo(Video video) {
        Bundle bundle = new Bundle(1);
        bundle.putParcelable("video", video);
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        fragment.setArguments(bundle);
        startPlayer(fragment);
    }

    @Override
    public void startClip(Clip clip) {
        Bundle bundle = new Bundle(1);
        bundle.putParcelable("clip", clip);
        ClipPlayerFragment fragment = new ClipPlayerFragment();
        fragment.setArguments(bundle);
        startPlayer(fragment);
    }

    @Override
    public void startOfflineVideo(OfflineVideo video) {
        Bundle bundle = new Bundle(1);
        bundle.putParcelable("video", video);
        OfflinePlayerFragment fragment = new OfflinePlayerFragment();
        fragment.setArguments(bundle);
        startPlayer(fragment);
    }

    @Override
    public void viewChannel(String channelName) {
        //TODO
    }

    @Override
    public void onBackPressed() {
        if (!isPlayerMaximized) {
            super.onBackPressed();
        } else {
            BasePlayerFragment playerFragment = (BasePlayerFragment) fragmentManager.findFragmentByTag(PLAYER_TAG);
            playerFragment.minimize();
            isPlayerMaximized = false;
        }
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

    @Override
    public void onMaximized() {
        isPlayerMaximized = true;
    }

    @Override
    public void onMinimized() {
        isPlayerMaximized = false;
    }

    @Override
    public void onClosedToLeft() {
        closePlayer();
    }

    @Override
    public void onClosedToRight() {
        closePlayer();
    }

    @Override
    public void onMoved(float horizontalDragOffset, float verticalDragOffset) {
        navigationBar.setTranslationY(-verticalDragOffset * navigationBar.getHeight() + navigationBar.getHeight());
    }

    private void startPlayer(BasePlayerFragment fragment) {
        hideNavigationBar();
        fragmentManager.beginTransaction().replace(R.id.activity_main_player_container, fragment, PLAYER_TAG).commit();
        isPlayerMaximized = true;
    }

    private void closePlayer() {
        fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag(PLAYER_TAG)).commit();
        isPlayerMaximized = false;
    }

    private void hideNavigationBar() {
        navigationBar.setTranslationY(navigationBar.getHeight());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(MAXIMIZED_PLAYER_TAG, isPlayerMaximized);
    }

    @Override
    public void onLoginClicked() {
        navController.navigate(R.id.activity_login);
    }
}
