/*
 * Copyright Â© 2015 FancyFon Software Ltd.
 * All rights reserved.
 * 
 */
package org.strongswan.android.ipc;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.util.Base64;
import android.util.Log;
import org.strongswan.android.R;
import org.strongswan.android.security.LocalKeystore;
import libcore.io.IoUtils;
import java.io.IOException;
import java.security.KeyStoreException;
import java.util.List;

/**
 * @author Piotr SorĂłbka <piotr.sorobka@fancyfon.com>
 */
public class VpnProfileCrudServiceImpl extends Service {

    private static final String TAG = VpnProfileCrudServiceImpl.class.getSimpleName();
    public static final String VPN_PROFILE_CRUD_LOCAL_ACTION = "org.strongswan.android.action.BIND_VPN_PROFILE_CRUD_SERVICE_LOCAL";
    private LocalBinder localBinder = new LocalBinder();
    private VpnProfileCrud vpnProfileCrud;
    private LocalKeystore localKeystore;

    @Override
    public void onCreate() {
        super.onCreate();
        vpnProfileCrud = new VpnProfileCrud(this);
    }

    @Override
    public void onDestroy() {
        vpnProfileCrud.close();
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        public VpnProfileCrudServiceImpl getService() {
            return VpnProfileCrudServiceImpl.this;
        }
    }

    private final VpnProfileCrudService.Stub remoteBinder = new VpnProfileCrudService.Stub() {

        @Override
        public boolean createVpnProfile(Bundle vpnProfile) throws RemoteException {
            installCertificatesFromProfile(vpnProfile);
            return vpnProfileCrud.createVpnProfile(vpnProfile);
        }

        @Override
        public Bundle readVpnProfile(long l) throws RemoteException {
            return vpnProfileCrud.readVpnProfile(l);
        }

        @Override
        public List<Bundle> readVpnProfiles() throws RemoteException {
            return vpnProfileCrud.readVpnProfiles();
        }

        @Override
        public boolean updateVpnProfile(Bundle vpnProfile) throws RemoteException {
            return vpnProfileCrud.updateVpnProfile(vpnProfile);
        }

        @Override
        public boolean deleteVpnProfile(long l) throws RemoteException {
            return vpnProfileCrud.deleteVpnProfile(l);
        }

        @Override
        public boolean deleteVpnProfiles() throws RemoteException {
            return vpnProfileCrud.deleteVpnProfiles();
        }

    };



    private void installCertificatesFromProfile(Bundle vpnProfileBundle) {
        try {

                createLocalKeystore();
                String id = localKeystore.generateId();
                String userAlias = localKeystore.addPkcs12(extractCertificateFromBundle(vpnProfileBundle, R.string.vpn_profile_bundle_user_certificate_key),
                        vpnProfileBundle.getString(getResources().getString(R.string.vpn_profile_bundle_user_certificate_password_key)), id);
                String certAlias = localKeystore.addCaCertificate(extractCertificateFromBundle(vpnProfileBundle, R.string.vpn_profile_bundle_certificate_key), id);

                vpnProfileBundle.putString(getResources().getString(R.string.vpn_profile_bundle_certificate_id_key), id);
                vpnProfileBundle.putString(getResources().getString(R.string.vpn_profile_bundle_user_certificate_alias_key),
                        userAlias);
                vpnProfileBundle.putString(getResources().getString(R.string.vpn_profile_bundle_certificate_alias_key),
                        certAlias);

        } catch (Throwable e) {
            Log.e(TAG, "Error installing certificate: " + e);
        }
    }




    private   byte[] extractCertificateFromBundle(Bundle vpnProfile, int certificateResourceId) throws KeyStoreException {
           try {
               return Base64.decode(vpnProfile.getString(getResources().getString(certificateResourceId)), Base64.DEFAULT);
           }catch (Throwable t){
              Log.d(TAG,"Can't parse certificate "+t);
               return null;

           }

    }

    private void createLocalKeystore() throws KeyStoreException {
        if(localKeystore == null) {
            localKeystore = new LocalKeystore();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(VPN_PROFILE_CRUD_LOCAL_ACTION)) {
            return localBinder;
        }
        return remoteBinder;
    }
}
