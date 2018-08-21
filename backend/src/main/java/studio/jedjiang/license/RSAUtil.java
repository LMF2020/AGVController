package studio.jedjiang.license;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

public abstract class RSAUtil {

	private static final String KEY_ALGORITHM = "RSA";
	/**
	 * RSA encrypt mode
	 */
	public static final int ENCRYPT_MODE = Cipher.ENCRYPT_MODE;

	/**
	 * RSA decrypt mode
	 */
	public static final int DECRYPT_MODE = Cipher.DECRYPT_MODE;
	
	public static final String ENCRYPT_RSA_TOKEN = "ENCRYPT_RSA_TOKEN";

	/**
	 * RSA KeyPair
	 * 
	 * @return
	 * @throws Exception
	 */
	public static KeyPair generateKeyPair() throws Exception {
		int bit=1024;
		KeyPairGenerator keygen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(bit, RSAKeyGenParameterSpec.F4);
		keygen.initialize(spec);
		return keygen.generateKeyPair();
	}

	/**
	 * Encrypt and decrypt the data by private key.
	 * 
	 * @param data
	 * @param key,
	 *            The key that used base64 encoded.
	 * @param mode
	 * @return
	 * @throws Exception
	 */
	public static byte[] operByPrivateKey(byte[] data, String key, int mode) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(key);// Base64Utils.decodeFromString(key);

		// Get the private key
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

		// encrypt or decrypt data
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(mode, privateKey);

		return cipher.doFinal(data);
	}

	/**
	 * Encrypt and decrypt the data by public key
	 * 
	 * @param data
	 * @param key, The key that used base64 encoded.
	 * @param mode
	 * @return
	 * @throws Exception
	 */
	public static byte[] operByPublicKey(byte[] data, String key, int mode) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(key);

		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key publicKey = keyFactory.generatePublic(x509KeySpec);

		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(mode, publicKey);
 
		return cipher.doFinal(data);
	}

	public static void main(String[] args) throws Exception {
		KeyPair keyPair = RSAUtil.generateKeyPair();
		String publicKey = Base64.encodeBase64String(keyPair.getPublic().getEncoded()); 
		String privateKey = Base64.encodeBase64String(keyPair.getPrivate().getEncoded());
		
		System.out.println("Pulickey : " + publicKey);
		System.out.println("PrivateKey : " + privateKey);
		String data = "Welcome to zoom";

		byte[] encodeData = RSAUtil.operByPublicKey(data.getBytes(), publicKey, RSAUtil.ENCRYPT_MODE);
		byte[] decodeData = RSAUtil.operByPrivateKey(encodeData, privateKey, RSAUtil.DECRYPT_MODE);

		System.out.println(new String(decodeData));
	}
}