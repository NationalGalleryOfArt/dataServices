package gov.nga.imaging;

import java.util.Collection;

public class NGAImage
{
    private Imagery.PROJECT project;
    private Imagery.ENTITY_TYPE entityType;
    private Imagery.DISPLAYTYPE displayType;
    private String entityId;
    private String altTitle;
    private String sequenceNumber;
    private Collection<Derivative> derivatives;

    public Imagery.PROJECT getProject()
    {
        return project;
    }

    public void setProject(Imagery.PROJECT project)
    {
        this.project = project;
    }

    public Imagery.ENTITY_TYPE getEntityType()
    {
        return entityType;
    }

    public void setEntityType(Imagery.ENTITY_TYPE entityType)
    {
        this.entityType = entityType;
    }

    public String getEntityId()
    {
        return entityId;
    }

    public void setEntityId(String entityId)
    {
        this.entityId = entityId;
    }

    public Imagery.DISPLAYTYPE getDisplayType()
    {
        return displayType;
    }

    public void setDisplayType(Imagery.DISPLAYTYPE displayType)
    {
        this.displayType = displayType;
    }

    public String getAltTitle()
    {
        return altTitle;
    }

    public void setAltTitle(String altTitle)
    {
        this.altTitle = altTitle;
    }

    public String getSequenceNumber()
    {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber)
    {
        this.sequenceNumber = sequenceNumber;
    }

    public Collection<Derivative> getDerivatives()
    {
        return derivatives;
    }

    public void setDerivatives(Collection<Derivative> derivatives)
    {
        this.derivatives = derivatives;
    }

    public Derivative findDerivative(Imagery.FORMAT format, Integer maxWidth, Integer maxHeight)
    {
        if (maxWidth==null) maxWidth = Integer.MAX_VALUE;
        if (maxHeight==null) maxHeight = Integer.MAX_VALUE;
        
        Derivative result = null;
        for (Derivative d: getDerivatives())
        {
            if ( d.getFormat().equals(format) && d.getWidth()<=maxWidth && d.getHeight()<=maxHeight )
            {
                if (result==null || (d.getHeight()>result.getHeight() && d.getWidth()>result.getWidth()) )
                {
                    result = d;
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NGAImage ngaImage = (NGAImage) o;

        if (displayType != ngaImage.displayType) return false;
        if (entityId != null ? !entityId.equals(ngaImage.entityId) : ngaImage.entityId != null) return false;
        if (entityType != ngaImage.entityType) return false;
        if (project != ngaImage.project) return false;
        if (sequenceNumber != null ? !sequenceNumber.equals(ngaImage.sequenceNumber) : ngaImage.sequenceNumber != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = project != null ? project.hashCode() : 0;
        result = 31 * result + (entityType != null ? entityType.hashCode() : 0);
        result = 31 * result + (displayType != null ? displayType.hashCode() : 0);
        result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
        result = 31 * result + (sequenceNumber != null ? sequenceNumber.hashCode() : 0);
        return result;
    }
}
