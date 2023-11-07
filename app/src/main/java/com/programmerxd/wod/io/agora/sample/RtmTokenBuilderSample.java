package io.agora.sample;

import io.agora.rtm.RtmTokenBuilder;
import io.agora.rtm.RtmTokenBuilder.Role;

public class RtmTokenBuilderSample {
    private static final String appId = "970CA35de60c44645bbae8a215061b33";
    private static final String appCertificate = "5CFd2fd1755d40ecb72977518be15d3b";
    private static final String userId = "2882341273";
    private static final int expireTimestamp = 0;

    public static void main(String[] args) throws Exception {
    	RtmTokenBuilder token = new RtmTokenBuilder();
        String result = token.buildToken(appId, appCertificate, userId, Role.Rtm_User, expireTimestamp);
        System.out.println(result);
    }
}
