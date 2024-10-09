package com.tt.dramatime.util;


import com.orhanobut.logger.Logger;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author WJ
 */
public class AesGcm256Utils {

    public static void main(String[] args) throws Exception {

        String nonce = "603ed006b9d107426cc39bc1";
        String ciphertextHex = "f2462134ca3a2626fab7595f30cf22fc9b605e83dd380f1b52b5a04c7cf850655fc60e6d932b1f4b4b5b3c72dd4c1dc2b1960e271909ff8c26a5ad03505760fd40e198cfe2e4b846cd5888cb7101ca407e2e0af254f6d368ff12785ed7671d3c15f4e531f901acf02661a28c39dc8e83eac52d8ec09bfaf40c267b38649d165010300eef5c19eed7ecdee7c55d828e4a0468770a9d62d839a1cbcd8b3e974d9e76ad2c19e63576fe540035b02fafa2b4a4b92aff66c1241fd44e4907e28068bcdd7e868fa61de12bf1d4b32b0f2fb7a8909fc58868ae1d80147254676cd87192f700dddf0144eab25ad4ce8ffb1d034914cbe8725133e7e726d7ba37ad47fa5f7a01c0b5d86f5457c14b6924bddf29fceeb63060e85269c4a961c662ba6af4ee985bdd71d73c8218170f72f05655c3780a2e8b56fa1058df9667e36a67022b9ce7e61d1a37551a2f08a96958352d7198d2cbfe6439d0a6a461c16b4ee92c15fa2c3d5f3cc67749b8aa4d0d277d7df11381ee4c78dd915ad0ef93ab8ecc8a217b0e5448083a887e77616451809dfd94e4fd3eef7e7dd5630e9dde025c44424d8337c955d4e97bd9e9f7a42ed56220ac0cb9157ad8b8082c7c6506844b3342983431ff1c405337eb62f9ae33ab1aa3d84875b260d9068358a0c53bb01bc8fc493c64bceab0775821935a7dc0e1f5b9f99b36f9bb76dad61eac82225407490f16b2ad9fa4c0645974b354e1f503332fd85a8a0e1cf122a49a5bf33d8ed9e177868731fae906ea3d876c1bdf7f9bee3b91448f994b8e25b0f3f982f1fe1eb4ce30ef60adc3f34ebdee55edf105a13761e1f3ba4510155a4b360708eaf4f419ffaf73cdb538ad501b59f5cee104d3e988be58cfd2c8105d09e25e18230057914c28128d371ca319bbc0a5eaf41c27c698856b33e5f0c9cf7cf98b16720868e7b9ae";

        System.out.println(decrypt(nonce, ciphertextHex));
    }

    public static String decrypt(String nonce, String data) throws Exception {

        Logger.e("nonce:" + nonce + "\ndata:" + data);
        //Miiow TV的key 要换会DramaTime的
        //String key = "6fd2d29a965780ed3292822a1592b9ddac8c610e922e069c25b0672d47f63f47";

        String key = "bfea75228ab5b64e3048c3bd25afd52c3037fb61298b87e56a94821a07edbee1";

        // Convert hex strings to byte arrays
        byte[] keyBytes = hexStringToByteArray(key);
        byte[] nonceBytes = hexStringToByteArray(nonce);
        byte[] ciphertextBytes = hexStringToByteArray(data);

        // Create SecretKey from keyBytes
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        // Create GCMParameterSpec from nonceBytes
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, nonceBytes);

        // Initialize Cipher for decryption
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

        // Decrypt the ciphertext
        byte[] decryptedBytes = cipher.doFinal(ciphertextBytes);

        // Convert decrypted bytes to String
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }


    // Helper method to convert hex string to byte array
    private static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}


