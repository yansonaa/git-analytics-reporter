package com.gitanalytics.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AesEncryptorTest {

    private final AesEncryptor encryptor = new AesEncryptor();

    @Test
    void testEncryptDecrypt() {
        org.springframework.test.util.ReflectionTestUtils.setField(encryptor, "secretKey", "TestSecretKey1234");
        String plain = "my-secret-token-123";
        String encrypted = encryptor.encrypt(plain);
        assertNotNull(encrypted);
        assertNotEquals(plain, encrypted);

        String decrypted = encryptor.decrypt(encrypted);
        assertEquals(plain, decrypted);
    }

    @Test
    void testEncryptDeterministic() {
        org.springframework.test.util.ReflectionTestUtils.setField(encryptor, "secretKey", "TestSecretKey1234");
        String plain = "token";
        String e1 = encryptor.encrypt(plain);
        String e2 = encryptor.encrypt(plain);
        // AES ECB 模式下相同输入相同输出（实际生产建议使用 CBC/GCM + IV）
        assertEquals(e1, e2);
    }
}
