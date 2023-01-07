package gov.nga.integration.cspace.monitoring;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import gov.nga.common.utils.CollectionUtils;

@Service
public class GrpcTMSStats {

	private static final Logger LOG = LoggerFactory.getLogger(GrpcTMSStats.class);
	
	public enum TMSOperation {
		ARTOBJECT_FETCH, ARTOBJECT_SEARCH, CONSITIUENT_FETCH, CONSTITUENT_SEARCH, DEPARTMENT_FETCH,
		DEPARTMENT_SEARCH, EXHIBITION_FETCH, EXHIBITION_SEARCH, EXHIBTION_ARTOBJECT_FETCH,
		EXHIBITION_CONSTITUENT_FETCH, LOCATION_FETCH, LOCATION_SEARCH, MEDIA_FETCH, MEDIA_SEARCH,
		PLACE_FETCH, SUGGESTION, REST_FETCH, REST_SEARCH, IMG_FETCH;
	}

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private final Map<Integer, Map<TMSOperation, Map<String, Integer>>> hours = CollectionUtils.newHashMap();
	
	public void reportTransaction(final TMSOperation type, final int numOfObjects) {
		scheduler.schedule(new TallyAdder(type, numOfObjects), 1, TimeUnit.SECONDS);
	}
	
	protected void updateTally(final int hour, final TMSOperation type, final int numOfObjects) {
		synchronized(hours) {
			final Map<TMSOperation, Map<String, Integer>> operation = 
					hours.containsKey(hour) ? hours.get(hour) : CollectionUtils.newHashMap();
			
			final Map<String, Integer> stats = 
					operation.containsKey(type) ? operation.get(type) : CollectionUtils.newHashMap();
			
			final int totalCalls = stats.containsKey("calls") ? stats.get("calls") + 1 : 1;
			final int totalObjects = stats.containsKey("numOfObjs") 
					? stats.get("numOfObjs") + numOfObjects : numOfObjects;
			stats.put("calls", totalCalls);
			stats.put("numOfObjs", totalObjects);
			
			operation.put(type, stats);
			hours.put(hour, operation);
		}
	}
	
	private int getCurrentHour() {
		return LocalTime.now().getHourOfDay();
	}
    
	@Scheduled(cron="0 0 0 * * ?")
	public void resetStats() {
		LOG.info("Clearing stats.");
		synchronized(hours) {
			hours.clear();
		}
	}
    
	@Scheduled(cron="45 59 * * * ?")
	public void reportStats() {
		final int hour = getCurrentHour();
		LOG.info("*********** Report on Remote Calls for the past hour *******");
		final Map<TMSOperation, Map<String, Integer>> operation = hours.get(hour);
		if (operation == null) {
			LOG.info("No transactions occured.");
		} else {
			final StringBuilder stats = new StringBuilder();
			for (Map.Entry<TMSOperation, Map<String, Integer>> op: operation.entrySet()) {
				stats.append(String.format("%s: %d Calls on %d objects\n", op.getKey(),
									op.getValue().get("calls"), op.getValue().get("numOfObjs")));
			}
			LOG.info(String.format("transactions\n%s", stats));
		}
	}
	
	class TallyAdder implements Runnable {
		
		private final int hour;
		final TMSOperation type; 
		final int numOfObjects;
		
		protected TallyAdder(final TMSOperation type, final int numOfObjects) {
			hour = getCurrentHour();
			this.type = type;
			this.numOfObjects = numOfObjects;
		}
		
		@Override 
		public void run() {
			updateTally(hour, type, numOfObjects);
		}
	}
	
}
