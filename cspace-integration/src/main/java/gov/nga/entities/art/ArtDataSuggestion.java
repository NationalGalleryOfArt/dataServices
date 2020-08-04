package gov.nga.entities.art;

import gov.nga.common.suggest.Suggestion;
import gov.nga.common.utils.StringUtils;
import gov.nga.common.utils.TypeUtils;

public class ArtDataSuggestion extends Suggestion
{
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
    
    public String toString()
    {
        return StringUtils.remove(StringUtils.removeHTML(getDisplayString()),'"');
    }
    
    @Override
    public boolean equals(Object o) 
    {
        boolean isEqual = false;
        if (o instanceof ArtDataSuggestion) 
        {
            ArtDataSuggestion other = (ArtDataSuggestion) o;
            isEqual = other.entityID.equals(entityID) && TypeUtils.compare(other.getCompareString().toLowerCase(), getCompareString().toLowerCase()) == 0;
        }
        return isEqual;
    }
    
    @Override
    public int hashCode() 
    {
        if (entityID == null && getCompareString() == null)
            return 0;
        if (getCompareString() == null)
            return Long.valueOf(entityID).hashCode();
        return (Long.valueOf(entityID).hashCode() >> 13) ^ getCompareString().toLowerCase().hashCode();
    }
}
