package org.strongswan.android.yubikey;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.strongswan.android.R;
import org.strongswan.android.ui.VpnProfileControlActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mariuszrafalski on 08.02.2017.
 */

public class StartYubiKeyVpn extends AppCompatActivity {
    private static final Pattern OTP_PATTERN_NEO = Pattern.compile("^https://my\\.yubico\\.com/neo/([a-zA-Z0-9!]+)$");
    private static final Pattern OTP_PATTERN_YK = Pattern.compile("^https://my\\.yubico\\.com/yk/#([a-zA-Z0-9!]+)$");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null && getIntent().getDataString() != null) {
            Matcher matcher = OTP_PATTERN_NEO.matcher(getIntent().getDataString());
            if (!matcher.matches()) {
                matcher = OTP_PATTERN_YK.matcher(getIntent().getDataString());
            }
            if (matcher.matches()) {
                startVpnProfile(matcher.group(1));
            } else {
                Toast.makeText(this, R.string.yubikey_profile_not_found, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.yubikey_profile_not_found, Toast.LENGTH_LONG).show();
        }
        finish();
    }

    public void startVpnProfile(String password) {
        Intent intent = new Intent(getApplicationContext(), VpnProfileControlActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(VpnProfileControlActivity.START_YUBIKEY);
        intent.putExtra(VpnProfileControlActivity.YUBIKEY_EXTRA_PASSWORD, password);
        startActivity(intent);
    }

}
