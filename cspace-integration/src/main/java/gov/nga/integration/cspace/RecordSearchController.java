package gov.nga.integration.cspace;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.search.ResultsPaginator;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.StringUtils;

public abstract class RecordSearchController {

	public abstract Pattern getSourcePattern();
	public abstract String[] getSupportedSources();
	
	private static final Logger log = LoggerFactory.getLogger(RecordSearchController.class);

    // Spring REST will automatically parse values that are separated with a comma into an array
    // and will pass the individual values
    protected List<OrderField> getSortFields(List<String> order, String[] supportedNamespaces) throws APIUsageException {
    	order = CollectionUtils.clearEmptyOrNull(order);
		List<OrderField> orders = CollectionUtils.newArrayList();

    	if (order != null && order.size() > 0) {
    		for (String fieldName : order) {
    			if (fieldName == null)
    				throw new APIUsageException("Unspecified sort order field");

    			// detect the order (asc or desc)
    			boolean ascending = true;
    			if (fieldName.substring(0,1).equals("-")) {
    				ascending = false;
    				// strip the minus sign
    				fieldName = fieldName.substring(1);
    			}
    			
    			String ns = NamespaceUtils.getNamespace(fieldName,getDefaultNamespace());
    			boolean found = false;
    			for (String n : supportedNamespaces) {
    				found = found || ns.equals(n);
    			}
    			if (!found)
    				throw new APIUsageException("Unsupported namespace or empty field encountered in sort order");

    			fieldName = NamespaceUtils.ensureNamespace(fieldName,getDefaultNamespace());
    			if (fieldName != null) {
    				orders.add(new OrderField(fieldName, ascending));
    			}
    		}
    	}
    	return orders;
    }
    
    public abstract String getDefaultNamespace();

    public ResultsPaginator getPaginator(int skip, int limit) {
    	// limit results to a reasonable number to encourage well behaved API usage
    	if (limit > 1000)
    		limit = 1000;
    	return new ResultsPaginator(skip, limit);
    }

    // read the source from the URL and valid it if present.  If it's not present
    // return all of the sources supported for the subclass extending us
    protected String[] getSources(HttpServletRequest req) throws APIUsageException {
    	String source = null;
    	Matcher m = getSourcePattern().matcher(req.getRequestURI());
    	if (m.find())
    		source = m.group(1);
    	if (source == null)
    		return getSupportedSources();
    	boolean found = false;
    	for (String s : getSupportedSources()) {
    		found = found || source.equals(s);
    	}
    	if (!found)
    		throw new APIUsageException("No such source: " + source);
    	return new String[]{source};
    }

	public static void logSearchResults(HttpServletRequest request, Integer numSearchResults) {
		String url = request.getRequestURL().toString();
		if (!StringUtils.isNullOrEmpty(request.getQueryString()))
			url += "?" + request.getQueryString();
		if (numSearchResults == null)
			numSearchResults = 0;
		String message = numSearchResults + " results for " + url;
		if (request.getMethod().equals("POST")) {
			Map<String, String[]> m = request.getParameterMap();
			if (m != null)
				message += " with posted parameters " + request.getParameterMap().toString();
		}
		log.info(message);
	}
	
	// LASTMODIFIED FIELD
    protected static DateTime[] getLastModifiedDates(String[] lastModified, String[] ns_lastModified, String defaultEarliestDate) throws APIUsageException {
    	List<String> lmList = CollectionUtils.newArrayList(lastModified, ns_lastModified);
    	
    	int size = ( lmList == null ? 0 : lmList.size() );
    	String lm1 = null;
    	if (size > 0)
    		lm1 = lmList.get(0);
    	String lm2 = null;
    	if (size > 1)
    		lm2 = lmList.get(1);
    	
    	// if one of the supplied values is not null, then we can proceed
    	if (!StringUtils.isNullOrEmpty(lm1) || !StringUtils.isNullOrEmpty(lm2)) {
    		// take the first two
    		try {
    			
    			/*  FROM API CONTROL DOC
    			 * 	If a single value is supplied, that value should be assumed as the earliest date with an unbounded upper limit.  
    			 *  When two or more values are supplied, only the first two values should be used and those values represent a date 
    			 *  range.  If the second value is earlier than the first, the values should be swapped by the API implementation in 
    			 *  order to construct a valid date range for the search.
    			 */
    			
    			// if lm1 is empty, then we will always assign a lower bound based on the TMS conversion date
    			if (StringUtils.isNullOrEmpty(lm1))
    				lm1 = defaultEarliestDate;
    			// if lm2 is empty, then we assign an upper bound of today's date since no records could possibly be modified after the current time
    			if (StringUtils.isNullOrEmpty(lm2))
    				lm2 = DateTime.now().toString();
    			
    			// swap if lm1 is > lm2 for some reason, then we swap values
    			if (lm1.compareTo(lm2) > 0) {
    				String hold = lm1; lm1=lm2; lm2=hold;
    			}

    			// now, all the dates should be set to something non empty, so we try to parse them
    			DateTime dm1 = new DateTime(lm1);
    			DateTime dm2 = new DateTime(lm2);
    			return new DateTime[]{dm1,dm2};
    		}
    		catch (IllegalArgumentException ie) {
    			throw new APIUsageException("Could not parse one of the dates supplied for last modified date: "+ie.getMessage());
    		}
    	}
    	return new DateTime[]{};
    }

    public static String[] getRequestingServer(HttpServletRequest request) {
    	String port = null;
    	String scheme = request.getHeader("X-Forwarded-Proto");
		if (StringUtils.isNullOrEmpty(scheme)) {
			String sslOn = request.getHeader("X-Forwarded-SSL");
			scheme = StringUtils.isNullOrEmpty(sslOn) ? request.getScheme() : "https"; 
		}
		String host = request.getHeader("X-Forwarded-Host");
		if (StringUtils.isNullOrEmpty(host)) {
			host = request.getServerName(); 
			port = request.getServerPort() + "";
		}
		else {
			// parse the port from the server name
			String[] parts = host.split(":");
			host = parts[0];
			if (parts.length > 1)
				port = parts[1];
		}

		return new String[] {scheme,host,port};
	}
    


}