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

import gov.nga.search.Faceted;
import gov.nga.search.SearchFilter;
import gov.nga.search.Searchable;
import gov.nga.search.SortHelper;
import gov.nga.search.SortOrder;
import gov.nga.search.Sortable;
import gov.nga.search.Sorter;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.DateUtils;
import gov.nga.utils.TypeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Media extends SupplementingEntityImpl implements Searchable, Sortable, Faceted {
	
	public enum MEDIATYPE {
		AUDIO("audio","audio/mpeg", "http://vocab.getty.edu/aat/300312047", "mp3 audio", "https://api.soundcloud.com"),
		VIDEO("video","video/mp4",	"http://vocab.getty.edu/aat/300312050", "mp4 video", "https://api.brightcove.com");
		
		private String label = null;
		private String mimeType = null;
		private String lodID = null;
		private String lodLabel = null;
		private String provider = null;
		private MEDIATYPE(String label, String mimeType, String lodID, String lodLabel, String provider) {
			this.label = label;
			this.mimeType = mimeType;
			this.lodID = lodID;
			this.lodLabel = lodLabel;
			this.provider = provider;
		}
		public String getLabel() {
			return label;
		}
		public String getMimeType() {
			return mimeType;
		}
		public String getLodID() {
			return lodID;
		}
		public String getLodLabel() {
			return lodLabel;
		}
		public String getProvider() {
			return provider;
		}
		public static MEDIATYPE fromLabel(String label) {
			for (MEDIATYPE m : MEDIATYPE.values()) {
				if ( label.equals(m.getLabel()) )
					return m;
			}
			return null;
		}
	}
	
	// media search fields
	public static enum SEARCH {
		MEDIAID,
		MEDIALANGUAGE,
		PRESENTATIONDATE,
		RELEASEDATE,
		MEDIATYPE
	}

	public static enum SORT {
		MEDIAID_ASC,
		MEDIAID_DESC,
		MEDIALANGUAGE_ASC,
		MEDIALANGUAGE_DESC,
		MEDIATYPE_ASC,
		MEDIATYPE_DESC,
		RELEASEDATE_ASC,
		RELEASEDATE_DESC,
		PRESENTATIONDATE_ASC,
		PRESENTATIONDATE_DESC,
		LASTMODIFIED_ASC,
		LASTMODIFIED_DESC
	}


    public static final String defaultSource = "www.nga.gov";

    public Media(ArtDataManagerService manager) {
        super(manager,null);
    }

    static final String fetchAllMediaQuery = 
        "SELECT m.mediaID,  	m.mediaType, 	m.title, " + 
        "       m.description,	m.duration, m.language, " +
        "		m.presentationDate, m.releaseDate, m.lastModified, " +
        "		m.imageURL, m.thumbnailURL, m.playURL, " +
        "       m.keywords,		m.tags,			m.transcript  " +
        "FROM data.media_items m";

    protected String getAllMediaQuery() {
        return fetchAllMediaQuery;
    }
    
    public Media(
            ArtDataManagerService manager, 
            Long mediaID, String mediaType, String title, 
            String description, Long duration, String language, 
            String presentationDate, String releaseDate, String lastModified, 
            String imageURL, String thumbnailURL, String playURL,  
            String keywords, String tags, String transcript ) throws SQLException  {
        
        this(manager);
        setSource(defaultSource);
        setMediaID(mediaID);
        setMediaType(mediaType);
        setTitle(title);
        setDescription(description);
        setDuration(duration);
        setLanguage(language);
        setPresentationDate(presentationDate);
        setReleaseDate(releaseDate);
        setLastModified(lastModified);
        setImageURL(imageURL);
        setThumbnailURL(thumbnailURL);
        setPlayURL(playURL);
        setKeywords(keywords);
        setTags(tags);
        setTranscript(transcript);
    }

    public Media(ArtDataManagerService manager, ResultSet rs) throws SQLException  {
    	
        this(
                manager, 
                rs.getLong(1), 
                rs.getString(2), 
                rs.getString(3), 
                rs.getString(4),
                TypeUtils.getLong(rs, 5),
                rs.getString(6),
                DateUtils.formatDate(DateUtils.DATE_FORMAT_ISO_8601_WITH_TIME_AND_TZ_CORRECT, rs.getTimestamp(7)),
                DateUtils.formatDate(DateUtils.DATE_FORMAT_ISO_8601_WITH_TIME_AND_TZ_CORRECT, rs.getTimestamp(8)),
                DateUtils.formatDate(DateUtils.DATE_FORMAT_ISO_8601_WITH_TIME_AND_TZ_CORRECT, rs.getTimestamp(9)),
                rs.getString(10),
                rs.getString(11),
                rs.getString(12),
                rs.getString(13),
                rs.getString(14),
                rs.getString(15)
        );
    }

    public Media factory(ResultSet rs) throws SQLException {
        Media d = new Media(getManager(),rs);
        return d; 
    }
    
	private String source = null;
	private void setSource(String source) {
		this.source = source;
	}
	public String getSource() {
		return this.source;
	}

	private Long mediaID = null;
	private void setMediaID(Long mediaID) {
		this.mediaID = mediaID;
	}
	public Long getMediaID() {
		return this.mediaID;
	}
	
	private String language = null;
	private void setLanguage(String language) {
		this.language = language;
	}
	public String getLanguage() {
		return this.language;
	}
	
	private String title = null;
	private void setTitle(String title) {
		this.title = title;
	}
	public String getTitle() {
		return this.title;
	}
	
	private String description = null;
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}

	private Long duration = null;
	private void setDuration(Long duration) {
		this.duration = duration;
	}
	public Long getDuration() {
		return duration;
	}

	private MEDIATYPE mediaType = null;
    private void setMediaType(String mediaType) {
		this.mediaType = MEDIATYPE.fromLabel(mediaType);
	}
	public MEDIATYPE getMediaTypeEnum() {
		return mediaType;
	}
    public String getMediaType() {
    	return getMediaTypeEnum().getLabel();
    }

    String thumbnailURL = null;
    public String getThumbnailURL() {
		return thumbnailURL;
	}

	public void setThumbnailURL(String thumbnailURL) {
		this.thumbnailURL = thumbnailURL;
	}

	String keywords = null;
    public String[] getKeywords() {
    	if (keywords == null)
    		return null;
		return keywords.split(",\\s*");
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	String tags = null;
    public String[] getTags() {
    	if (tags == null)
    		return null;
		return tags.split(",\\s*");
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	String imageURL = null;
    public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	String playURL = null;
    public String getPlayURL() {
		return playURL;
	}
	public void setPlayURL(String playURL) {
		this.playURL = playURL;
	}

    public String getStreamingProvider() {
    	switch (getMediaTypeEnum()) {
    	case AUDIO : return "soundcloud";
    	case VIDEO : return "brightcove";
    	default : return null;
    	}
	}

	String presentationDate = null;
    public String getPresentationDate() {
		return presentationDate;
	}

	public void setPresentationDate(String presentationDate) {
		this.presentationDate = presentationDate;
	}

	String releaseDate = null;
    public String getReleaseDate() {
		return releaseDate;
	}
    public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	String lastModified = null;
    public String getLastModified() {
		return lastModified;
	}
    public void setLastModified(String lastModified) {
		this.lastModified= lastModified;
	}

	String transcript = null;
	public String getTranscript() {
		return transcript;
	}
	public void setTranscript(String transcript) {
		this.transcript = transcript;
	}

	@Override
	public Long matchesAspect(Object ao, Object order) {
		// we currently don't have a need to match one media item with another so this
		// will remain unimplemented
		return null;
	}

	@Override
	// used for sorting media or for deferring sorting to an ArtObject if one is associated with the media item and 
	// ArtObject sort criteria happen to be in use for the particular query
	public int aspectScore(Object od, Object order, String matchString) {
		Media m = (Media) od;
		if (m == null || order == null)
			return Sorter.NULL;

		if ( order instanceof SORT ) {
			switch ((SORT) order) {
			case LASTMODIFIED_ASC:
				return SortHelper.compareObjects(getLastModified(), m.getLastModified());
			case LASTMODIFIED_DESC:
				return SortHelper.compareObjects(m.getLastModified(), getLastModified());
			case RELEASEDATE_ASC:
				return SortHelper.compareObjects(getReleaseDate(), m.getReleaseDate());
			case RELEASEDATE_DESC:
				return SortHelper.compareObjects(m.getReleaseDate(), getReleaseDate());
			case PRESENTATIONDATE_ASC:
				return SortHelper.compareObjects(getPresentationDate(), m.getPresentationDate());
			case PRESENTATIONDATE_DESC:
				return SortHelper.compareObjects(m.getPresentationDate(), getPresentationDate());
			case MEDIAID_ASC:
				return SortHelper.compareObjects(getMediaID(), m.getMediaID());
			case MEDIAID_DESC:
				return SortHelper.compareObjects(m.getMediaID(), getMediaID());
			case MEDIALANGUAGE_ASC:
				return SortHelper.compareObjects(getLanguage(), m.getLanguage());
			case MEDIALANGUAGE_DESC:
				return SortHelper.compareObjects(m.getLanguage(), getLanguage());
			case MEDIATYPE_ASC:
				return SortHelper.compareObjects(getMediaType(), m.getMediaType());
			case MEDIATYPE_DESC:
				return SortHelper.compareObjects(m.getMediaType(), getMediaType());

			}
		}
		else if ( order instanceof ArtObject.SORT) {
			ArtObject o1 = getArtObject();
			ArtObject o2 = m.getArtObject();
			// if objects are null or the same object
			if (o1 == o2)
				return Sorter.NULL;
			if (o1 == null)
				return 1;
			if (o2 == null)
				return -1;
			return o1.aspectScore(o2, order, matchString);
		}

		return Sorter.NULL;
	}

	@Override
	public SortOrder getDefaultSortOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortOrder getNaturalSortOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean matchesFilter(SearchFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    // no facets for art object images... at least not yet
	public List<String> getFacetValue(Object f) {
		return CollectionUtils.newArrayList();
	}
	
}

