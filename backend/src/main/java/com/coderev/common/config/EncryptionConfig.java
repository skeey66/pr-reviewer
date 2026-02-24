package com.coderev.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EncryptionConfig {

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final int GCM_TAG_LENGTH = 128;
  private static final int IV_LENGTH = 12;

  private final SecretKeySpec secretKey;

  public EncryptionConfig(@Value("${app.encryption.secret-key}") String key) {
    byte[] keyBytes = normalizeKey(key);
    this.secretKey = new SecretKeySpec(keyBytes, "AES");
  }

  public String encrypt(String plainText) {
    try {
      byte[] iv = new byte[IV_LENGTH];
      new SecureRandom().nextBytes(iv);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

      byte[] encrypted = cipher.doFinal(plainText.getBytes());

      // IV + 암호문을 합쳐서 Base64 인코딩
      byte[] combined = ByteBuffer.allocate(IV_LENGTH + encrypted.length)
        .put(iv)
        .put(encrypted)
        .array();

      return Base64.getEncoder().encodeToString(combined);
    } catch (Exception e) {
      throw new RuntimeException("암호화 실패", e);
    }
  }

  public String decrypt(String cipherText) {
    try {
      byte[] combined = Base64.getDecoder().decode(cipherText);

      ByteBuffer buffer = ByteBuffer.wrap(combined);
      byte[] iv = new byte[IV_LENGTH];
      buffer.get(iv);
      byte[] encrypted = new byte[buffer.remaining()];
      buffer.get(encrypted);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

      return new String(cipher.doFinal(encrypted));
    } catch (Exception e) {
      throw new RuntimeException("복호화 실패", e);
    }
  }

  // 키를 32바이트(AES-256)로 정규화
  private byte[] normalizeKey(String key) {
    byte[] keyBytes = new byte[32];
    byte[] original = key.getBytes();
    System.arraycopy(original, 0, keyBytes, 0, Math.min(original.length, 32));
    return keyBytes;
  }
}
