/*
    NGA Web API: PerformanceMonitorFactory - returns a performance monitor suitable for a given class
  
    Copyright (C) 2018 National Gallery of Art Washington DC
    Developers: NGA Contractors

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
package gov.nga.performancemonitor;

public class PerformanceMonitorFactory {
	public static PerformanceMonitor getMonitor(@SuppressWarnings("rawtypes") Class classSeed)
	{
		return new PerformanceMonitor(classSeed.getClass());
	}
	
	public static PerformanceMonitor getMonitor(String classSeed)
	{
		return new PerformanceMonitor(classSeed);
	}
}
