package edu.oswego.cs.rest.controllers.utils;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.api.services.oauth2.model.Userinfo;
import com.ibm.websphere.security.jwt.*;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

@ApplicationScoped
public class OAuthUtils {
    static List<String> scopes = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email");

    private static final String fullURL = "http://localhost:13126";
    private static final String oauthClientId = "952282231282-ned8emonjrqbhj8v5b8efcr94d3nh13j.apps.googleusercontent.com";
    private static final String oauthClientSecret = "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";
    private static final String oauthAppName = "CPR";
    private static final String emailDomain = "oswego.edu";
    // private static final String fullURL = System.getenv("REACT_APP_URL");
    // private static final String oauthClientId = System.getenv("CLIENT_ID");
    // private static final String oauthClientSecret = System.getenv("CLIENT_SECRET");
    // private static final String oauthAppName = System.getenv("APP_NAME");
    // private static final String emailDomain = System.getenv("EMAIL_DOMAIN");
  

    public static GoogleAuthorizationCodeFlow flow;

    public static GoogleAuthorizationCodeFlow newFlow() throws IOException {
        flow = new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(), // Sends requests to the OAuth server
                JacksonFactory.getDefaultInstance(), // Converts between JSON and Java
                oauthClientId,
                oauthClientSecret,
                scopes) // Tells the user what permissions they're giving you
                .setDataStoreFactory(MemoryDataStoreFactory.getDefaultInstance()) // Stores the user's credential in memor
                .setAccessType("offline")                                         // @TODO Need to change this to DataStoreFactory with StoredCredential
                .build();

        return flow;
    }

    public static boolean isUserLoggedIn(String sessionID) {
        try {
            return newFlow().loadCredential(sessionID) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static Userinfo getUserInfo(String sessionId) throws IOException {
        Credential credential = newFlow().loadCredential(sessionId);
        Oauth2 oauth2Client =
                new Oauth2.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                        .setApplicationName(oauthAppName)
                        .build();

        return oauth2Client.userinfo().get().execute();
    }

    public static Tokeninfo getTokenInfo(String sessionId, String accessToken) throws IOException {
        Credential credential = newFlow().loadCredential(sessionId);
        Oauth2 oauth2Client =
                new Oauth2.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                        .setApplicationName(oauthAppName)
                        .build();

        return oauth2Client.tokeninfo().setAccessToken(accessToken).execute();
    }

    public static boolean isOswego(String sessionId) {
        try {
            Userinfo userinfo = getUserInfo(sessionId);
            return userinfo.getHd().equals(emailDomain);
        } catch (Exception e) {
            return false;
        }
    }

    public static String buildJWT(String sessionId) throws IOException, JwtException, InvalidBuilderException, InvalidClaimException {

        String accessToken = newFlow().loadCredential(sessionId).getAccessToken();
        Userinfo userinfo = getUserInfo(sessionId);
        Set<String> roles = new HashSet<String>();
        roles.add("students");

       
        String jwtToken = JwtBuilder.create("cpr22s")
                .claim(Claims.SUBJECT, userinfo.getEmail()) // subject (the user)
                .claim("upn", userinfo.getEmail()) // user principle name
                .claim("roles", roles.toArray(new String[roles.size()])) // group
                .claim("aud", "CPR22S480") // audience
                .claim("access_token", accessToken) // access token from google. 
                .claim("hd", userinfo.getHd()) // host domain
                .claim("first_name", userinfo.getGivenName()) 
                .claim("last_name", userinfo.getFamilyName())
                .claim("userID", userinfo.getId()) // google userID
                .buildJwt().compact();
        return jwtToken;
    }


}
