package com.grafiplot.control;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.OnBackPressedCallback;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.util.Collections;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private WebView web;
    private boolean scanning;
    private final ActivityResultLauncher<ScanOptions> scanner = registerForActivityResult(new ScanContract(), result -> {
        scanning = false;
        if (result.getContents() != null) {
            web.evaluateJavascript("window.receiveScan(" + JSONObject.quote(result.getContents()) + ")", null);
        }
    });
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        setContentView(web);
        ViewCompat.setOnApplyWindowInsetsListener(web, (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        web.setWebViewClient(new WebViewClientCompat() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !"appassets.androidplatform.net".equals(request.getUrl().getHost());
            }
        });
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.addWebMessageListener(web, "Grafiplot", Collections.singleton("https://appassets.androidplatform.net"),
                (view, message, origin, mainFrame, reply) -> {
                    if (mainFrame && "scan".equals(message.getData()) && !scanning) {
                        scanning = true;
                        scanner.launch(new ScanOptions()
                            .setCaptureActivity(PortraitCaptureActivity.class)
                            .setDesiredBarcodeFormats(ScanOptions.QR_CODE, ScanOptions.EAN_13, ScanOptions.EAN_8,
                                ScanOptions.UPC_A, ScanOptions.UPC_E, ScanOptions.CODE_128, ScanOptions.CODE_39,
                                ScanOptions.CODE_93, ScanOptions.ITF, ScanOptions.RSS_14, ScanOptions.RSS_EXPANDED,
                                ScanOptions.DATA_MATRIX, ScanOptions.PDF_417)
                            .setPrompt("Mantén el móvil vertical. Centra el QR o código de barras en el recuadro.")
                            .setBeepEnabled(true).setOrientationLocked(true));
                    }
                });
        } else Toast.makeText(this, "Actualiza Android System WebView para usar el escáner", Toast.LENGTH_LONG).show();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                web.evaluateJavascript("window.goBack()", result -> { if (!"true".equals(result)) finish(); });
            }
        });
        web.loadUrl("https://appassets.androidplatform.net/assets/mobile.html");
    }
    @Override protected void onDestroy() { web.destroy(); super.onDestroy(); }
}
