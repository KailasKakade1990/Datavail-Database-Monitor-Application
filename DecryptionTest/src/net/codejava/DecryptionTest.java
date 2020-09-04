package net.codejava;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

public class DecryptionTest {

	public static void main(String[] args) throws KeyException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException, GeneralSecurityException, IOException {
		// TODO Auto-generated method stub
		final String secretKey = "$1234%&Key%";

		AES objAES = new AES();

		String originalString = "Mlycsnp/C9YMEnrgMTO4DjB4xlQGj6nLW/L1a/u36GwSXplwFueVoIfQx7IPLzqfxAO0Ms8YKtyLexy34gagcr5j6gC4goxzjTIgS66KLiiGyCipwaYkU0civfqgtpqWJbyOuANYxdx5M6GUr6xiGE3u+8tvbR3ARmlS3AdpOf0=";
		String decryptedString = objAES.decrypt(originalString, secretKey);

		System.out.println(originalString);
		System.out.println(decryptedString);

	}

}
