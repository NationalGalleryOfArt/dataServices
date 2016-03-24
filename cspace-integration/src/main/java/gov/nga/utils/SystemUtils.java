package gov.nga.utils;

import java.text.NumberFormat;

public class SystemUtils {
	
	private final static double MB = Math.pow(1024,2);

	public static String freeMemorySummary() {
		
		// System.gc();
		Runtime runtime = Runtime.getRuntime();

		NumberFormat format = NumberFormat.getInstance();

		StringBuilder sb = new StringBuilder();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		
		// <------------ max memory ------------------------------><-- PERM SIZE --> (even this varies a little)
		// <--- totalMemory() -------------><-- can be allocated ->
		// <-- used by objects --><- free -> (gc can grow free)
		
		// So, this means: can be allocated = max memory - total memory + free memory
		// allocated memory + 

//		sb.append("\nfree memory: " + format.format(freeMemory / 1024) + "\n");
		sb.append("\n  max memory (MB): " + format.format(maxMemory / MB) + "\n");
		sb.append(" used memory (MB): " + format.format((allocatedMemory - freeMemory ) / MB ) + "\n");
		sb.append("avail memory (MB): " + format.format((freeMemory + (maxMemory - allocatedMemory)) / MB) + "\n");

		return sb.toString();
	}
    
}
