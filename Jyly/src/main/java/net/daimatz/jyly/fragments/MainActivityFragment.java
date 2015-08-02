package net.daimatz.jyly.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import butterknife.ButterKnife;
import butterknife.InjectView;

import net.daimatz.jyly.R;
import net.daimatz.jyly.api.Instagram;

public class MainActivityFragment extends Fragment {
    private static final String TAG = MainActivityFragment.class.toString();

    private Instagram mInstagram = null;
    private Instagram.Session mInstagramSession = null;

    public MainActivityFragment() {
    }

    @InjectView(R.id.list) ListView list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View v = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, v);

        Activity activity = getActivity();
        if (activity == null) { return v; }

        if (mInstagram == null) {
            mInstagram = new Instagram(
                activity,
                getString(R.string.instagram_client_id),
                getString(R.string.instagram_client_secret),
                getString(R.string.instagram_redirect_uri)
            );
        }
        if (mInstagramSession == null) {
            mInstagramSession = mInstagram.getSession();
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);

        if (mInstagramSession.isActive()) {
            setupListView("OK, session is active. user: " + mInstagram.getUser());
        } else {
            mInstagram.showAuthDialog(getActivity(), new Instagram.AuthListener() {
                @Override public void onSuccess(String code) {
                    setupListView("Now authorized. code: " + code);
                }
                @Override public void onError(String error) {
                    setupListView("An error occurred! error: " + error);
                }
                @Override public void onCancel() {
                    setupListView("Cancelled!");
                }
            });
        }
    }

    private void setupListView(String message) {
        Activity activity = getActivity();
        if (activity == null) { return; }
        String[] strings = {message};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_expandable_list_item_1, strings);
        list.setAdapter(adapter);
    }
}
