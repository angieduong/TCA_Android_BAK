package com.seta.tollroaddroid.app.thirdparty;

import android.annotation.TargetApi;

import android.os.Build;
import android.support.v4.os.CancellationSignal;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

/**
 * Created by dtaka on 8/20/2016.
 */
@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHelper extends FingerprintManagerCompat.AuthenticationCallback {
    private FingerprintHelperListener listener;

    public FingerprintHelper(FingerprintHelperListener listener) {
        this.listener = listener;
    }

    private CancellationSignal cancellationSignal;

    public void startAuth(FingerprintManagerCompat manager, FingerprintManagerCompat.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();

        try {
            manager.authenticate(cryptoObject, 0, cancellationSignal, this, null);
        } catch (SecurityException ex) {
            listener.authenticationFailed("An error occurred:\n" + ex.getMessage());
        } catch (Exception ex) {
            listener.authenticationFailed("An error occurred\n" + ex.getMessage());
        }
    }

    public void cancel() {
        if (cancellationSignal != null)
            cancellationSignal.cancel();
    }

    public interface FingerprintHelperListener {
        public void authenticationFailed(String error);
        public void authenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result);
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        listener.authenticationFailed("Authentication error\n" + errString);
        super.onAuthenticationError(errMsgId,errString);
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        listener.authenticationFailed("Authentication help\n" + helpString);
        super.onAuthenticationHelp(helpMsgId, helpString);
    }

    @Override
    public void onAuthenticationFailed() {
        listener.authenticationFailed("Authentication failed.");
        super.onAuthenticationFailed();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        listener.authenticationSucceeded(result);
        super.onAuthenticationSucceeded(result);
    }
}