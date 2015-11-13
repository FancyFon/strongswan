package org.strongswan.android.apiclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.*;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import com.google.inject.Inject;
import libcore.io.IoUtils;
import org.strongswan.android.ipc.VpnProfileCrudService;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ContentView(R.layout.api_client_activity)
public class ApiClientActivity extends RoboActivity {
    public static final String TAG = "strongSwanApiClient";
    public static final int SERVICE_IPC_TYPE = 0;
    public static final int MESSENGER_IPC_TYPE = 1;
    @Inject
    Resources resources;
    @Inject
    Random random;
    @InjectView(R.id.messenger_radio_button)
    RadioButton messengerRadioButton;
    @InjectView(R.id.service_radio_button)
    RadioButton serviceRadioButton;
    @InjectView(R.id.package_name_edit_text)
    EditText packageNameEditText;
    private VpnProfileCrudService service;
    private Messenger messenger;
    private int ipcType;
    private  ArrayList<String>allowedApps;

    private Messenger returnMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == getInteger(org.strongswan.android.api.R.integer.vpn_profile_create_message)) {
            } else if (msg.what == getInteger(org.strongswan.android.api.R.integer.vpn_profile_read_message)) {
            } else if (msg.what == getInteger(org.strongswan.android.api.R.integer.vpn_profile_read_all_message)) {
                Bundle data = msg.getData();
                long[] ids = data.getLongArray(getString(R.string.vpn_profile_bundle_ids_key));
                if (ids.length == 0) {
                    logAndToast("No vpn profiles");
                    return;
                }
                for (long id : ids) {
                    Bundle bundle = data.getBundle(getString(R.string.vpn_profile_bundle_id_params_key, id));
                    logAndToastVpnProfileBundle(bundle);
                }
            } else if (msg.what == getInteger(org.strongswan.android.api.R.integer.vpn_profile_update_message)) {
            } else if (msg.what == getInteger(org.strongswan.android.api.R.integer.vpn_profile_delete_message)) {
            } else if (msg.what == getInteger(org.strongswan.android.api.R.integer.vpn_profile_delete_all_message)) {
                logAndToast("was any vpn profiles deleted via messenger? " + (msg.arg2 == 0));
            } else {
                logAndToast("Unknown message: " + msg);
                super.handleMessage(msg);
            }
        }
    });

    //  Max bundle size is 1 MB, some checking later
    private String getCertificateBase64String(String  path) throws Exception {
           return Base64.encodeToString(IoUtils.readFileAsByteArray(path) , Base64.DEFAULT);
    }

    private ServiceConnection vpnProfileCrudServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            logAndToast("connected to service");
            ApiClientActivity.this.service = VpnProfileCrudService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            logAndToast("disconnected from service");
            service = null;
        }
    };

    private ServiceConnection vpnProfileCrudMessengerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            logAndToast("connected to messenger");
            messenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            logAndToast("disconnected from messenger");
            messenger = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        disconnect();
        super.onDestroy();
    }

    private boolean connectToService() {
        if (service == null) {
            Intent intent = new Intent(resources.getString(R.string.vpn_profile_crud_service_action));
            return bindService(intent, vpnProfileCrudServiceConnection, BIND_AUTO_CREATE);
        } else {
            logAndToast("already connected to service");
            return false;
        }
    }

    private boolean connectToMessengerService() {
        if (messenger == null) {
            Intent intent = new Intent(resources.getString(R.string.vpn_profile_crud_messenger_service_action));
            return bindService(intent, vpnProfileCrudMessengerServiceConnection, BIND_AUTO_CREATE);
        } else {
            logAndToast("already connected to messenger");
            return false;
        }
    }

    private void disconnect() {
        if (service != null) {
            unbindService(vpnProfileCrudServiceConnection);
            logAndToast("unbinding from service");
        } else {
            logAndToast("not connected to service");
        }
        if (messenger != null) {
            unbindService(vpnProfileCrudMessengerServiceConnection);
            logAndToast("unbinding from messenger");
        } else {
            logAndToast("not connected to messenger");
        }
    }

    private void logAndToast(String message) {
        logAndToast(message, null);
    }

    private void logAndToast(String message, Throwable t) {
        if (t == null) {
            Log.i(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, message, t);
            Toast.makeText(this, message + ", cause: " + t, Toast.LENGTH_SHORT).show();
        }
    }

    public void clickReadVpnProfiles(View view) {
        if (ipcType == MESSENGER_IPC_TYPE) {
            if (messenger != null) {
                Message message = Message.obtain(null, getResources().getInteger(R.integer.vpn_profile_read_all_message), random.nextInt(), 0);
                message.replyTo = returnMessenger;
                try {
                    messenger.send(message);
                } catch (RemoteException e) {
                    logAndToast("failed to get vpn profiles via messenger", e);
                }
            } else {
                logAndToast("not connected to messenger");
            }
        } else {
            if (service != null) {
                try {
                    List<Bundle> vpnProfiles = service.readVpnProfiles();
                    for (Bundle bundle : vpnProfiles) {
                        logAndToastVpnProfileBundle(bundle);
                    }
                } catch (RemoteException e) {
                    logAndToast("failed to get vpn profiles via service", e);
                }
            } else {
                logAndToast("not connected to service");
            }
        }
    }

    private void logAndToastVpnProfileBundle(Bundle bundle) {
        long mId = bundle.getLong(resources.getString(R.string.vpn_profile_bundle_id_key));
        String mGateway = bundle.getString(resources.getString(R.string.vpn_profile_bundle_gateway_key));
        String mName = bundle.getString(resources.getString(R.string.vpn_profile_bundle_name_key));
        String mPassword = bundle.getString(resources.getString(R.string.vpn_profile_bundle_password_key));
        String mVpnType = bundle.getString(resources.getString(R.string.vpn_profile_bundle_type_key));
        String mUsername = bundle.getString(resources.getString(R.string.vpn_profile_bundle_username_key));
        logAndToast("VpnProfile: id: " + mId + ", name: " + mName + ", gateway: " + mGateway + ", type: " + mVpnType +
                ", pass: " + (mPassword == null ? "null" : mPassword) +
                ", user: " + (mUsername == null ? "null" : mUsername)  );

    }

    public void clickDisconnectFromStrongSwan(View view) {
        disconnect();
    }

    public void clickConnectToStrongSwan(View view) {
        boolean result = connectToService();
        logAndToast("bind successful to service? " + result);
        result = connectToMessengerService();
        logAndToast("bind successful to messenger service? " + result);
    }

    public void clickCreateVpnProfile(View view) {
        allowedApps=new ArrayList<String>();
        allowedApps.add( packageNameEditText.getText().toString());
       Bundle eapBundle = getEapBundle();
        Bundle certBundle = getCertBundle();
        if (ipcType == MESSENGER_IPC_TYPE) {
            if (messenger != null) {
                Message message = Message.obtain(null, getResources().getInteger(R.integer.vpn_profile_create_message), random.nextInt(), 0);
                message.setData(eapBundle);
                message.replyTo = returnMessenger;
                try {
                    messenger.send(message);
                } catch (RemoteException e) {
                    logAndToast("failed to add eap vpn profile via service", e);
                }
                message = Message.obtain(null, getResources().getInteger(R.integer.vpn_profile_create_message), random.nextInt(), 0);
                message.setData(certBundle);
                message.replyTo = returnMessenger;
                try {
                    messenger.send(message);
                } catch (RemoteException e) {
                    logAndToast("failed to add cert vpn profile via service", e);
                }
            } else {
                logAndToast("not connected to messenger");
            }
        } else {
            if (service != null) {
                try {
                    boolean result = service.createVpnProfile(eapBundle);
                    logAndToast("was eap vpn profile added? " + result);
                } catch (Exception e) {
                    logAndToast("failed to add eap vpn profile via service", e);
                }
                try {
                    boolean result = service.createVpnProfile(certBundle);
                    logAndToast("was cert vpn profile added? " + result);
                } catch (Exception e) {
                    logAndToast("failed to add cert vpn profile via service", e);
                }
            } else {
                logAndToast("not connected to service");
            }
        }
    }



    private Bundle getEapBundle() {
        Bundle vpnProfile = new Bundle();
        vpnProfile.putString(resources.getString(R.string.vpn_profile_bundle_name_key), "eap famocvpn");
        vpnProfile.putString(resources.getString(R.string.vpn_profile_bundle_gateway_key), "famocvpn.emdmcloud.com");
        vpnProfile.putString(resources.getString(R.string.vpn_profile_bundle_username_key), "john");
        vpnProfile.putString(resources.getString(R.string.vpn_profile_bundle_password_key), "haslo123");
        vpnProfile.putString(resources.getString(R.string.vpn_profile_bundle_type_key), resources.getString(R.string.vpn_profile_bundle_type_ikev2_eap_value));
        vpnProfile.putStringArrayList(resources.getString(R.string.vpn_profile_bundle_allowed_applications), allowedApps);
        return vpnProfile;
    }

    private Bundle getCertBundle() {
        Bundle vpnProfile = new Bundle();
        vpnProfile.putString(resources.getString(R.string.vpn_profile_bundle_name_key), "cert famocvpn");
        vpnProfile.putString(resources.getString(R.string.vpn_profile_bundle_gateway_key), "famocvpn.emdmcloud.com");
        vpnProfile.putString(resources.getString(R.string.vpn_profile_bundle_type_key), resources.getString(R.string.vpn_profile_bundle_type_ikev2_cert_value));
        vpnProfile.putString(resources.getString(R.string.vpn_profile_bundle_user_certificate_password_key), "MAGIC_PASSWORD");
        vpnProfile.putString(resources.getString(R.string.vpn_profile_bundle_certificate_key),getCaCertificate() );
        vpnProfile.putString(resources.getString(R.string.vpn_profile_bundle_user_certificate_key), getUserCertificate());

        vpnProfile.putStringArrayList(resources.getString(R.string.vpn_profile_bundle_allowed_applications),allowedApps );
        return vpnProfile;
    }

    private String getCaCertificate() {
        try {
         return    getCertificateBase64String(Environment.getExternalStorageDirectory()+File.separator+ "strongSwan_Root_CA.pem");
        } catch ( Exception e) {
            logAndToast("Error when parsing Ca Certificate,  sending null in bundle ",e);
            return null;
        }
    }

    private String getUserCertificate() {
        try {
            return    getCertificateBase64String(Environment.getExternalStorageDirectory()+ File.separator+"strongswan.p12");
        } catch ( Exception e) {
            logAndToast("Error when parsing user certificate,  sending null in bundle ", e);
            return null;
        }
    }

    private int getInteger(int id) {
        return getResources().getInteger(id);
    }

    public void clickService(View view) {
        ipcType = SERVICE_IPC_TYPE;
        serviceRadioButton.setChecked(true);
        messengerRadioButton.setChecked(false);
    }

    public void clickMessenger(View view) {
        ipcType = MESSENGER_IPC_TYPE;
        serviceRadioButton.setChecked(false);
        messengerRadioButton.setChecked(true);
    }

    public void clickDeleteVpnProfiles(View view) {
        if (ipcType == MESSENGER_IPC_TYPE) {
            if (messenger != null) {
                Message message = Message.obtain(null, getResources().getInteger(R.integer.vpn_profile_delete_all_message), random.nextInt(), 0);
                message.replyTo = returnMessenger;
                try {
                    messenger.send(message);
                } catch (RemoteException e) {
                    logAndToast("failed to delete vpn profiles via messenger", e);
                }
            } else {
                logAndToast("not connected to messenger");
            }
        } else {
            try {
                boolean result = service.deleteVpnProfiles();
                logAndToast("was any vpn profiles deleted? " + result);
            } catch (RemoteException e) {
                logAndToast("failed to delete vpn profiles via service", e);
            }
        }

    }
}

