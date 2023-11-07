package com.programmerxd.wod;

//import android.widget.Toast;
import io.agora.media.RtcTokenBuilder2;
import io.agora.media.RtcTokenBuilder2.Role;

public class App {
    static String appId = "3f393ce1fa6b4c6b80495f09c07f5d34";
    static String appCertificate = "aea11b2781ea47888b26ec7036f9a988";
    static String[] channelNames = {"rs1", "rs2", "rs3", "rs4", "rs5"};
//    static String channelName = "rs2";
    static int uid = 0; // The integer uid, required for an RTC token

    public static String[] generateTokens(home activity, int numberOfTokens) {
        String[] tokens = new String[numberOfTokens];
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        int timestamp = (int) (System.currentTimeMillis() / 1000 + 800);

        for (int i = 0; i < numberOfTokens; i++) {
            tokens[i] = tokenBuilder.buildTokenWithUid(appId, appCertificate,
                    channelNames[i], uid, Role.ROLE_PUBLISHER, timestamp, timestamp + i);
        }
        return tokens;
    }
}
