package com.gitanalytics.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
public class AesEncryptor {

    private static final String ALGORITHM = "AES";

    @Value("${app.encrypt.key:GitAnalyticsSecretKey}")
    private String secretKey;

    public String encrypt(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(getKeyBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("加密失败", e);
            throw new RuntimeException("加密失败", e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(getKeyBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("解密失败", e);
            throw new RuntimeException("解密失败", e);
        }
    }

    private byte[] getKeyBytes() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[16];
        System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, 16));
        return key;
    }
}
