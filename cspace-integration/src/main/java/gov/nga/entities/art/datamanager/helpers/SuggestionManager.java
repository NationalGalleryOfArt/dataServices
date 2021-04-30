package gov.nga.entities.art.datamanager.helpers;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import gov.nga.common.entities.art.ArtDataSuggestion;
import gov.nga.common.entities.art.ArtEntity;
import gov.nga.common.entities.art.SuggestType;
import gov.nga.common.suggest.SuggestionFactory;
import gov.nga.common.utils.CollectionUtils;
import gov.nga.common.utils.StringUtils;

public class SuggestionManager implements SuggestionFactory<ArtDataSuggestion>
{
	private final Map<SuggestType, Map<String, Set<ArtDataSuggestion>>> suggestionsMap = CollectionUtils.newHashMap();
	private SuggestType typeMode;
	
	public SuggestionManager()
	{
		for (SuggestType type: SuggestType.values())
		{
			createMap(type);
		}
	}
	
	public Map<String, Set<ArtDataSuggestion>> createMap(final SuggestType type)
	{
		Map<String, Set<ArtDataSuggestion>> map = CollectionUtils.newTreeMap(
					                new Comparator<String>() {
					                    public int compare(String a, String b) {
					                        return StringUtils.getDefaultCollator().compare(a, b);
					                    }
					                });
		suggestionsMap.put(type, map);
		return map;
	}
	
	public Map<String, Set<ArtDataSuggestion>> getMap(final SuggestType type)
	{
		return suggestionsMap.get(type);
	}
	
	public void setTypeMode(final SuggestType type)
	{
		typeMode = type;
	}

    @Override
    public ArtDataSuggestion createSuggestion(String compareString, String displayString, Object object) 
    {
        final ArtEntity artObj = (ArtEntity)object;
        return new ArtDataSuggestion(compareString, displayString, artObj.getEntityID());
    }

    @Override
    public String getKey(final String value, final Object object) 
    {
        final ArtEntity artObj = (ArtEntity)object;
        String key;
        switch (typeMode)
        {
	        case ARTIST_TITLE:
	        case ARTOBJECT_TITLE:
	        case PROVENANCE_TITLE:
	        	key = value;
        	default:
        		key = String.format("%s %d", value, artObj.getEntityID());
        }
        return key;
    }
}
