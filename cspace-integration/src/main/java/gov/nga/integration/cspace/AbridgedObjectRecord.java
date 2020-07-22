/*
    NGA ART DATA API: AbridgedObjectRecord provides for the JSON bean container for the shortened 
	art object records returned in references and via search results.   

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.common.entities.art.ExhibitionArtObject;
import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.ArtObjectAssociation;
import gov.nga.entities.art.ArtObjectConstituent;
import gov.nga.entities.art.ArtObjectImage;
import gov.nga.entities.art.Media;
import gov.nga.entities.art.OperatingModeService;
import gov.nga.entities.art.Place;
import gov.nga.integration.cspace.ArtObjectPredicates.ARTOBJECTPREDICATES;
import gov.nga.integration.cspace.MediaPredicates.MEDIAPREDICATES;
import gov.nga.integration.cspace.imageproviders.WebImage;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.StringUtils;

// SEE notes in ObjectRecord for notes about alignment of this representation with Sirma's CS integration services implementation
@JsonPropertyOrder({ "type", "namespace", 	"source", 		"id", 				"url",  "isNGA",				
	 "accessionNum", 	"title", 		"classification",   "department",    "dexIDs",
	 "artistNames", "lastModified",		"attribution", 		"subClassification", 
	 "displayDate", "medium",			"dimensions",		"departmentAbbr", 	"onView",
	 "location",	"homeLocation",		"current_location",	"ownerNames",  		"creditLine",		"description",	"inscription",
	 "markings",	"portfolio",		"overviewText",		"provenanceText",	"curatorialRemarks",
	 "watermarks",	"bibliography", 	"catalogueRaisonneRef",	"produced_by", "references" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbridgedObjectRecord extends LinkedArtRecord implements NamespaceInterface {

	private static final Logger log = LoggerFactory.getLogger(AbridgedObjectRecord.class);
	
	private static final String defaultNamespace = "cultObj";

	// mandatory fields of the API - source and id are also mandatory and are inherited from the base object Record
    private String accessionNum;				// mandatory field
	private String title;						// optional field, but searchable, so including it in abridged object used in search results
	private String classification;				// mandatory field for cspace
    private String artistNames;					// mandatory field for cspace
    private PlaceRecord current_location;		// the place where this object is located
    //below are fields required by IRIS
    private Boolean isNGA;
    private String medium;
    private String dimensions;
    private String displayDate;
    private String department;
    private List<String> dexIDs;
    
    @JsonIgnore
    private ArtObject artObject;

    
	public AbridgedObjectRecord(ArtObject o, OperatingModeService om, CSpaceTestModeService ts, String[] urlParts) throws InterruptedException, ExecutionException, MalformedURLException {
		this(o, false, om, ts, null, urlParts, Collections.emptyMap());
	}
	
	public AbridgedObjectRecord(ArtObject o, boolean references, OperatingModeService om, CSpaceTestModeService ts, 
			List<CSpaceImage> images, String[] urlParts, final Map<Long, List<ExhibitionArtObject>> exhibitionMap) throws InterruptedException, ExecutionException, MalformedURLException {
		super("ManMadeObject");
		if (o == null)
			return;

		setArtObject(o);
		setNamespace(defaultNamespace);
		setSource("tms");
		setId(o.getObjectID().toString());
        setClassification(o.getClassification());
		setAccessionNum(o.getAccessionNum());
        setTitle(StringUtils.removeOnlyHTMLAndFormatting(o.getTitle()));
        setLastModified(o.getLastDetectedModification());

        this.artistNames = constituentNames(o.getArtists(),"artist");
        
		URL objectURL = null;
		
		try {
			if (urlParts[2] != null)
				objectURL = new URL(urlParts[0], urlParts[1], Integer.parseInt(urlParts[2]),"/art/tms/objects/"+o.getObjectID()+".json");
			else
				objectURL = new URL(urlParts[0], urlParts[1], "/art/tms/objects/"+o.getObjectID()+".json");
			setUrl(objectURL);
		}
		catch (MalformedURLException me) {
			log.error("Problem creating object URL: " + me.getMessage());
		}

		setCurrent_location(o);

		if (references) {
			setReferences(o, om, ts, images, urlParts);
			setMediaReferences();
		}
		
		if (om.getOperatingMode() == OperatingModeService.OperatingMode.PRIVATE) {
			setMedium(o.getMedium());
			setDimensions(o.getDimensions());
			setDisplayDate(o.getDisplayDate());
			setDepartment(o.getDepartmentAbbr());
			//setLocation(new PlaceRecord(o.getCurrentLocation().getLocationInfo());
			final List<String> dexIDS = CollectionUtils.newArrayList();
			if (exhibitionMap.size() > 0) {
				for(Map.Entry<Long, List<ExhibitionArtObject>> candExh: exhibitionMap.entrySet()) {
					for (ExhibitionArtObject obj: candExh.getValue()) {
						if (obj.getArtObjectID().equals(o.getObjectID())) {
							String dexid = obj.getDexID();
							try {
								dexid = String.format("%03d", Long.parseLong(dexid));
							}
							catch (final Exception err) {
								//do nothing
							}
							dexIDS.add(String.format("%d-%s", candExh.getKey(), dexid));
							break;
						}
					}
				}
			}
			if (dexIDS.size() > 0) {
				setDexIDs(dexIDS);
			}
			
			switch (o.getTMSStatus())
			{
				case ONDEPOSIT:
				case ONLOAN:
				case PREACCESSIONED:
				case ACCESSIONED:
				case DEACCESSIONED:
					setIsNGA(true);
					break;	
				default:
					setIsNGA(false);
			}
			
		}

		
	}
	
	private void setArtObject(ArtObject artObject) {
		this.artObject = artObject;
	}
	
	public ArtObject getArtObject() {
		return this.artObject;
	}

	public static String getDefaultNamespace() {
		return defaultNamespace;
	}

	public String getArtistNames() {
		return artistNames;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		if (classification != null) {
			if ( classification.startsWith("Index of American Design") )
				classification = "Drawing";
			else if ( classification.startsWith("Ephemera") )
				classification = "Ephemera";
		}
		this.classification = classification;
	}
	
	public ProductionRecord getProduced_by() {
		return new ProductionRecord(this,"produced_by");
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAccessionNum() {
		return accessionNum;
	}

	public void setAccessionNum(String accessionNum) {
		this.accessionNum = accessionNum;
	}

//	public URL getUrl() {
//		return super.getUrl();
//	}

	public String constituentNames(List<ArtObjectConstituent> constituents, String filterRoleType) {
		String cNames=null;
		if (constituents != null) {
			for (ArtObjectConstituent aoc : constituents) {
				String cName = aoc.getConstituent().getForwardDisplayName();
				String roleType = aoc.getRoleType();
				String role = aoc.getRole();
				log.trace("Role: " + role);
				log.trace("Role type: " + roleType);
				/* DCL has discussed limiting the roletypes and / or roles returned by artistNames - personally I think that's a mistake but
				 * the jury is still out
				 */
				/* if ( cName != null && !StringUtils.isNullOrEmpty(roleType) && roleType.toLowerCase().equals("artist") 
					 && !StringUtils.isNullOrEmpty(role) && role.toLowerCase().equals("artist")) {
					log.info("HERE HER HE H");
					cNames += cName;
					if (cNames != null)
						cNames += "; ";
					else
						cNames = "";
					
				 */	
				if ( cName != null && !StringUtils.isNullOrEmpty(roleType) && roleType.toLowerCase().equals(filterRoleType) 
						&& !StringUtils.isNullOrEmpty(role) ) {
					if (cNames != null)
						cNames += "; ";
					else
						cNames = "";
					cNames += cName;
					if (role != null)
						cNames += " (" + role + ")"; 
				}
			}
		}
		return cNames;
	}
	
	public void setReferences(ArtObject o, OperatingModeService om, CSpaceTestModeService ts, List<CSpaceImage> images, String[] urlParts) throws InterruptedException, ExecutionException, MalformedURLException {
		List<Reference> rList = CollectionUtils.newArrayList();
		// first, go through the related objects
		ArtObjectAssociation aop = o.getParentAssociation();
		if (aop != null) {
			AbridgedObjectRecord aor = new AbridgedObjectRecord(aop.getAssociatedArtObject(), false, om, ts, images, urlParts, Collections.emptyMap());
			rList.add(new Reference(ARTOBJECTPREDICATES.HASPARENT.getLabels(), aor));
		}
		List<ArtObjectAssociation> l = o.getChildAssociations();
		if (l != null) {
			for (ArtObjectAssociation aoc : l) {
				if (aoc != null) {
					AbridgedObjectRecord aor = new AbridgedObjectRecord(aoc.getAssociatedArtObject(), false, om, ts, images, urlParts, Collections.emptyMap());
					rList.add(new Reference(ARTOBJECTPREDICATES.HASCHILD.getLabels(), aor));
				}
			}
		}
		l = o.getSiblingAssociations();
		if (l!= null) {
			for (ArtObjectAssociation aos : l) {
				if (aos != null) {
					AbridgedObjectRecord aor = new AbridgedObjectRecord(aos.getAssociatedArtObject(), false, om, ts, images, urlParts, Collections.emptyMap());
					rList.add(new Reference(ARTOBJECTPREDICATES.HASSIBLING.getLabels(), aor));
				}
			}		
		}
		
		// iterate through the images associated with this art object
		if (images != null) {
			for (CSpaceImage ci : images) {
				WebImage wi = WebImage.factory(ci, ts);
				AbridgedImageRecord air = new AbridgedImageRecord(wi,false,om,ts,urlParts);
				addRepresentation(air);
				if (ArtObjectImage.isPrimaryView(wi)) {
					rList.add(new Reference(MEDIAPREDICATES.HASPRIMARYDEPICTION.getLabels(), air));
				}
				// we don't want to return cropped images associated with this object
				else if (!wi.isCropped()) {
					rList.add(new Reference(MEDIAPREDICATES.HASDEPICTION.getLabels(), air));
				}
			}
		}
		
		if (rList.size() <= 0)
			rList = null;

		setReferences(rList);
	}
	
	public void setCurrent_location(ArtObject o) {
		if (o.getLocation() != null && o.getLocation().getLocationInfo().getPlace() != null)
			this.current_location = new PlaceRecord((Place) o.getLocation().getLocationInfo().getPlace());
	}
	
	public PlaceRecord getCurrent_location() {
		return current_location;
	}
	
	public void setMediaReferences() {
		// iterate through media that's related to this art object
		List<Media> mediaList = getArtObject().getRelatedMedia();
		if (mediaList != null) {
			for ( Media m : mediaList ) {
				if ( m != null) {
					LinkedArtInformationObject ir = new MediaRecord(m);
					addReferredToBy(ir);
				}
			}
		}
	}

	public Boolean getIsNGA() {
		return isNGA;
	}

	private void setIsNGA(Boolean isNGA) {
		this.isNGA = isNGA;
	}

	public String getMedium() {
		return medium;
	}

	public void setMedium(String medium) {
		this.medium = medium;
	}

	public String getDimensions() {
		return dimensions;
	}

	private void setDimensions(String dimensions) {
		this.dimensions = dimensions;
	}

	public String getDisplayDate() {
		return displayDate;
	}

	private void setDisplayDate(String displayDate) {
		this.displayDate = displayDate;
	}

	public String getDepartment() {
		return department;
	}

	private void setDepartment(String department) {
		this.department = department;
	}

	public List<String> getDexIDs() {
		return dexIDs;
	}

	private void setDexIDs(List<String> dexIDs) {
		this.dexIDs = dexIDs;
	}

	private void setCurrent_location(PlaceRecord current_location) {
		this.current_location = current_location;
	}

}

 