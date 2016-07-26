package gov.nga.performancemonitor;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceMonitor 
{
	//private static Logger LOGGER = LoggerFactory.getLogger(PerformanceMonitor.class);
    
    private String logClass;
    private Calendar seedTime;
    private Calendar lastClickTime;
    private Logger log;
    
    protected PerformanceMonitor(Class<?> logClass)
    {
        this(logClass.getSimpleName());
    }
    
    protected PerformanceMonitor(String logClass)
    {
        if (logClass != null)
        {
            this.logClass = "." + logClass;
        }
        else
        {
            this.logClass = "";
        }
        log = LoggerFactory.getLogger(PerformanceMonitor.class.getCanonicalName() + this.logClass);
        //LOGGER.debug("Monitor established for: " + logClass);
        resetSeedTime();
    }
    
    public void resetSeedTime()
    {
        seedTime = Calendar.getInstance();
        lastClickTime = seedTime;
    }
    
    private Long elapseTime(Calendar seed)
    {
        return Calendar.getInstance().getTimeInMillis() - seed.getTimeInMillis();
    }
    
    public long logElapseTimeFromSeed(String text)
    {
        long time = getElapseTimeFromSeed();
        log.trace(text + " = " + time + "ms");
        return time;
    }
    
    public long getElapseTimeFromSeed()
    {
        lastClickTime = Calendar.getInstance();
        return elapseTime(seedTime);
    }
    
    public long getElapseTimeFromLastReport()
    {
        long time = elapseTime(lastClickTime);
        lastClickTime = Calendar.getInstance();
        return time;
    }
    
    public long logElapseTimeFromLastReport(String text)
    {
        long time = getElapseTimeFromLastReport();
        log.trace(text + " = " + time + "ms");
        return time;
    }
	
	public String toString()
	{
		return "Peformance monitor for class: " + logClass;
	}
}
