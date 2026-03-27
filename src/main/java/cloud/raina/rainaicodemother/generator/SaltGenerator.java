package cloud.raina.rainaicodemother.generator;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码盐值生成器
 */
public class SaltGenerator {
    private static final int SALT_LENGTH = 16; // 16 字节 = 128 位
    private static final SecureRandom secureRandom = new SecureRandom();
    
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    public static void main(String[] args) {
        String salt = generateSalt();
        System.out.println("生成的盐值：" + salt);
        System.out.println("盐值长度：" + salt.length());
    }
}
