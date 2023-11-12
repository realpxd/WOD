/*
    App class for generating RTC tokens for Agora SDK.
*/

package com.programmerxd.wod;

import io.agora.media.RtcTokenBuilder2;
import io.agora.media.RtcTokenBuilder2.Role;

public class App {
    // Agora App credentials
    static String appId = "3f393ce1fa6b4c6b80495f09c07f5d34";
    static String appCertificate = "aea11b2781ea47888b26ec7036f9a988";

    // Array of channel names
    static String[] channelNames = {"rs1", "rs2", "rs3", "rs4", "rs5"};

    // Default user ID for RTC token
    static int uid = 0;

    // Method to generate RTC tokens
    public static String[] generateTokens(home activity, int numberOfTokens) {
        String[] tokens = new String[numberOfTokens];
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        int timestamp = (int) (System.currentTimeMillis() / 1000 + 80);

        // Generate tokens for each channel
        for (int i = 0; i < numberOfTokens; i++) {
            tokens[i] = tokenBuilder.buildTokenWithUid(appId, appCertificate,
                    channelNames[i], uid, Role.ROLE_PUBLISHER, timestamp, timestamp + i);
        }
        return tokens;
    }
}
