/*
 * Copyright (C) 2021 Sean J. Barbeau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.gpstest.ui.dashboard

import android.location.Location
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.gpstest.Application
import com.android.gpstest.R
import com.android.gpstest.model.SatelliteMetadata
import com.android.gpstest.model.ScanStatus
import com.android.gpstest.util.PreferenceUtils.*
import com.android.gpstest.util.SatelliteUtil.isVerticalAccuracySupported

@Composable
fun FeaturesInfoList(
    satelliteMetadata: SatelliteMetadata,
    scanStatus: ScanStatus,
    location: Location,
) {
    Text(
        modifier = Modifier.padding(5.dp),
        text = stringResource(id = R.string.dashboard_feature_info),
        style = MaterialTheme.typography.h6,
        color = MaterialTheme.colors.onBackground
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        elevation = 2.dp
    ) {
        Column {
            Nmea(satelliteMetadata)
            VerticalAccuracy(
                satelliteMetadata,
                scanStatus,
                location)
            NavigationMessages(
                satelliteMetadata,
                scanStatus
            )
        }
    }
}

@Composable
fun VerticalAccuracy(
    satelliteMetadata: SatelliteMetadata,
    scanStatus: ScanStatus,
    location: Location,
) {
    FeatureSupport(
        imageId = R.drawable.ic_vertical_accuracy_24dp,
        contentDescriptionId = R.string.dashboard_feature_vert_accuracy_title,
        featureTitleId = R.string.dashboard_feature_vert_accuracy_title,
        featureDescriptionId = R.string.dashboard_feature_vert_accuracy_description,
        satelliteMetadata = satelliteMetadata,
        supported = if (location.isVerticalAccuracySupported()) Support.YES else Support.NO,
        scanStatus = scanStatus,
        iconSizeDp = 50
    )
}

@Composable
fun NavigationMessages(
    satelliteMetadata: SatelliteMetadata,
    scanStatus: ScanStatus
) {
    val capabilityNavMessagesInt = Application.prefs.getInt(
        Application.app.getString(R.string.capability_key_nav_messages),
        CAPABILITY_UNKNOWN
    )
    // On Android S and higher we immediately know if support is available, so don't wait for scan
    val newScanStatus = ScanStatus(
        finishedScanningCfs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) true else scanStatus.finishedScanningCfs,
        timeUntilScanCompleteMs = scanStatus.timeUntilScanCompleteMs,
        scanDurationMs = scanStatus.scanDurationMs
    )
    FeatureSupport(
        imageId = R.drawable.ic_navigation_message,
        contentDescriptionId = R.string.dashboard_feature_navigation_messages_title,
        featureTitleId = R.string.dashboard_feature_navigation_messages_title,
        featureDescriptionId = R.string.dashboard_feature_navigation_messages_description,
        satelliteMetadata = satelliteMetadata,
        supported = if (capabilityNavMessagesInt == CAPABILITY_SUPPORTED) Support.YES else Support.NO,
        scanStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) newScanStatus else scanStatus,
        iconSizeDp = 50
    )
}


@Composable
fun Nmea(satelliteMetadata: SatelliteMetadata) {
    val nmeaCapability = Application.prefs.getInt(
        Application.app.getString(R.string.capability_key_nmea),
        CAPABILITY_UNKNOWN
    )

    FeatureSupport(
        imageId = R.drawable.ic_nmea,
        contentDescriptionId = R.string.pref_nmea_output_title,
        featureTitleId = R.string.pref_nmea_output_title,
        featureDescriptionId = R.string.dashboard_feature_nmea_description,
        satelliteMetadata = satelliteMetadata,
        supported = fromPref(nmeaCapability),
        iconSizeDp = 50
    )
}