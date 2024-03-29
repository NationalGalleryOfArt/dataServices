/*
    NGA Art Data API: Art Entity for representing an image of an art object. These
    images are treated somewhat differently than other images since the workflow
    associated with the production of art object images is fairly well defined.
    Note: with IIIF now in place, the entire Image APIs need to be completely 
    refactored as they are currently a bit laborious to use and are rooted in
    the management of specific derivative sizes rather than providing the type of
    flexibility that IIIF affords.

    Copyright (C) 2018 National Gallery of Art Washington DC
    Developers: David Beaudet

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

package gov.nga.entities.art;

import gov.nga.utils.CollectionUtils;
import gov.nga.utils.DateUtils;
import gov.nga.utils.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.measure.Measure;
import javax.measure.converter.ConversionException;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class ArtObjectImage extends Derivative {

    // private static final Logger log = LoggerFactory.getLogger(ArtObjectImage.class);

    protected static final String PRIMARY_OBJECT_IMAGE_SEQUENCE = Long.toString(0);
    public static final String defaultSource = "web-images-repository";
    private static final Logger log = LoggerFactory.getLogger(ArtObjectImage.class);
    static {log.info(ArtObjectImage.class.getName() + " loaded");}

    public ArtObjectImage(ArtDataManagerService manager) {
        super(manager);
    }

    private static final String fetchAllImagesQuery = """
            SELECT uuid as imageID,  '/iiif/' as imgVolumePath, uuid as filename, 
            case when iiifFormat = 'tif' then 'PTIF' else upper(iiifFormat) end as format, 
            width,    height,        null as targetWidth,  null as targetHeight,
            viewType, sequence,      depictsTMSObjectID as tmsObjectID,  created as catalogued 
            FROM data.published_images where depictsTMSObjectID is not null and viewType in ('primary','alternate')
    """;

    protected String getAllImagesQuery() {
        return fetchAllImagesQuery;
    }
    
    public ArtObjectImage(
            ArtDataManagerService manager, 
            String imageID, String imgVolumePath, String filename, String ft, 
            Long width, Long height, Long targetWidth, Long targetHeight,
            String vt, String sequence, Long tmsObjectID, String catalogued ) throws SQLException  {
        
        this(manager);
        setSource(defaultSource);
        setImageID(imageID);
        setImgVolumePath(imgVolumePath);
        setFilename(filename);
        setWidth(width);
        setHeight(height);
        setTargetWidth(targetWidth);
        setTargetHeight(targetHeight);
        setSequence(sequence);
        setArtObjectID(tmsObjectID);
        setCatalogued(catalogued);
        

        if (vt.equals(IMGVIEWTYPE.PRIMARY.getLabel()))
            setViewType(IMGVIEWTYPE.PRIMARY);
        else if (vt.equals(IMGVIEWTYPE.CROPPED.getLabel()))
            setViewType(IMGVIEWTYPE.CROPPED);
        else 
            setViewType(IMGVIEWTYPE.ALTERNATE);
        
        if (ft.equals(IMGFORMAT.PTIF.getLabel()))
            setFormat(IMGFORMAT.PTIF);
        else 
            setFormat(IMGFORMAT.JPEG);
    }

    public ArtObjectImage(ArtDataManagerService manager, ResultSet rs) throws SQLException  {
        this(
                manager, 
                rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                TypeUtils.getLong(rs, 5), TypeUtils.getLong(rs, 6), TypeUtils.getLong(rs, 7), TypeUtils.getLong(rs, 8), 
                rs.getString(9), rs.getString(10), TypeUtils.getLong(rs, 11), DateUtils.formatDate(DateUtils.DATE_FORMAT_ISO_8601_WITH_TIME_AND_TZ_CORRECT, rs.getTimestamp(12))
        );
    }

    public Derivative factory(ResultSet rs) throws SQLException {
        ArtObjectImage d = new ArtObjectImage(getManager(),rs);
        return d; 
    }
    
    public IMAGECLASS getImageClass() {
        return IMAGECLASS.ARTOBJECTIMAGE;
    }
    
    public static boolean isPrimaryView(Derivative d) {
    	return ( d.getViewType() == IMGVIEWTYPE.PRIMARY && d.getSequence().equals(PRIMARY_OBJECT_IMAGE_SEQUENCE)) ;
    }
    
    // no facets for art object images... at least not yet
	public List<String> getFacetValue(Object f) {
		return CollectionUtils.newArrayList();
	}
	
	public Double getDimensionOfSubject(ArtObjectDimension.DIMENSION_TYPE dimensionType) {
		Double subjectMeasurement = null;
		ArtObject o = getArtObject();
		if (o != null) {
			try {
				Measure<Double, Length> m = o.getDimension(dimensionType);
				if (m != null) {
					Double d = m.doubleValue(SI.CENTIMETER);
					if (d != null && d > 0) {
						subjectMeasurement = d;
					}
				}
			}
			catch (ConversionException ce) {
				log.error(ce.getMessage());
			}
		}
		return subjectMeasurement;
	}
	
}

