package gov.nga.utils;

public abstract class ByteUtils {
	
	public static int fillBytes(byte[] bytes, int idx, int intValue) {
		bytes[idx++] = (byte) (intValue >>> 24);
		bytes[idx++] = (byte) (intValue >>> 16);
		bytes[idx++] = (byte) (intValue >>> 8);
		bytes[idx++] = (byte) (intValue >>> 0);
		return idx;
	}
}
