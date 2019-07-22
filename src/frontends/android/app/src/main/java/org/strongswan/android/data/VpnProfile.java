/*
 * Copyright (C) 2012-2018 Tobias Brunner
 * Copyright (C) 2012 Giuliano Grassi
 * Copyright (C) 2012 Ralf Sager
 * HSR Hochschule fuer Technik Rapperswil
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.  See <http://www.fsf.org/copyleft/gpl.txt>.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 */

package org.strongswan.android.data;


import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;

import org.strongswan.android.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class VpnProfile implements Cloneable
{
	/* While storing this as EnumSet would be nicer this simplifies storing it in a database */
	public static final int SPLIT_TUNNELING_BLOCK_IPV4 = 1;
	public static final int SPLIT_TUNNELING_BLOCK_IPV6 = 2;
	public static final int DEFAULT_LOGGING_LEVEL = 1;

	public static final int FLAGS_SUPPRESS_CERT_REQS = 1 << 0;
	public static final int FLAGS_DISABLE_CRL = 1 << 1;
	public static final int FLAGS_DISABLE_OCSP = 1 << 2;
	public static final int FLAGS_STRICT_REVOCATION = 1 << 3;
	public static final int FLAGS_RSA_PSS = 1 << 4;

	private String mName, mGateway, mUsername, mPassword, mCertificate, mUserCertificate;
	private String mRemoteId, mLocalId, mExcludedSubnets, mIncludedSubnets, mSelectedApps;
	private String mIkeProposal, mEspProposal;
	private Integer mMTU, mPort, mSplitTunneling, mNATKeepAlive, mFlags;
	private SelectedAppsHandling mSelectedAppsHandling = SelectedAppsHandling.SELECTED_APPS_DISABLE;
	private VpnType mVpnType;
	private UUID mUUID;
	private long mId = -1;
	private boolean byYubikey;
	private String excludePackageNames;

	private String mCertificateId;
	private ArrayList<String> allowedApplications = new ArrayList<String>();
	private int mLoggingLevel;

	public enum SelectedAppsHandling
	{
		SELECTED_APPS_DISABLE(0),
		SELECTED_APPS_EXCLUDE(1),
		SELECTED_APPS_ONLY(2);

		private Integer mValue;

		SelectedAppsHandling(int value)
		{
			mValue = value;
		}

		public Integer getValue()
		{
			return mValue;
		}
	}

	public VpnProfile()
	{
		this.mUUID = UUID.randomUUID();
	}

	public VpnProfile(Bundle bundle, Resources resources) {
		this.mUUID = UUID.randomUUID();
		fromBundle(bundle, resources);
	}

	public long getId()
	{
		return mId;
	}

	public void setId(long id)
	{
		this.mId = id;
	}

	public void setUUID(UUID uuid)
	{
		this.mUUID = uuid;
	}

	public UUID getUUID()
	{
		return mUUID;
	}

	public String getName()
	{
		return mName;
	}

	public void setName(String name)
	{
		this.mName = name;
	}

	public String getGateway()
	{
		return mGateway;
	}

	public void setGateway(String gateway)
	{
		this.mGateway = gateway;
	}

	public VpnType getVpnType()
	{
		return mVpnType;
	}

	public void setVpnType(VpnType type)
	{
		this.mVpnType = type;
	}

	public String getIkeProposal()
	{
		return mIkeProposal;
	}

	public void setIkeProposal(String proposal)
	{
		this.mIkeProposal = proposal;
	}

	public String getEspProposal()
	{
		return mEspProposal;
	}

	public void setEspProposal(String proposal)
	{
		this.mEspProposal = proposal;
	}

	public String getUsername()
	{
		return mUsername;
	}

	public void setUsername(String username)
	{
		this.mUsername = username;
	}

	public String getPassword()
	{
		return mPassword;
	}

	public void setPassword(String password)
	{
		this.mPassword = password;
	}

	public String getCertificateAlias()
	{
		return mCertificate;
	}

	public void setCertificateAlias(String alias)
	{
		this.mCertificate = alias;
	}

	public String getUserCertificateAlias()
	{
		return mUserCertificate;
	}

	public void setUserCertificateAlias(String alias)
	{
		this.mUserCertificate = alias;
	}

	public String getLocalId()
	{
		return mLocalId;
	}

	public void setLocalId(String localId)
	{
		this.mLocalId = localId;
	}

	public String getRemoteId()
	{
		return mRemoteId;
	}

	public void setRemoteId(String remoteId)
	{
		this.mRemoteId = remoteId;
	}

	public Integer getMTU()
	{
		return mMTU;
	}

	public void setMTU(Integer mtu)
	{
		this.mMTU = mtu;
	}

	public Integer getPort()
	{
		return mPort;
	}

	public void setPort(Integer port)
	{
		this.mPort = port;
	}

	public Integer getNATKeepAlive()
	{
		return mNATKeepAlive;
	}

	public void setNATKeepAlive(Integer keepalive)
	{
		this.mNATKeepAlive = keepalive;
	}

	public void setExcludedSubnets(String excludedSubnets)
	{
		this.mExcludedSubnets = excludedSubnets;
	}

	public String getExcludedSubnets()
	{
		return mExcludedSubnets;
	}

	public void setIncludedSubnets(String includedSubnets)
	{
		this.mIncludedSubnets = includedSubnets;
	}

	public String getIncludedSubnets()
	{
		return mIncludedSubnets;
	}

	public void setSelectedApps(String selectedApps)
	{
		this.mSelectedApps = selectedApps;
	}

	public void setSelectedApps(SortedSet<String> selectedApps)
	{
		this.mSelectedApps = selectedApps.size() > 0 ? TextUtils.join(" ", selectedApps) : null;
	}

	public String getSelectedApps()
	{
		return mSelectedApps;
	}

	public SortedSet<String> getSelectedAppsSet()
	{
		TreeSet<String> set = new TreeSet<>();
		if (!TextUtils.isEmpty(mSelectedApps))
		{
			set.addAll(Arrays.asList(mSelectedApps.split("\\s+")));
		}
		return set;
	}

	public void setSelectedAppsHandling(SelectedAppsHandling selectedAppsHandling)
	{
		this.mSelectedAppsHandling = selectedAppsHandling;
	}

	public void setSelectedAppsHandling(Integer value)
	{
		mSelectedAppsHandling = SelectedAppsHandling.SELECTED_APPS_DISABLE;
		for (SelectedAppsHandling handling : SelectedAppsHandling.values())
		{
			if (handling.mValue.equals(value))
			{
				mSelectedAppsHandling = handling;
				break;
			}
		}
	}

	public SelectedAppsHandling getSelectedAppsHandling()
	{
		return mSelectedAppsHandling;
	}

	public Integer getSplitTunneling()
	{
		return mSplitTunneling;
	}

	public void setSplitTunneling(Integer splitTunneling)
	{
		this.mSplitTunneling = splitTunneling;
	}

	public Integer getFlags()
	{
		return mFlags == null ? 0 : mFlags;
	}

	public void setFlags(Integer flags)
	{
		this.mFlags = flags;
	}

	public ArrayList<String> getAllowedApplications() {
		return allowedApplications;
	}

	public void setAllowedApplications(ArrayList<String> allowedApplications) {
		this.allowedApplications = allowedApplications;
	}

	public String getCertificateId() {
		return mCertificateId;
	}

	public void setCertificateId(String mCertificateId) {
		this.mCertificateId = mCertificateId;
	}


	public int getLoggingLevel() {
		return mLoggingLevel;
	}

	public void setLoggingLevel(int mLoggingLevel) {
		this.mLoggingLevel = mLoggingLevel;
	}

	public boolean isByYubikey() {
		return byYubikey;
	}

	public void setByYubikey(boolean byYubikey) {
		this.byYubikey = byYubikey;
	}

	public String getExcludePackageNames() {
		return excludePackageNames;
	}

	public List<String> getExcludePackageNameList() {
		if (TextUtils.isEmpty(excludePackageNames)) {
			return new ArrayList<>();
		}

		return Arrays.asList(excludePackageNames.split("\\s+"));
	}

	public void setExcludePackageNames(String excludePackageNames) {
		this.excludePackageNames = excludePackageNames;
	}

	public void setExcludePackageNameList(List<String> packageNameList) {
		if (packageNameList == null || packageNameList.isEmpty()) {
			return;
		}

		excludePackageNames = TextUtils.join(" ", packageNameList);
	}

	public Bundle toBundle(Resources resources) {
		Bundle bundle = new Bundle();
		bundle.putLong(resources.getString(R.string.vpn_profile_bundle_id_key), getId());
		bundle.putString(resources.getString(R.string.vpn_profile_bundle_certificate_alias_key), getCertificateAlias());
		bundle.putString(resources.getString(R.string.vpn_profile_bundle_gateway_key), getGateway());
		bundle.putString(resources.getString(R.string.vpn_profile_bundle_name_key), getName());
		bundle.putString(resources.getString(R.string.vpn_profile_bundle_password_key), getPassword());
		bundle.putString(resources.getString(R.string.vpn_profile_bundle_type_key), getVpnType().name());
		bundle.putString(resources.getString(R.string.vpn_profile_bundle_user_certificate_alias_key), getUserCertificateAlias());
		bundle.putString(resources.getString(R.string.vpn_profile_bundle_username_key), getUsername());
		bundle.putString(resources.getString(R.string.vpn_profile_bundle_certificate_id_key), getCertificateId());
		bundle.putStringArrayList(resources.getString(R.string.vpn_profile_bundle_allowed_applications), getAllowedApplications());
		bundle.putInt(resources.getString(R.string.vpn_profile_bundle_logging_level),getLoggingLevel());
		bundle.putBoolean(resources.getString(R.string.vpn_profile_bundle_by_yubikey), isByYubikey());
		bundle.putStringArrayList(resources.getString(R.string.vpn_profile_bundle_exclude_package_name), new ArrayList<>(getExcludePackageNameList()));
		return bundle;
	}

	private void fromBundle(Bundle bundle, Resources resources) {
		mId = bundle.getLong(resources.getString(R.string.vpn_profile_bundle_id_key));
		mCertificate = bundle.getString(resources.getString(R.string.vpn_profile_bundle_certificate_alias_key));
		mGateway = bundle.getString(resources.getString(R.string.vpn_profile_bundle_gateway_key));
		mName = bundle.getString(resources.getString(R.string.vpn_profile_bundle_name_key));
		mPassword = bundle.getString(resources.getString(R.string.vpn_profile_bundle_password_key));
		mVpnType = VpnType.fromIdentifier(bundle.getString(resources.getString(R.string.vpn_profile_bundle_type_key)));
		mUserCertificate = bundle.getString(resources.getString(R.string.vpn_profile_bundle_user_certificate_alias_key));
		mUsername = bundle.getString(resources.getString(R.string.vpn_profile_bundle_username_key));
		mCertificateId = bundle.getString(resources.getString(R.string.vpn_profile_bundle_certificate_id_key));
		allowedApplications = bundle.getStringArrayList(resources.getString(R.string.vpn_profile_bundle_allowed_applications));
		mLoggingLevel = bundle.getInt(resources.getString(R.string.vpn_profile_bundle_logging_level), DEFAULT_LOGGING_LEVEL);
		byYubikey = bundle.getBoolean(resources.getString(R.string.vpn_profile_bundle_by_yubikey), false);
		setExcludePackageNameList(bundle.getStringArrayList(resources.getString(R.string.vpn_profile_bundle_exclude_package_name)));
	}

	@Override
	public String toString()
	{
		return mName;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o != null && o instanceof VpnProfile)
		{
			VpnProfile other = (VpnProfile)o;
			if (this.mUUID != null && other.getUUID() != null)
			{
				return this.mUUID.equals(other.getUUID());
			}
			return this.mId == other.getId();
		}
		return false;
	}

	@Override
	public VpnProfile clone()
	{
		try
		{
			return (VpnProfile)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new AssertionError();
		}
	}
}
