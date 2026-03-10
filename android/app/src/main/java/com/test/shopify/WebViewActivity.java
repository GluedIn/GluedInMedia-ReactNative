package com.test.shopify;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.test.R;
import com.gluedin.base.presentation.customView.PlusSAWMediumTextView;
import com.gluedin.base.presentation.constants.PlusSawDataHolder;

public class WebViewActivity extends AppCompatActivity {

    private ConstraintLayout mainLayout;
    private WebView webView;
    private PlusSAWMediumTextView tvTitle;
    private View imgBack;
    private LinearLayout alertLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        initViews();
        setFullScreenChanges();

        String url = getIntent().getStringExtra("url");
        String title = getIntent().getStringExtra("title");

        if (tvTitle != null) {
            tvTitle.setText(title);
        }

        setWebView(url);

        if (imgBack != null) {
            imgBack.setOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        mainLayout = findViewById(R.id.mainLayout);
        webView = findViewById(R.id.web_view);
        tvTitle = findViewById(R.id.title);
        imgBack = findViewById(R.id.img_back);
        alertLayout = findViewById(R.id.ALERT_LAYOUT);
    }

    private void setFullScreenChanges() {
        if (mainLayout == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            int systemBarsType = WindowInsetsCompat.Type.systemBars();
            v.setPadding(
                    insets.getInsets(systemBarsType).left,
                    insets.getInsets(systemBarsType).top,
                    insets.getInsets(systemBarsType).right,
                    insets.getInsets(systemBarsType).bottom
            );
            return insets;
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setWebView(String url) {
        if (webView == null) return;

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }

        webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        webView.setInitialScale(1);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            settings.setDisplayZoomControls(true);
        }

        // Cookie persistence
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().startSync();

        webView.setWebViewClient(new WebClient(alertLayout));
        webView.loadUrl(url != null ? url : "");
    }

    private static class WebClient extends WebViewClient {
        private final LinearLayout progressLayout;

        public WebClient(LinearLayout progressLayout) {
            this.progressLayout = progressLayout;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (progressLayout != null) {
                progressLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (progressLayout != null) {
                progressLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieSyncManager.getInstance().sync();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatusBar();
    }

    private void updateStatusBar() {
        int resolvedColor = ContextCompat.getColor(this, R.color.black);
        boolean darkIcons = true;

        getWindow().setStatusBarColor(resolvedColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
            controller.setAppearanceLightStatusBars(darkIcons);
        } else {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            if (darkIcons) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }
}