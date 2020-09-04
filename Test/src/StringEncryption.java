import java.io.ByteArrayOutputStream;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.binary.Base64;

public class StringEncryption {
	public static void main(String[] args) throws Exception {
		String str = "linuxmysqlhost=jdbc:mysql://localhost:3306;linuxmysqluser=root;linuxmysqlpass=root";
		StringEncryption obj = new StringEncryption();
		String enstr = obj.encrypt(str);
		System.out.println(enstr);
		System.out.println(obj.decrypt("ANLSU/H2TKQWyt+O2DB4P97opzvwuwj9rOyRovOGDrNBtoOZrOnycPSGlye54R0o93Ro1Cl8NRV9pucmbXxvQxeqkpOShjyqN3TTBN4/8EBLDjtWZGZzU6lg9gftp5A6NYPXJzfP0JFBvzcp6L2LZwNTARj7wJFJ"));
	}

	String decrypt(String inputText) throws Exception {

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] _key = { 123, (byte) 217, 19, 11, 24, 26, 85, 45 };
		byte[] _vector = { (byte) 146, 64, (byte) 191, 111, 23, 3, 113, 119 };

		
		try {
			KeySpec keySpec = new DESKeySpec(_key);
			SecretKey key = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);
			IvParameterSpec iv = new IvParameterSpec(_vector);

			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key, iv);

			byte[] decoded = Base64.decodeBase64(inputText.getBytes("ASCII"));
			bout.write(cipher.doFinal(decoded));
		} catch (Exception e) {
			System.out.println("Exception ... " + e);
		}
		return new String(bout.toByteArray(), "ASCII");
	}

	String encrypt(String inputText) throws Exception {
		byte[] _key = { 123, (byte) 217, 19, 11, 24, 26, 85, 45 };
		byte[] _vector = { (byte) 146, 64, (byte) 191, 111, 23, 3, 113, 119 };
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			KeySpec keySpec = new DESKeySpec(_key);
			SecretKey key = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);
			IvParameterSpec iv = new IvParameterSpec(_vector);
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			bout.write(cipher.doFinal(inputText.getBytes("ASCII")));
		} catch (Exception e) {
			System.out.println("Exception .. " + e.getMessage());
		}
		return new String(Base64.encodeBase64(bout.toByteArray()), "ASCII");
	}
}
