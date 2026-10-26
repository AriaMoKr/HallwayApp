package io.github.ariamokr.hallway;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The hallway, full screen, in the system's WebView. Pages on its origin stay in the app; other
 * links go to the app on the device that opens them, or a message gives the address. The remote's
 * D-pad reaches the page as arrow keys and Enter (WebView's own mapping), but with TalkBack on its
 * arrows never reach the app at all (README, "The page and the app"). Back asks the page first
 * (window.hallwayTV.back(), README "The page and the app"), then goes back a page, then leaves; a
 * long press of Back, or Menu, calls window.hallwayTV.menu().
 */
public final class MainActivity extends Activity {
    /** Android TV's stand-in for the apps a TV lacks: for a web link, a note that there is no browser. */
    private static final String STUBS = "com.android.tv.frameworkpackagestubs";

    private WebView web;
    /** Where the app opens: the hallway, or in a debug build an address given (README, "Developing"). */
    private String start = Links.HOME;
    private boolean debuggable;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        debuggable = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        if (debuggable) {
            WebView.setWebContentsDebuggingEnabled(true);
            startFrom(getIntent());
        }
        web = newWebView();
        setContentView(web);
        web.requestFocus();
        if (state == null || web.restoreState(state) == null) web.loadUrl(start);
    }

    /** A debug build takes the address to open from the intent: adb shell am start ... --es url <url>. */
    private boolean startFrom(Intent intent) {
        String url = intent.getStringExtra("url");
        if (url == null || Links.origin(url) == null) return false;
        start = url;
        return true;
    }

    private WebView newWebView() {
        WebView w = new WebView(this);
        w.setBackgroundColor(0xff0b0907);
        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true); // the settings and the shared session's name live in localStorage
        s.setMediaPlaybackRequiresUserGesture(false); // music's sounds
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setUserAgentString(Links.userAgent(s.getUserAgentString(), BuildConfig.VERSION_NAME));
        w.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (!request.isForMainFrame() || Links.inside(request.getUrl().toString(), start)) return false;
                openElsewhere(request.getUrl());
                return true;
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                // The page's renderer stopped (out of memory, say): open the hallway again rather than close.
                ((ViewGroup) view.getParent()).removeView(view);
                view.destroy();
                web = newWebView();
                setContentView(web);
                web.requestFocus();
                web.loadUrl(start);
                return true;
            }
        });
        w.setWebChromeClient(new WebChromeClient() {
            // No grey play button on a <video> before it plays.
            @Override
            public Bitmap getDefaultVideoPoster() {
                return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            }
        });
        return w;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Opened again from the home screen: the page stays as it was. A debug build can be sent elsewhere.
        if (debuggable && startFrom(intent)) web.loadUrl(start);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }

    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c == null) return;
            c.hide(WindowInsets.Type.systemBars());
            c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    // ── Links that leave the hallway's origin ─────────────────────────────────────────────────

    /** The app on the device that opens uri (YouTube's, say), else a message with its address. */
    private void openElsewhere(Uri uri) {
        Intent view = new Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE);
        List<ResolveInfo> apps = Build.VERSION.SDK_INT >= 33
                ? getPackageManager().queryIntentActivities(view, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY))
                : getPackageManager().queryIntentActivities(view, PackageManager.MATCH_DEFAULT_ONLY);
        List<ResolveInfo> real = new ArrayList<>();
        List<ComponentName> stubs = new ArrayList<>();
        for (ResolveInfo app : apps) {
            String pkg = app.activityInfo.packageName;
            if (pkg.equals(STUBS)) stubs.add(new ComponentName(pkg, app.activityInfo.name));
            else if (!pkg.equals(getPackageName())) real.add(app);
        }
        try {
            if (real.size() == 1) {
                startActivity(new Intent(view).setClassName(real.get(0).activityInfo.packageName, real.get(0).activityInfo.name));
                return;
            }
            if (real.size() > 1) {
                Intent choose = Intent.createChooser(view, null);
                if (!stubs.isEmpty()) choose.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, stubs.toArray(new ComponentName[0]));
                startActivity(choose);
                return;
            }
        } catch (ActivityNotFoundException | SecurityException e) {
            // As if there were none.
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.elsewhere_title)
                .setMessage(getString(R.string.elsewhere_message, uri.toString()))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    // ── The remote's Back and Menu ───────────────────────────────────────────────────────────

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Back and Menu come here before the page: Back's long press needs tracking, and the page
        // learns of either through window.hallwayTV.
        int code = event.getKeyCode();
        if (code == KeyEvent.KEYCODE_BACK || code == KeyEvent.KEYCODE_MENU) {
            return event.dispatch(this, getWindow().getDecorView().getKeyDispatcherState(), this);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            event.startTracking(); // held, it is a long press (onKeyLongPress), and its key up is cancelled
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            askPage("menu", null);
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.isTracking() && !event.isCanceled()) back();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            askPage("menu", null);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /** Back: the page's own first (leaving its card), then the page before, then out of the app. */
    private void back() {
        askPage("back", handled -> {
            if (handled) return;
            if (web.canGoBack()) web.goBack();
            else finish();
        });
    }

    /** Calls window.hallwayTV[name]() in the page, if it has one; then(true) when it returned true. */
    private void askPage(String name, Consumer<Boolean> then) {
        String js = "(function(){try{var t=window.hallwayTV;return !!(t&&typeof t." + name + "==='function'&&t." + name + "()===true)}catch(e){return false}})()";
        web.evaluateJavascript(js, (value) -> {
            if (then != null) then.accept("true".equals(value));
        });
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────────────────────

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
    }

    @Override
    protected void onPause() {
        web.onPause(); // the page is hidden, so it stops drawing frames
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    protected void onDestroy() {
        web.destroy();
        super.onDestroy();
    }
}
