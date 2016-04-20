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
