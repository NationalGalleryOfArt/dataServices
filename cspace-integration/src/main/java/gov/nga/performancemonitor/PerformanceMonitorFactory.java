package gov.nga.performancemonitor;

public class PerformanceMonitorFactory 
{
	@SuppressWarnings("unchecked")
	public static PerformanceMonitor getMonitor(Class classSeed)
	{
		return new PerformanceMonitor(classSeed);
	}
	
	public static PerformanceMonitor getMonitor(String classSeed)
	{
		return new PerformanceMonitor(classSeed);
	}
}
