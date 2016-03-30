package gov.nga.imaging;

public class Derivative
{
    private NGAImage image;
    private String id;
    private Imagery.FORMAT format;
    private Integer width; 
    private Integer height;
    private String imagePath;
    private String imageName;

    public NGAImage getImage()
    {
        return image;
    }

    public void setImage(NGAImage image)
    {
        this.image = image;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Imagery.FORMAT getFormat()
    {
        return format;
    }

    public void setFormat(Imagery.FORMAT format)
    {
        this.format = format;
    }

    public Integer getWidth()
    {
        return width;
    }

    public void setWidth(Integer width)
    {
        this.width = width;
    }

    public Integer getHeight()
    {
        return height;
    }

    public void setHeight(Integer height)
    {
        this.height = height;
    }

    public String getImagePath()
    {
        return imagePath;
    }

    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }

    public String getImageName()
    {
        return imageName;
    }

    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }
}
