package com.amway.kinesis.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.util.Base64;

/**
 * The Class PropertyDecriptor.
 */
public class PropertyDecriptor {

	/** The Constant CHARSET_UTF_8. */
	private static final String CHARSET_UTF_8 = "UTF-8";

	/**
	 * Gets the decrypted key.
	 *
	 * @param environmentVar
	 *            the environment var
	 * @return the decrypted key
	 */
	public static String getDecryptedKey(String environmentVar) {
		byte[] encryptedKey = Base64.decode(System.getenv(environmentVar));

		AWSKMS client = AWSKMSClientBuilder.defaultClient();

		DecryptRequest request = new DecryptRequest().withCiphertextBlob(ByteBuffer.wrap(encryptedKey));

		ByteBuffer plainTextKey = client.decrypt(request).getPlaintext();
		return new String(plainTextKey.array(), Charset.forName(CHARSET_UTF_8));
	}
}
