package com.tradeadvisor.app;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    private static final int TASKBAR_HEIGHT_DP = 64;

    private View taskbar;
    private WebView webView;
    private int taskbarHeightPx;
    private boolean taskbarVisible = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = getBridge().getWebView();
        taskbarHeightPx =
                (int) (TASKBAR_HEIGHT_DP * getResources().getDisplayMetrics().density);

        if (webView == null || !(webView.getParent() instanceof CoordinatorLayout)) {
            return;
        }

        CoordinatorLayout root = (CoordinatorLayout) webView.getParent();

        taskbar = getLayoutInflater().inflate(R.layout.bottom_taskbar, root, false);

        CoordinatorLayout.LayoutParams taskbarParams =
                new CoordinatorLayout.LayoutParams(
                        CoordinatorLayout.LayoutParams.MATCH_PARENT,
                        taskbarHeightPx
                );
        taskbarParams.gravity = Gravity.BOTTOM;

        root.addView(taskbar, taskbarParams);

        Button home = taskbar.findViewById(R.id.taskbar_home);
        Button scanner = taskbar.findViewById(R.id.taskbar_scanner);
        Button signals = taskbar.findViewById(R.id.taskbar_signals);
        Button settings = taskbar.findViewById(R.id.taskbar_settings);

        home.setOnClickListener(v ->
                webView.loadUrl("https://www.tradeadvisorfx.com"));

        scanner.setOnClickListener(v ->
                webView.loadUrl("https://www.tradeadvisorfx.com/chart-analysis"));

        signals.setOnClickListener(v ->
                webView.loadUrl("https://www.tradeadvisorfx.com/signals"));

        settings.setOnClickListener(v ->
                webView.loadUrl("https://www.tradeadvisorfx.com/settings"));

        setTaskbarVisible(false);
        checkAuthentication();
    }

    private void setTaskbarVisible(boolean visible) {
        if (taskbar == null || webView == null || taskbarVisible == visible) {
            return;
        }

        taskbarVisible = visible;
        taskbar.setVisibility(visible ? View.VISIBLE : View.GONE);

        ViewGroup.LayoutParams params = webView.getLayoutParams();

        if (params instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.LayoutParams webParams =
                    (CoordinatorLayout.LayoutParams) params;

            webParams.bottomMargin = visible ? taskbarHeightPx : 0;
            webView.setLayoutParams(webParams);
        }
    }

    private void checkAuthentication() {
        if (webView == null) {
            return;
        }

        String script =
                "(function() {" +
                "try {" +
                "for (var i = 0; i < localStorage.length; i++) {" +
                "var k = localStorage.key(i);" +
                "if (!k || k.indexOf('sb-') !== 0 || k.indexOf('-auth-token') === -1) continue;" +
                "var v = JSON.parse(localStorage.getItem(k) || 'null');" +
                "var s = v && (v.currentSession || v);" +
                "if (s && s.access_token && s.user) return true;" +
                "}" +
                "} catch (e) {}" +
                "return false;" +
                "})()";

        webView.evaluateJavascript(script, value -> {
            boolean authenticated = "true".equals(value);
            setTaskbarVisible(authenticated);
            webView.postDelayed(this::checkAuthentication, 1000);
        });
    }
}
