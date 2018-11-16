package gov.nga.imaging;

/*
NGA Art Data API: Derivative is a commonly used base class for imagery
used with most image operations in the NGA's art data APIs

Copyright (C) 2018 National Gallery of Art Washington DC
Developers: David Beaudet, NGA Contractors

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
