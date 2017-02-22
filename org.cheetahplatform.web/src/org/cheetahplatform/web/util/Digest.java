package org.cheetahplatform.web.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class can be used for generating hashes of credentials stored in the cheetah web database. The printed digest can directly be used
 * in the database (table user_table). Change username and password in the main method to generate a hash of your choice.
 *
 * @author stefan.zugal
 *
 */
public class Digest {
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String username = "admin@cheetahplatform.org";
		String password = "cheetah";
		printDigest(username, password);
	}

	/**
	 * Prints a digest for the given username and password.
	 *
	 * @param username
	 * @param password
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static void printDigest(String username, String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("md5");
		byte[] sha1hash = new byte[40];
		String newPassword = username + ":myrealm:" + password;
		md.update(newPassword.getBytes("iso-8859-1"), 0, newPassword.length());
		sha1hash = md.digest();
		String hashed = org.apache.tomcat.util.buf.HexUtils.convert(sha1hash);

		System.out.println("Password hash");
		System.out.println(hashed);
	}
}
