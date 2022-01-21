/*
 * Copyright (C) 2013-2019 Sean J. Barbeau
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
package com.android.gpstest.util;

import android.text.TextUtils;
import android.util.Log;

import com.android.gpstest.model.DilutionOfPrecision;
import com.android.gpstest.model.GeoidAltitude;

public class NmeaUtils {

    private static final String TAG = "NmeaUtils";

    /**
     * Given a $GPGGA, $GNGNS, or $GNGGA NMEA sentence, return the altitude above mean sea level (geoid
     * altitude),
     * or null if the altitude can't be parsed.
     *
     * Example inputs are:
     * $GPGGA,032739.0,2804.732835,N,08224.639709,W,1,08,0.8,19.2,M,-24.0,M,,*5B
     * $GNGNS,015002.0,2804.733672,N,08224.631117,W,AAN,09,1.1,78.9,-24.0,,*23
     * $GNGGA,172814.00,2803.208136,N,08225.981423,W,1,08,1.1,-19.7,M,-24.8,M,,*5F
     * $GNGNS,165422,2804.28021,N,8225.598206,W,AAANNN,20,0.6,18.1,-24,,,V*24,,,,,,,
     *
     * Example outputs would be:
     * 19.2 (altitude MSL) and -24.0 (height of geoid above WGS84 ellipsoid)
     * 78.9 (altitude MSL) and -24.0 (height of geoid above WGS84 ellipsoid)
     * -19.7 (altitude MSL) and -24.8 (height of geoid above WGS84 ellipsoid)
     * 18.1 (altitude MSL) and -24 (height of geoid above WGS84 ellipsoid)
     *
     * @param nmeaSentence a $GPGGA, $GNGNS, or $GNGGA NMEA sentence
     * @return the altitude above mean sea level (geoid altitude) and height of the geoid above
     * the WGS84 ellipsoid in a {@link GeoidAltitude}, or null if these values can't be parsed
     */
    public static GeoidAltitude getAltitudeMeanSeaLevel(String nmeaSentence) {
        final int ALTITUDE_INDEX = 9;
        final int GEOID_HEIGHT_INDEX = 10;
        String[] tokens = nmeaSentence.split(",");

        if (nmeaSentence.startsWith("$GPGGA") || nmeaSentence.startsWith("$GNGNS") || nmeaSentence.startsWith("$GNGGA")) {
            String altitude;
            String geoidHeight;
            try {
                altitude = tokens[ALTITUDE_INDEX];
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "Bad NMEA sentence for geoid altitude - " + nmeaSentence + " :" + e);
                return null;
            }
            try {
                if (nmeaSentence.startsWith("$GNGNS")) {
                    geoidHeight = tokens[GEOID_HEIGHT_INDEX];
                } else {
                    geoidHeight = tokens[GEOID_HEIGHT_INDEX + 1];
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "Bad NMEA sentence for geoid height - " + nmeaSentence + " :" + e);
                return null;
            }
            if (!TextUtils.isEmpty(altitude) && !TextUtils.isEmpty(geoidHeight)) {
                Double altitudeParsed = null;
                Double geoidHeightParsed = null;
                try {
                    altitudeParsed = Double.parseDouble(altitude);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Bad geoid altitude value of '" + altitude + "' in NMEA sentence " + nmeaSentence + " :" + e);
                    return null;
                }

                try {
                    geoidHeightParsed = Double.parseDouble(geoidHeight);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Bad geoid height value of '" + geoidHeightParsed + "' in NMEA sentence " + nmeaSentence + " :" + e);
                    return null;
                }
                return new GeoidAltitude(altitudeParsed, geoidHeightParsed);
            } else {
                Log.w(TAG, "Couldn't parse geoid altitude and height above WGS84 ellipsoid from NMEA: " + nmeaSentence);
                return null;
            }
        } else {
            Log.w(TAG, "Input must be $GPGGA, $GNGNS, or $GNGGA NMEA: " + nmeaSentence);
            return null;
        }
    }

    /**
     * Given a $GNGSA or $GPGSA NMEA sentence, return the dilution of precision, or null if dilution of
     * precision can't be parsed.
     *
     * Example inputs are:
     * $GPGSA,A,3,03,14,16,22,23,26,,,,,,,3.6,1.8,3.1*38
     * $GNGSA,A,3,03,14,16,22,23,26,,,,,,,3.6,1.8,3.1,1*3B
     *
     * Example output is:
     * PDOP is 3.6, HDOP is 1.8, and VDOP is 3.1
     *
     * @param nmeaSentence a $GNGSA or $GPGSA NMEA sentence
     * @return the dilution of precision, or null if dilution of precision can't be parsed
     */
    public static DilutionOfPrecision getDop(String nmeaSentence) {
        final int PDOP_INDEX = 15;
        final int HDOP_INDEX = 16;
        final int VDOP_INDEX = 17;
        String[] tokens = nmeaSentence.split(",");

        if (nmeaSentence.startsWith("$GNGSA") || nmeaSentence.startsWith("$GPGSA")) {
            String pdop, hdop, vdop;
            try {
                pdop = tokens[PDOP_INDEX];
                hdop = tokens[HDOP_INDEX];
                vdop = tokens[VDOP_INDEX];
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "Bad NMEA message for parsing DOP - " + nmeaSentence + " :" + e);
                return null;
            }

            // See https://github.com/barbeau/gpstest/issues/71#issuecomment-263169174
            if (vdop.contains("*")) {
                vdop = vdop.split("\\*")[0];
            }

            if (!TextUtils.isEmpty(pdop) && !TextUtils.isEmpty(hdop) && !TextUtils.isEmpty(vdop)) {
                DilutionOfPrecision dop = null;
                try {
                    dop = new DilutionOfPrecision(Double.parseDouble(pdop), Double.parseDouble(hdop),
                            Double.parseDouble(vdop));
                } catch (NumberFormatException e) {
                    // See https://github.com/barbeau/gpstest/issues/71#issuecomment-263169174
                    Log.e(TAG, "Invalid DOP values in NMEA: " + nmeaSentence);
                }
                return dop;
            } else {
                Log.w(TAG, "Empty DOP values in NMEA: " + nmeaSentence);
                return null;
            }
        } else {
            Log.w(TAG, "Input must be a $GNGSA NMEA: " + nmeaSentence);
            return null;
        }
    }
}
