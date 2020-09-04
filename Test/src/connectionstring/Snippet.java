package connectionstring;

public class Snippet {
	public static void main(String[] args) {
		final String secretKey = "ssshhhhhhhhhhh!!!!";

		String originalString = "localhost java";
		String encryptedString = AES.encrypt(originalString, secretKey);
		String decryptedString = AES.decrypt(encryptedString, secretKey);

		System.out.println(originalString);
		System.out.println(encryptedString);
		System.out.println(decryptedString);
	}
}
