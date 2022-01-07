package gov.nga.entities.art.datamanager.services;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.ArtData;
import gov.nga.common.entities.art.ArtDataCacheStatusListener;
import gov.nga.common.entities.art.ArtDataCacher;
import gov.nga.common.entities.art.ArtDataManagerSubscriber;
import gov.nga.common.entities.art.ArtDataSuggestion;
import gov.nga.common.entities.art.ArtObject;
import gov.nga.common.entities.art.Constituent;
import gov.nga.common.entities.art.ConstituentAltName;
import gov.nga.common.entities.art.DataNotReadyException;
import gov.nga.common.entities.art.Department;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.Location;
import gov.nga.common.entities.art.Media;
import gov.nga.common.entities.art.Place;
import gov.nga.common.entities.art.SuggestType;
import gov.nga.common.entities.art.SuggestionManager;
import gov.nga.common.suggest.Suggest;
import gov.nga.common.utils.CollectionUtils;
import gov.nga.common.utils.SystemUtils;

public class ArtDataCache implements ArtDataManagerSubscriber, ArtDataCacher
{
    
    private static final Logger LOG = LoggerFactory.getLogger(ArtDataCache.class);
	
	
	private boolean isEnabled = true;
	private boolean isDataReady = false;
	private List<ArtDataCacheStatusListener> listners = CollectionUtils.newArrayList();
	private DataCache cache;
	
	public void unload()
	{
		synchronized(this)
		{
			cache = null;
			isDataReady = false;
		}
	}

	
	public void artDataReady(final ArtData data) 
	{
		LOG.info("Updating cache...");
        LOG.info(SystemUtils.freeMemorySummary());
		final DataCache nCache = new DataCache(data);
	    LOG.info("Finished building new cache");
        LOG.info(SystemUtils.freeMemorySummary());
	    
		synchronized(this)
		{
			cache = nCache;
			isDataReady = true;
		}
		LOG.debug("Cache updated. Notify listners.");
        LOG.info(SystemUtils.freeMemorySummary());
		notifyListners();
	}

	@Override
	public List<ArtObject> getArtObjectsRaw() throws DataNotReadyException
	{
		if (!isDataReady)
		{
			throw new DataNotReadyException();
		}
		List<ArtObject> lst = CollectionUtils.newArrayList();
		synchronized(this)
		{
			lst.addAll(cache.getArtObjectMap().values());
		}
		return lst;
	}

	@Override
	public List<Constituent> getConstituentsRaw()  throws DataNotReadyException
	{
		if (!isDataReady)
		{
			throw new DataNotReadyException();
		}
		List<Constituent> lst = CollectionUtils.newArrayList();
		synchronized(this)
		{
			lst.addAll(cache.getConstituentMap().values());
		}
		return lst;
	}
	
	@Override
	public List<Department> getDepartmentsRaw() throws DataNotReadyException
	{
		if (!isDataReady)
		{
			throw new DataNotReadyException();
		}
		List<Department> lst = CollectionUtils.newArrayList();
		synchronized(this)
		{
			lst.addAll(cache.getDepartmentMap().values());
		}
		return lst;
	}

	@Override
	public List<Location> getLocationsRaw()  throws DataNotReadyException
	{
		if (!isDataReady)
		{
			throw new DataNotReadyException();
		}
		List<Location> lst = CollectionUtils.newArrayList();
		synchronized(this)
		{
			lst.addAll(cache.getLocationMap().values());
		}
		return lst;
	}

	@Override
	public List<Exhibition> getExhibitionsRaw()  throws DataNotReadyException
	{
		if (!isDataReady)
		{
			throw new DataNotReadyException();
		}
		List<Exhibition> lst = CollectionUtils.newArrayList();
		synchronized(this)
		{
			lst.addAll(cache.getExhibitionMap().values());
		}
		return lst;
	}

	@Override
	public boolean getIsDataReady() 
	{
		return isDataReady;
	}

	@Override
	public void subScribe(final ArtDataCacheStatusListener listener) 
	{
		synchronized(listners)
		{
			listners.add(listener);
		}
	}

	@Override
	public void unSubScribe(final ArtDataCacheStatusListener listener) 
	{
		synchronized(listners)
		{
			listners.remove(listener);
		}
	}
	
	private void notifyListners()
	{
		for (ArtDataCacheStatusListener listner: listners)
		{
			LOG.debug("Notifying of cache update: " + listner);
			new Notifier(listner).run();
		}
	}
	
	private void notifyDeactivation()
	{
		for (ArtDataCacheStatusListener listner: listners)
		{
			LOG.debug("Notifying of service deactivation: " + listner);
			new DeActivationNotifier(listner).run();
		}
	}
	
	class Notifier implements Runnable
	{
		private final ArtDataCacheStatusListener listener;
		
		Notifier (ArtDataCacheStatusListener l)
		{
			listener = l;
		}
		
		public void run ()
		{
			listener.cacheUpdted();
		}
	}
	
	class DeActivationNotifier implements Runnable
	{
		private final ArtDataCacheStatusListener listener;
		
		DeActivationNotifier (ArtDataCacheStatusListener l)
		{
			listener = l;
		}
		
		public void run ()
		{
			listener.cacheDeactivating();;
		}
	}

	@Override
	public Map<Long, ArtObject> getArtObjectMap() throws DataNotReadyException 
	{
		return CollectionUtils.newHashMap(cache.getArtObjectMap());
	}

	@Override
	public Map<Long, Constituent> getConstituentMap() throws DataNotReadyException 
	{
		return CollectionUtils.newHashMap(cache.getConstituentMap());
	}

	@Override
	public Map<Long, Location> getLocationsMap() throws DataNotReadyException
	{
		return CollectionUtils.newHashMap(cache.getLocationMap());
	}

	@Override
	public Map<Long, Exhibition> getExhibitionMap() throws DataNotReadyException 
	{
		return CollectionUtils.newHashMap(cache.getExhibitionMap());
	}

	@Override
	public Map<String, Set<ArtDataSuggestion>> getSuggestionsRaw(SuggestType type) throws DataNotReadyException 
	{
		return cache.getSuggestionManager().getMap(type);
	}

	@Override
	public Map<Long, Media> getMediaMap() throws DataNotReadyException
	{
		return cache.getMediaMap();
	}

	@Override
	public List<Media> getMediaRaw() throws DataNotReadyException 
	{
		return CollectionUtils.newArrayList(getMediaMap().values());
	}

	@Override
	public Map<String, Place> getPlaceMap() throws DataNotReadyException 
	{
		return cache.getPlacesMap();
	}

	@Override
	public List<Place> getPlacesRaw() throws DataNotReadyException 
	{
		return CollectionUtils.newArrayList(getPlaceMap().values());
	}

	@Override
	public Map<String, List<Media>> getNewMediaRelationships() throws DataNotReadyException 
	{
		return cache.getNewMediaRelationships();
	}

	@Override
	public Map<Long, Place> getNewPlacesTMSLocations() throws DataNotReadyException 
	{
		return cache.getNewPlacesTMSLocations();
	}
	
	@Override
	public SuggestionManager getSuggestionManager() 
	{
		return cache.getSuggestionManager();
	}

	@Override
	public void artDataUpdated() {
		// TODO Auto-generated method stub
		
	}

}
