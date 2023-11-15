package com.oceanbase.ocp.executor.internal.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.executor.internal.auth.http.DigestAuthConfig;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@Slf4j
public class DigestSignature {

    private String date;

    private String method;

    private String url;

    private String contentType;

    private String traceId;

    private DigestAuthConfig authConfig;

    public String getAuthorizationHeader() {
        String username = authConfig.getUsername();
        String password = authConfig.getPassword();
        String signature = method + "\n" + url + "\n" + contentType + "\n" + date + "\n" + traceId;
        String sign = Base64.getEncoder().encodeToString(
                hmacSha256(password, signature.getBytes(StandardCharsets.UTF_8)));
        String authorization = "OCP-HMACSHA256 " + username + ":" + sign;
        return authorization;
    }

    public static byte[] hmacSha256(String key, byte[] content) {
        try {
            Validate.notEmpty(key, "cannot be null or empty.");
            Validate.notNull(content, "content");
            Validate.isTrue(content.length != 0, "content cannot be empty.");

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(content);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Not supported signature method HmacSHA256", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Failed to calculate the signature", e);
        }
    }
}
