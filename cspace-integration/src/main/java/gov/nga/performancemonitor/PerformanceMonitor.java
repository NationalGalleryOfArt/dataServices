/*
    NGA Web API: PerformanceMonitor - used for collecting runtime performance stats 
  
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
