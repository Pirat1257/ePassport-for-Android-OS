package com.example.epassport;
import android.os.Build;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

/*--------------------Класс для шифрования и дешифрования данных--------------------*/
/*--------------------Шифрование--------------------*/
public class MyCrypto {
    public Key publicKey = null;
    private Key privateKey = null;

    private static String RSA_CONFIGURATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static String RSA_PROVIDER = "BC";

    private static final String AES_CONFIGURATION = "AES/CBC/PKCS5Padding";
    private static byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
    private static byte[] keyBytes;

    /*--------------------Зашифрование RSA (для сервера)--------------------*/
    public byte[] encryptRSA (String str_key, String clear) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey key = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.decode(str_key, Base64.NO_WRAP)));

        Cipher c = Cipher.getInstance(RSA_CONFIGURATION, RSA_PROVIDER);
        c.init(Cipher.ENCRYPT_MODE, key, new OAEPParameterSpec("SHA-256",
                "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
        byte[] encodedBytes = Base64.encode(c.doFinal(clear.getBytes("UTF-8")), Base64.DEFAULT);
        return encodedBytes;
    }

    /*--------------------Зашифрование AES (для сервера)--------------------*/
    public byte[] encryptAES(String key, byte[] mes) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, NoSuchPaddingException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        keyBytes = key.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(keyBytes);
        keyBytes = md.digest();
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance(AES_CONFIGURATION);

        SecureRandom random = new SecureRandom();
        this.ivBytes = new byte[16];
        random.nextBytes(this.ivBytes);

        cipher.init(Cipher.ENCRYPT_MODE, newKey, random);

        byte[] destination = new byte[ivBytes.length + mes.length];
        System.arraycopy(ivBytes, 0, destination, 0, ivBytes.length);
        System.arraycopy(mes, 0, destination, ivBytes.length, mes.length);

        byte[] encodedBytes = Base64.encode(cipher.doFinal(destination), Base64.DEFAULT);
        return encodedBytes;
    }

    /*--------------------Расшифрование AES (для сервера)--------------------*/
    public byte[] decryptAES(String key, byte[] mes) throws NoSuchAlgorithmException,
            NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException {
        keyBytes = key.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(keyBytes);
        keyBytes = md.digest();

        byte[] bytes = Base64.decode(mes, Base64.DEFAULT);

        byte[] ivB = Arrays.copyOfRange(bytes,0,16);
        byte[] codB = Arrays.copyOfRange(bytes,16, bytes.length);

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivB);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance(AES_CONFIGURATION);
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        byte[] res = cipher.doFinal(codB);
        return  res;
    }

    /*--------------------Генерация пары--------------------*/
    public String generate_keys() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair kp = kpg.genKeyPair();
            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new String(Base64.encode(publicKey.getEncoded(), 0));
    }

    public static byte[] decryptBASE64(String key) throws Exception {
        return Base64.decode(key, Base64.DEFAULT);
    }

    /*--------------------Шифрование с помощью открытого ключа--------------------*/
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String encryptByPublicKey(String str_key, String clear) throws Exception {
        // Необходимо использовать правильную кодировку
        byte[] keyBytes = decryptBASE64(str_key);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Key publicKey = keyFactory.generatePublic(x509KeySpec);
        // Encode the original data with RSA public key
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, publicKey);
            encodedBytes = c.doFinal(clear.getBytes());
        } catch (Exception e) {

        }
        return java.util.Base64.getEncoder().encodeToString(encodedBytes);
    }

    /*--------------------Расшифрование с помощью секретного ключа--------------------*/
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String decryptByPrivateKey(String encrypted) throws Exception {
        // Необходимо использовать правильную кодировку
        byte[] encrypted_bytes = java.util.Base64.getDecoder().decode(encrypted);
        // Decode the encoded data with RSA public key
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, privateKey);
            decodedBytes = c.doFinal(encrypted_bytes);
        } catch (Exception e) {

        }
        return new String(decodedBytes);
    }

    /*--------------------Генерация сеансового ключа--------------------*/
    public String generate_key() {
        // Set up secret key spec for 128-bit AES encryption and decryption
        SecretKeySpec sks = null;
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed("any data used as random seed".getBytes());
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128, sr);
            sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
        } catch (Exception e) {

        }
        return new String(Base64.encode(sks.getEncoded(), 0));
    }

    /*--------------------Шифрование с помощью сеансового ключа--------------------*/
    public static String encrypt(String key, byte[] clear) throws Exception
    {
        MessageDigest md = MessageDigest.getInstance("md5"); // md5 - 128-битный алгоритм хеширования, один из серии алгоритмов по построению дайджеста сообщения
        byte[] digestOfPassword = md.digest(key.getBytes("UTF-16LE")); // Для массива создается дайджест сообщения MessageDigest.
        // Экземпляр секретного ключа SecretKeySpec создается для алгоритма "AES"
        SecretKeySpec skeySpec = new SecretKeySpec(digestOfPassword, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec); // Инициализация Cipher выполняется вызовом его метода init
        byte[] encrypted = cipher.doFinal(clear); // Для шифрования и дешифрования данных с помощью экземпляра Cipher, используется один из методов update() или doFinal().
        return Base64.encodeToString(encrypted,Base64.DEFAULT);
    }

    /*--------------------Расшифрование--------------------*/
    public static String decrypt(String key, byte[] encrypted) throws Exception
    {
        MessageDigest md = MessageDigest.getInstance("md5");
        byte[] digestOfPassword = md.digest(key.getBytes("UTF-16LE"));

        SecretKeySpec skeySpec = new SecretKeySpec(digestOfPassword, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, "UTF-16LE");
    }

    /*--------Вторая версия шифрования (подходит для картинок)-------*/
    public static byte[] encrypt2(String key, byte[] clear) throws Exception
    {
        MessageDigest md = MessageDigest.getInstance("md5");
        byte[] digestOfPassword = md.digest(key.getBytes("UTF-16LE"));

        SecretKeySpec skeySpec = new SecretKeySpec(digestOfPassword, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encodedText = cipher.doFinal(clear);

        return encodedText;
    }

    /*--------Вторая версия расшифрования (подходит для картинок)--------*/
    public static byte[] decrypt2(String key, byte[] encrypted) throws Exception
    {
        MessageDigest md = MessageDigest.getInstance("md5");
        byte[] digestOfPassword = md.digest(key.getBytes("UTF-16LE"));

        SecretKeySpec skeySpec = new SecretKeySpec(digestOfPassword, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decodedText = cipher.doFinal(encrypted);

        return decodedText;
    }

    /*--------Перевод байтов хеша в строку--------*/
    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /*--------Хеш алгоритм SHA256--------*/
    public String sha256(byte[] bytes) {
        // Генерируем хеш
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(bytes);
            return bytesToHexString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
