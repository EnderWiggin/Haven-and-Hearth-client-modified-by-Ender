package jerklib.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Utility functions related to IP Addresses.
 * 
 * @author Andres N. Kievsky
 */
public class InetAddressUtils {

	/**
	 * Given an ip in a numeric, string format, return the InetAddress.
	 * @param ip the ip address in string format (such as 3232235780)
	 * @return the InetAddress object (such as the object representing 192.168.1.4)
	 */
	public static InetAddress parseNumericIp(String ip)
	{
		return parseNumericIp(Long.parseLong(ip));
	}

	/**
	 * Given an ip in numeric format, return the InetAddress.
	 * @param ip the ip address in long (such as 3232235780)
	 * @return the InetAddress object (such as the object representing 192.168.1.4)
	 */
	public static InetAddress parseNumericIp(long ip)
	{
	        try {
				return InetAddress.getByAddress(numericIpToByteArray(ip));
			} catch (UnknownHostException e) {
				return null;
			}
	}

	/**
	 * Given an ip in numeric format, return a byte array that can be fed to
	 * InetAddress.
	 * @param ip the ip address in long (such as 3232235780)
	 * @return the byte array.
	 */
	public static byte[] numericIpToByteArray(long ip)
	{
        byte[] ipArray = new byte[4];
        ipArray[3] = (byte) (ip & 0xff);
        ipArray[2] = (byte) ((ip >> 8) & 0xff);
        ipArray[1] = (byte) ((ip >> 16) & 0xff);
        ipArray[0] = (byte) ((ip >> 24) & 0xff);
        return ipArray;
	}

}
