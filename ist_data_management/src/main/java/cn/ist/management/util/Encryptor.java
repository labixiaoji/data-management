package cn.ist.management.util;

import cn.ist.management.po.Field;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Encryptor {

    private static SecretKey secretKey;

    static {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            secretKey = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static String encrypt(String input) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String encryptedInput) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedInput));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 用于String的脱敏
    private static String replaceHalfWithStar(String input) {

        if (input == null || input.isEmpty()) {
            return input;
        }

        char[] charArray = input.toCharArray();
        int length = charArray.length;
        Random random = new Random();

        int numToReplace = length / 2;

        boolean[] replacedIndices = new boolean[length];

        for (int i = 0; i < numToReplace; i++) {
            int randomIndex;
            do {
                randomIndex = random.nextInt(length);
            } while (replacedIndices[randomIndex]);
            charArray[randomIndex] = '*';
            replacedIndices[randomIndex] = true;
        }

        return new String(charArray);

    }

    // 用于Number的脱敏
    private static double roundToNearestPowerOfTen(double input) {

        // 生成一个随机数，决定向上舍入还是向下舍入
        Random random = new Random();
        boolean roundUp = random.nextBoolean();

        // 计算最接近的10的幂
        double nearestPowerOfTen = Math.pow(10, Math.floor(Math.log10(input)));

        if (roundUp) {
            // 向上舍入
            return nearestPowerOfTen * Math.ceil(input / nearestPowerOfTen);
        } else {
            // 向下舍入
            return nearestPowerOfTen * Math.floor(input / nearestPowerOfTen);
        }

    }

    public static void desensitize(List<Field> fields, List<Map<String, Object>> rows) {

        HashMap<String, Field> map = new HashMap<>();
        for (Field field : fields) {
            map.put(field.getName(), field);
        }

        for (Map<String, Object> row : rows) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                String key = entry.getKey();
                if (map.containsKey(key)) {
                    Field field = map.get(key);
                    if (field.getSensitive()) {
                        String value = String.valueOf(entry.getValue());
                        if (field.getType().equals(Field.TYPE_STRING)) {
                            entry.setValue(replaceHalfWithStar(value));
                        } else if (field.getType().equals(Field.TYPE_NUMBER)) {
                            entry.setValue(roundToNearestPowerOfTen(Double.parseDouble(value)));
                        }
                    }
                }
            }
        }

    }

    public static void encrypt(List<Field> fields, List<Map<String, Object>> rows) {

        if (rows.isEmpty()) {
            return;
        }

        HashMap<String, Field> map = new HashMap<>();
        for (Field field : fields) {
            map.put(field.getName(), field);
        }

        for (Map<String, Object> row : rows) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String key = entry.getKey();
                if (map.containsKey(key)) {
                    Field field = map.get(key);
                    if (field.getEncrypt()) {
                        entry.setValue(encrypt(String.valueOf(entry.getValue())));
                    }
                }
            }
        }

    }

}
