package net.daimatz.jyly.api;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

public class Instagram {
    private static final String TAG = Instagram.class.toString();
    private static final String AUTH_URL = "https://instagram.com/oauth/authorize/?";
    private static final String ACCESS_TOKEN_URL = "https://api.instagram.com/oauth/access_token";
    private static final String API_BASE_URL = "https://instagram.com/v1";

    private static final ViewGroup.LayoutParams
            FILL = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private static final ViewGroup.LayoutParams
            WRAP = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    private final Context mContext;

    private final String mClientId;
    private final String mClientSecret;
    private final String mRedirectUri;
    private final Session mSession;

    public Instagram(Context context, String clientId, String clientSecret, String redirectUri) {
        mContext = context;
        mClientId = clientId;
        mClientSecret = clientSecret;
        mRedirectUri = redirectUri;
        mSession = new Session(mContext);
    }

    public void showAuthDialog(Activity activity, final AuthListener listener) {
        final Dialog dialog = new Dialog(activity) {
            @Override public void onBackPressed() {
                super.onBackPressed();
                listener.onCancel();
            }
        };
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutParams(WRAP);

        WebView webView = new WebView(activity);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, String url) {
                Log.d(TAG, "Redirecting URL: " + url);
                if (url.startsWith(mRedirectUri)) {
                    Uri uri = Uri.parse(url);
                    String code = uri.getQueryParameter("code");
                    String error = uri.getQueryParameter("error");
                    if (code != null) {
                        listener.onSuccess(code);
                    } else if (error != null) {
                        listener.onError(error);
                    }
                    dialog.dismiss();
                    return true;
                }
                return false;
            }

            @Override
            public void onReceivedError(WebView v, int errorCode, String description, String failingUrl) {
                super.onReceivedError(v, errorCode, description, failingUrl);
                listener.onError(description);
                dialog.dismiss();
            }
        });
        webView.loadUrl(buildAuthUri().toString());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSavePassword(false);
        settings.setSaveFormData(false);
        content.addView(webView, FILL);

        Display display = dialog.getWindow().getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        dialog.setContentView(content, new ViewGroup.LayoutParams(
                (int)(point.x * 0.87),
                (int)(point.y * 0.6)
        ));

        dialog.show();
    }

    private Uri buildAuthUri() {
        return Uri.parse(AUTH_URL).buildUpon()
                .appendQueryParameter("client_id", mClientId)
                .appendQueryParameter("redirect_uri", mRedirectUri)
                .appendQueryParameter("response_type", "code")
                .build();
    }

    @Nullable public User getUser() {
        return null;
    }

    public void resetSession() {
        mSession.reset();
    }

    public Session getSession() {
        return mSession;
    }

    public static class Session {
        private static final String TAG = Session.class.toString();

        private static final String SHARED_PREFERENCE = "Instagram_Preferences";
        private static final String USER_ID           = "user_id";
        private static final String USER_NAME         = "user_name";
        private static final String FULL_NAME         = "full_name";
        private static final String PROFILE_PICTURE   = "prof_pic";
        private static final String ACCESS_TOKEN      = "access_token";

        private final Context mContext;
        private final SharedPreferences mPref;

        public Session(Context context) {
            mContext = context;
            mPref = mContext.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        }

        public boolean store(User user) {
            SharedPreferences.Editor editor = mPref.edit();
            editor.putString(ACCESS_TOKEN,    user.accessToken);
            editor.putString(USER_ID,         user.userId);
            editor.putString(USER_NAME,       user.userName);
            editor.putString(FULL_NAME,       user.fullName);
            editor.putString(PROFILE_PICTURE, user.profilePicture);
            return editor.commit();
        }

        @SuppressWarnings("deprecation")
        public void reset() {
            store(new User("", "", "", "", ""));
            CookieSyncManager.createInstance(mContext);
            CookieManager.getInstance().removeAllCookie();
        }

        @Nullable
        public User getUser() {
            String accessToken = mPref.getString(ACCESS_TOKEN, ""),
                    userId = mPref.getString(USER_ID, ""),
                    userName = mPref.getString(USER_NAME, ""),
                    fullName = mPref.getString(FULL_NAME, ""),
                    profilePicture = mPref.getString(PROFILE_PICTURE, "");
            if (accessToken == null || accessToken.equals("") ||
                    userId == null || userId.equals("") ||
                    userName == null || userName.equals("") ||
                    fullName == null || fullName.equals("") ||
                    profilePicture == null || profilePicture.equals("")) {
                return null;
            }
            return new User(accessToken, userId, userName, fullName, profilePicture);
        }

        public String getAccessToken() {
            String accessToken = mPref.getString(ACCESS_TOKEN, "");
            return accessToken != null ? accessToken : "";
        }

        public boolean isActive() {
            String accessToken = mPref.getString(ACCESS_TOKEN, "");
            return accessToken != null && !accessToken.equals("");
        }
    }

    public static class User {
        public final String accessToken;
        public final String userId;
        public final String userName;
        public final String fullName;
        public final String profilePicture;
        public User(
                String accessToken,
                String userId,
                String userName,
                String fullName,
                String profilePicture
        ) {
            this.accessToken = accessToken;
            this.userId = userId;
            this.userName = userName;
            this.fullName = fullName;
            this.profilePicture = profilePicture;
        }
    }

    public interface AuthListener {
        void onSuccess(String code);
        void onError(String message);
        void onCancel();
    }
}
