package gov.nga.entities.art.datamanager.services;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.ArtData;
import gov.nga.common.entities.art.ArtObject;
import gov.nga.common.entities.art.Constituent;
import gov.nga.common.entities.art.ConstituentAltName;
import gov.nga.common.entities.art.Department;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.Location;
import gov.nga.common.entities.art.Media;
import gov.nga.common.entities.art.Place;
import gov.nga.common.entities.art.SuggestType;
import gov.nga.common.entities.art.SuggestionManager;
import gov.nga.common.suggest.Suggest;
import gov.nga.common.utils.CollectionUtils;

public class DataCache 
{
    private static final Logger LOG = LoggerFactory.getLogger(DataCache.class);
    
	private final Map<Long, ArtObject> artObjects = CollectionUtils.newHashMap();
    private final Map<Long, Constituent> constituents = CollectionUtils.newHashMap();
    private final Map<Long, Location> locations = CollectionUtils.newHashMap();
    private final Map<Long, Exhibition> exhibitions = CollectionUtils.newHashMap();
    private final Map<Long, Department> departments = CollectionUtils.newHashMap();
    private final Map<String, Place> places = CollectionUtils.newHashMap();
    private final Map<Long, Media> media = CollectionUtils.newHashMap();
    private final Map<String, List<Media>> newMediaRelationshps = CollectionUtils.newHashMap();
    private final Map<Long, Place> newPlacesTMSLocations = CollectionUtils.newHashMap();
	private final SuggestionManager suggestManager;
	
	public Map<Long, ArtObject> getArtObjectMap()
	{
		return artObjects;
	}
	
	public Map<Long, Constituent> getConstituentMap()
	{
		return constituents;
	}
	
	public Map<Long, Location> getLocationMap()
	{
		return locations;
	}
	
	public Map<Long, Exhibition> getExhibitionMap()
	{
		return exhibitions;
	}
	
    public Map<Long, Department> getDepartmentMap()
    {
    	return departments;
    }
    
    public Map<String, Place> getPlacesMap()
    {
    	return places;
    }
    
    public Map<Long, Media> getMediaMap()
    {
    	return media;
    }
	
	public SuggestionManager getSuggestionManager()
	{
		return suggestManager;
	}
    
	public Map<String, List<Media>> getNewMediaRelationships()
	{
		return this.newMediaRelationshps;
	}
	
	public Map<Long, Place> getNewPlacesTMSLocations()
	{
		return this.newPlacesTMSLocations;
	}
	
	protected DataCache(final ArtData data)
    {
    	suggestManager = new SuggestionManager();
    	this.initializeArtObject(data);
    	this.initializeConstituents(data);
    	this.initializeExhibitions(data);
    	this.initializeDepartments(data);
    	this.initializeLocations(data);
    	this.initializeMedia(data);
    	this.initializePlaces(data);
    	this.initializeNewMediaRelationships(data.getMediaRelationships());
    	this.initializeNewPlacesTMSLocations(data.getPlacesTMSLocationsMap());
    }
	
	private void initializeNewPlacesTMSLocations(final Map<Long, Place> data)
	{
		this.newPlacesTMSLocations.putAll(data);
	}
	
	private void initializeNewMediaRelationships(final Map<String, List<Media>> data)
	{
		this.newMediaRelationshps.putAll(data);
	}
	
	private void initializePlaces(final ArtData data)
	{
		for (Place obj: data.getPlaces())
		{
			places.put(obj.getEntityKey(), obj);
		}
	}
	
	private void initializeMedia(final ArtData data)
	{
		for (Media obj: data.getMedia())
		{
			media.put(obj.getMediaID(), obj);
		}
	}
    
    private void initializeArtObject(final ArtData data) 
    {
		for (ArtObject obj: data.getArtObjects())
		{
            artObjects.put(obj.getObjectID(), obj);
			if (StringUtils.isNotBlank(obj.getTitle()))
			{
				suggestManager.setTypeMode(SuggestType.ARTOBJECT_TITLE);
				Suggest.consumeIndexPair(suggestManager.getMap(SuggestType.ARTOBJECT_TITLE), obj.getTitle(), obj, suggestManager);
				suggestManager.setTypeMode(SuggestType.ARTOBJECT_TITLE_ID);
				Suggest.consumeIndexPair(suggestManager.getMap(SuggestType.ARTOBJECT_TITLE_ID), 
						String.format("%s %d", obj.getTitle(), obj.getObjectID()), obj, suggestManager);
			}
		}
    }
    private void initializeConstituents(final ArtData data) 
    {
    	for (Constituent cst: data.getConstituents())
		{
			constituents.put(cst.getConstituentID(), cst);
			SuggestType typeTitle = null;
			SuggestType typeTAI = null;
			if (cst.isArtistOfNGAObject())
			{
				typeTitle = SuggestType.ARTIST_TITLE;
				typeTAI = SuggestType.ARTIST_TITLE_ID;
			}
			else if (cst.isPreviousOwnerOfNGAObject())
			{
				typeTitle = SuggestType.PROVENANCE_TITLE;
				typeTAI = SuggestType.PROVENANCE_TITLE_ID;
			}
			
			if (typeTitle != null)
			{
				suggestManager.setTypeMode(typeTitle);
				Suggest.consumeIndexPair(suggestManager.getMap(typeTitle), cst.getPreferredDisplayName(), cst, suggestManager);
				suggestManager.setTypeMode(typeTAI);
				Suggest.consumeIndexPair(suggestManager.getMap(typeTAI), String.format("%s %d", cst.getPreferredDisplayName(), cst.getConstituentID()), cst, suggestManager);
                if (cst.getAltNames() != null) 
                {
                    for (ConstituentAltName a: cst.getAltNames()) 
                    {
                        Suggest.consumeIndexPair(suggestManager.getMap(typeTitle), a.getDisplayName(), cst, suggestManager);
                        Suggest.consumeIndexPair(suggestManager.getMap(typeTAI), a.getDisplayName(), cst, suggestManager);
                    }
                }
			}
		}
    }
    
    private void initializeExhibitions(final ArtData data) 
    {
    	for (Exhibition exh: data.getExhbitions())
	    {
	    	exhibitions.put(exh.getID(), exh);
	    }
    }
    
    private void initializeDepartments(final ArtData data) 
    {
    	for (Department dept: data.getDepartments())
	    {
	    	departments.put(dept.getID(), dept);
	    }
    }
    
    private void initializeLocations(final ArtData data) 
    {
    	for (Location loc: data.getLocations())
	    {
	    	locations.put(loc.getLocationID(), loc);
	    }
    }
}
