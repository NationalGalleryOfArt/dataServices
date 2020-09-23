package gov.nga.entities.art;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.suggest.Suggestion;
import gov.nga.common.utils.StringUtils;
import gov.nga.common.utils.TypeUtils;

public class ArtDataSuggestion extends Suggestion
{
	private static final Logger log = LoggerFactory.getLogger(ArtDataSuggestion.class);
	
	Long entityID = null;
    
    public ArtDataSuggestion(final String compareString, final String displayString, final Long entityID) 
    {
        super(compareString, displayString);
        this.entityID = entityID;
    }
    
    public Long getEntityID() 
    {
        return entityID;
    }
    
    @Override
    public String toString()
    {
        return String.format("ArtDataSuggestion: %s %d", StringUtils.remove(StringUtils.removeHTML(getDisplayString()),'"'), getEntityID());
    }

    @Override
    public boolean equals(Object o) 
    {
        boolean isEqual = false;
        if (o instanceof ArtDataSuggestion) 
        {
            ArtDataSuggestion other = (ArtDataSuggestion) o;
            isEqual = other.entityID.equals(entityID) && getDisplayString().toLowerCase().equals(other.getDisplayString().toLowerCase());
        }
        if (entityID == 17805L) log.info(String.format("%s <> %s : %b", this, o, isEqual));
        return isEqual;
    }

    @Override
    public int hashCode() 
    {
        if (entityID == null && getDisplayString() == null)
            return 0;
        if (getDisplayString() == null)
            return Long.valueOf(entityID).hashCode();
        return (Long.valueOf(entityID).hashCode() >> 13) ^ getDisplayString().toLowerCase().hashCode();
    }
}
