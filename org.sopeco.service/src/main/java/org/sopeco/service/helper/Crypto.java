package org.sopeco.service.helper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class to calculate the SHA-256 of a given string.
 * 
 * @author Peter Merkert
 */
public final class Crypto {

	private static final int HexOffset = 0xff;
	
	/**
	 * As utility class the constructor is not needed and made private.
	 */
	private Crypto() {
	}

	/**
	 * Calculates the hash value of the given input string using the SHA-256
	 * algorithm.
	 * 
	 * @param input the string to hash with SHA-256
	 * @return hash value of the input string.
	 */
	public static String sha256(String input) {
		
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = messageDigest.digest(input.getBytes());
			StringBuffer hashString = new StringBuffer();
			
			for (byte b : hashBytes) {
				
				String hex = Integer.toHexString(HexOffset & b);
				
				if (hex.length() == 1) {
					hashString.append(0);
				}
				
				hashString.append(hex);
			}
			
			return hashString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
		
	}
}
