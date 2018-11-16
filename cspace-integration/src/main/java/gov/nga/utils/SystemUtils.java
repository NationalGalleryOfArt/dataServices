/*
    Utils: SystemUtils provides utilities for printing system stats such as memory utilization
  
    Copyright (C) 2018 National Gallery of Art Washington DC
    Developers: David Beaudet

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
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
