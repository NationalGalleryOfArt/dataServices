/*
    NGA ART DATA API: MediaRecord is the JSON bean representing a media item

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
package gov.nga.integration.cspace;



import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.common.entities.art.Media;
import gov.nga.common.entities.art.Media.MEDIATYPE;


@JsonPropertyOrder( { 
	"type", "id", "label", "format", "conforms_to", "classified_as", "identified_by", "referred_to_by"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaRecord extends LinkedArtInformationObject {

	// mandatory fields of the API - source and id are also mandatory and are inherited from the base object Record
	private Media media;
	
	public MediaRecord(Media media)  {
		if (media == null)
			return;
		setMedia(media);
		setLabel(media.getTitle());
		setId("https://api.nga.gov/media/"+media.getStreamingProvider()+"/"+media.getMediaType()+"/"+media.getMediaID());
		addIdentity(
			new LinkedArtName(media.getTitle(),
				new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300404670", "preferred terms")
			)
		);
		
		addIdentity(
			new LinkedArtIdentifier(media.getMediaID().toString(),
				new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300404012", "mediaID")
			)
		);
		

		// add the 
		if (media.getMediaTypeEnum() != null) {
			MEDIATYPE m = media.getMediaTypeEnum();
			addClassifiedAs(new LinkedArtClassifiedType(m.getLodID(),m.getLodLabel()));
		}
		
		addReferredToBy(
			new LinkedArtLinguisticObject(media.getDescription(),
				new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300080091", "description"),
				new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300418049", "brief texts")
			)
		);
		
		// search through all the tags and add the equivalent Getty AAT if one exists
		String[] tags = media.getTags();
		if ( tags != null) {
			for (String t : tags) {
				if (t.equals("ngaweb:audio-video/collection-audio-tour"))
					addClassifiedAs(new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300259161", "audioguide"));
			}
			
		}
		
		addRepresentations();
		
	}

	private void setMedia(Media media) {
		this.media = media;
	}

	public Long getMediaID() {
		if (media != null)
			return media.getMediaID();
		return null;
	}

	@JsonIgnore
	public String getLanguage() {
		if (media != null)
			return media.getLanguage();
		return null;
	}
	
	@JsonProperty("language")
	public LinkedArtLanguage getLanguageAsType() {
		String l = getLanguage();
		if (l == null)
			return null;
		
		if (l.equals("en")) return new LinkedArtLanguage("http://vocab.getty.edu/aat/300388277","English");
		if (l.equals("fr")) return new LinkedArtLanguage("http://vocab.getty.edu/aat/300388306","French");
		if (l.equals("es")) return new LinkedArtLanguage("http://vocab.getty.edu/aat/300389311","Spanish");
		if (l.equals("ru")) return new LinkedArtLanguage("http://vocab.getty.edu/aat/300389168","Russian");
		if (l.equals("ja")) return new LinkedArtLanguage("http://vocab.getty.edu/aat/300388486","Japanese");
		if (l.equals("zh")) return new LinkedArtLanguage("http://vocab.getty.edu/aat/300388127","Mandarin");
		return null;
	}

	public String getTitle() {
		if (media != null)
			return media.getTitle();
		return null;
	}

	public String getDescription() {
		if (media != null)
			return media.getDescription();
		return null;
	}
	
	public LinkedArtBaseClass getConforms_to() {
		LinkedArtRecord conforms = new LinkedArtRecord(null);
		conforms.setId(media.getMediaTypeEnum().getProvider());
		return conforms;
	}

	public Long getDuration() {
		if (media != null)
			return media.getDuration();
		return null;
	}

	public String getFormat() {
		if (media != null && media.getMediaTypeEnum() != null)
			return media.getMediaTypeEnum().getMimeType();
		return null;
	}
	
	public String getStreamingProvider() {
		if (media != null) {
			switch (media.getMediaTypeEnum()) {
			case AUDIO : return "soundcloud";
			case VIDEO : return "brightcove";
			}
		}
		return null;
	}

	public String getThumbnailURL() {
		if (media != null)
			return media.getThumbnailURL();
		return null;
	}

/*	 "carries": [
	             {
	               "id": "https://linked.art/example/info/0", 
	               "type": "InformationObject", 
	               "about": [
	                 {
	                   "id": "https://linked.art/example/Type/0", 
	                   "type": "Type", 
	                   "label": "Religious, Devotional"
	                 }
	               ]
	             }
	           ], 
	*/ 
	/*public String[] getCarries() {
		if ( media != null && media.getKeywords() != null )  {
			LinkedArtInformationObject io = new LinkedArtInformationObject();
			for (String k : media.getKeywords()) {
				LinkedArtType t = new LinkedArtType(k,null);
				t.setId("https://api.nga.gov/);
				io.addAbout(new LinkedArtType(k, "Type", , label)
			}
			
			io.addAbout(l);
			io.addClassifiedAs(c);
		}
		return null;
	}
	*/

	public String[] getTags() {
		if (media != null) 
			return media.getTags();
		return null;
	}

	
	// must become
	 /*"representation": [
	                    {
	                      "id": "http://example.org/images/image.jpg", 
	                      "type": "VisualItem", 
	                      "label": "Image of Painting", 
	                      "classified_as": [
	                        {"id": "aat:300215302","type": "Type","label": "Digital Image"}
	                      ], 
	                      "format": "image/jpeg"
	                    }
	                  ]
	   */             		

	// thumbnail images are "small" whereas regular images are "large"
	// IIIF images might be best characterized as "full-size" although they are of course, a service, not a specific size
	// small is: http://vocab.getty.edu/aat/300073768
	// large is: http://vocab.getty.edu/aat/300073756
	public void addRepresentations() {
		if ( media == null )
			return;
		if (media.getImageURL() != null) {
			addRepresentation(
					new LinkedArtVisualItem(media.getImageURL(), "large title frame", 
							new LinkedArtClassifiedType("Type",  "digital images", "http://vocab.getty.edu/aat/300215302"),
							new LinkedArtClassifiedType("Type",  "large", "http://vocab.getty.edu/aat/300073756")
					)
			);
		}
		if ( media.getThumbnailURL() != null ) {
			addRepresentation(
					new LinkedArtVisualItem(media.getThumbnailURL(), "small title frame", 
							new LinkedArtClassifiedType("Type",  "digital images", "http://vocab.getty.edu/aat/300215302"),
							new LinkedArtClassifiedType("Type",  "small", "http://vocab.getty.edu/aat/300073768")
					)
			);
		}
	}

	public String getPlayURL() {
		if (media != null) 
			return media.getPlayURL();
		return null;
	}

	public String getPresentationDate() {
		if (media != null) 
			return media.getPresentationDate();
		return null;
	}

	public String getReleaseDate() {
		if (media != null) 
			return media.getReleaseDate();
		return null;
	}

	@JsonIgnore
	public String getTranscript() {
		if (media != null) 
			return media.getTranscript();
		return null;
	}

	public String getLastModified() {
		if (media != null) 
			return media.getLastModified();
		return null;
	}

}

