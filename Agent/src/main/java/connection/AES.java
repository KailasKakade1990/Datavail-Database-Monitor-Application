package connection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
/**
 * @File_Desc:Agent Connection String Encryption Oracle JDBC and MYSQL JDBC
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :AES
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 * 
 */
public class AES {
	private final String characterEncoding = "UTF-8";
	private final String cipherTransformation = "AES/CBC/PKCS5Padding";
	private final String aesEncryptionAlgorithm = "AES";

	public String decrypt(String encryptedText, String key)
			throws KeyException, GeneralSecurityException, GeneralSecurityException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException, IOException {
		byte[] cipheredBytes = Base64.getDecoder().decode(encryptedText);
		byte[] keyBytes = getKeyBytes(key);
		return new String(decrypt(cipheredBytes, keyBytes, keyBytes), characterEncoding);

	}

	public byte[] decrypt(byte[] cipherText, byte[] key, byte[] initialVector)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(cipherTransformation);
		SecretKeySpec secretKeySpecy = new SecretKeySpec(key, aesEncryptionAlgorithm);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(initialVector);
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpecy, ivParameterSpec);
		cipherText = cipher.doFinal(cipherText);

		return cipherText;
	}

	private byte[] getKeyBytes(String key) throws UnsupportedEncodingException {
		byte[] keyBytes = new byte[16];
		byte[] parameterKeyBytes = key.getBytes(characterEncoding);
		System.arraycopy(parameterKeyBytes, 0, keyBytes, 0, Math.min(parameterKeyBytes.length, keyBytes.length));

		return keyBytes;
	}
}
