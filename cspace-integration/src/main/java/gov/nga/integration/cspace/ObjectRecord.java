package gov.nga.integration.cspace;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.entities.art.OperatingModeService.OperatingMode;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.ArtObjectComponent;
import gov.nga.entities.art.Bibliography;
import gov.nga.entities.art.OperatingModeService;
import gov.nga.entities.art.Location;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.StringUtils;

/* according to: https://cs-dev.sirmaplatform.com/emf/service/integrations/cms/model
 * There's a chance this list is different based on classification, but NGA implementation has no instances where a mandatory field
 * is only available for a particular classification, so we just return all optional fields regardless of classification if they are set
 * mandatory
 * 	 cultObj:classification
 *   cultObj:id
 *   cultObj:accessionNum
 *   cultObj:artistNames
 *   cultObj:source
 * 
 * optional
 * 	 cultObj:title
 *   cultObj:description
 *   cultObj:subClassification
 *   cultObj:displayDate
 *   cultObj:medium
 *   cultObj:catalogueRaisonneRef
 *   cultObj:inscription
 *   cultObj:markings
 *   cultObj:dimensions
 *   cultObj:creditLine
 *   cultObj:location
 *   cultObj:provenanceText
 *   cultObj:portfolio
 *   cultObj:homeLocation
 *   cultObj:ownerNames
 *   cultObj:departmentAbbr
 *   cultObj:curatorialRemarks
 *   cultObj:bibliography
 *   cultObj:watermarks
 */

@JsonPropertyOrder({ "namespace", 	"source", 			"type", "id", 				"url", 				"accessionNum", 	"title", 		"classification", 
					 "artistNames", "lastModified",		"attribution", 		"subClassification", 
					 "displayDate", "medium",			"dimensions",		"departmentAbbr", 	"onView",
					 "location",	"homeLocation",		"current_location",	"ownerNames",  		"creditLine",		"description",	"inscription",
					 "markings",	"portfolio",		"overviewText",		"provenanceText",	"curatorialRemarks",
					 "watermarks",	"bibliography", 	"catalogueRaisonneRef",	"produced_by", "references" })

// TODO
/*Need to look for document from Guidekick
Need to add artist nationality
Need to break out the list of artists into an array and call it "artists" rather than artistnames
Need display date for art objects
add language back to specs
also look through my responses to john that are currently open in my inbox
*/

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectRecord extends AbridgedObjectRecord {

	//private static final Logger log = LoggerFactory.getLogger(ObjectRecord.class);
	
    // as far as the CSPACE API is concerned the following fields are ALL optional
    // and only appear with the unabridged version of the cultural object record
    private String attribution;			// including it regardless of whether part of CS integration or not
	private String subClassification;	
    private String displayDate;
    private String medium;
    private String dimensions;
    private String departmentAbbr;
    private String partner2departmentAbbr;
    private List<String> location;
    private List<String> homeLocation;
    private String ownerNames;
    private String creditLine;
    private String description;
    private String inscription;
    private String markings;
    private String portfolio;
    private String provenanceText;
    private String curatorialRemarks;
    private String watermarks;
    private String bibliography;
    private String catalogueRaisonneRef;
    private Boolean onView;
    
    public ObjectRecord(ArtObject o, Map<Long, Location> locs, OperatingModeService om, CSpaceTestModeService ts, List<CSpaceImage> images, String[] urlParts) throws InterruptedException, ExecutionException, MalformedURLException {
    	this(o, locs, true, om, ts, images, urlParts);
    }
       
    public ObjectRecord(ArtObject o, Map<Long, Location> locs, boolean references, OperatingModeService om, CSpaceTestModeService ts, List<CSpaceImage> images, String[] urlParts) throws InterruptedException, ExecutionException, MalformedURLException {
    	super(o, references, om, ts, images, urlParts);
    	if (o == null)
    		return;
    	if (!ts.isTestModeOtherHalfObjects())
    		setSubClassification(o.getSubClassification());
    	if ( om.getOperatingMode() == OperatingMode.PRIVATE )
    		this.ownerNames = constituentNames(o.getOwners(),"owner");
        setAttribution(o.getAttribution());
        setBibliography(o.getBibliography());
        setDisplayDate(o.getDisplayDate());
        setCreditLine(o.getCreditLine());
        setMedium(StringUtils.removeOnlyHTMLAndFormatting(o.getMedium()));
        setInscription(StringUtils.removeOnlyHTMLAndFormatting(o.getInscription()));
        setMarkings(StringUtils.removeOnlyHTMLAndFormatting(o.getMarkings()));
        setPortfolio(o.getPortfolio());
        setDimensions(o.getDimensions());
        setProvenanceText(StringUtils.removeOnlyHTMLAndFormatting(o.getProvenanceText()));
        if (ts.isTestModeOtherHalfObjects())
        	partner2departmentAbbr = o.getDepartmentAbbr();
        else
        	setDepartmentAbbr(o.getDepartmentAbbr());
        setDescription(StringUtils.removeOnlyHTMLAndFormatting(o.getDescription()));
        setCuratorialRemarks(StringUtils.removeOnlyHTMLAndFormatting(o.getCuratorialRemarks()));
        setWatermarks(o.getWatermarks());
        setCatalogueRaisonneRef(o.getCatalogRaisonneRef());
        setOnView(o.isOnView());
        setAllLocations(om, o.getComponents(), locs);
        setOverviewTextReference();
    }

	public String getSubClassification() {
		return subClassification;
	}

	public void setSubClassification(String subClassification) {
		this.subClassification = subClassification;
	}

	public String getOwnerNames() {
		return ownerNames;
	}

	private void addComponentLocationDesc(List<String> locationList, Location location, ArtObjectComponent component) {
		if (location != null && component != null) {
			locationList.add(component.getComponentNumber() + ": " + location.getDescription());
		}
	}

	public void setAllLocations(OperatingModeService om, List<ArtObjectComponent> components, Map<Long, Location> locations) {
		this.location = CollectionUtils.newArrayList();
		if ( om.getOperatingMode() == OperatingMode.PRIVATE )
			this.homeLocation = CollectionUtils.newArrayList();
		if (components != null && locations != null) {
			for (ArtObjectComponent c : components) {
				addComponentLocationDesc(this.location,locations.get(c.getLocationID()), c);
				if ( om.getOperatingMode() == OperatingMode.PRIVATE )
					addComponentLocationDesc(this.homeLocation,locations.get(c.getHomeLocationID()), c);
			}
		}
	}
	
	public String getAttribution() {
		return attribution;
	}

	public void setAttribution(String attribution) {
		this.attribution = attribution;
	}

	public String getBibliography() {
		return bibliography;
	}

	public void setBibliography(List<Bibliography> bList) {
		String bib=null;
		if (bList != null) {
			for (Bibliography b : bList) {
				if (bib != null)
					bib += '\n';
				else
					bib = "";
				bib += b.getCitation();
			}
		}
		this.bibliography = bib;
	}

	public String getDisplayDate() {
		return displayDate;
	}

	public void setDisplayDate(String displayDate) {
		this.displayDate = displayDate;
	}

	public String getCreditLine() {
		return creditLine;
	}

	public void setCreditLine(String creditLine) {
		this.creditLine = creditLine;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getLocation() {
		return location;
	}

	public List<String> getHomeLocation() {
		return homeLocation;
	}

	public String getMedium() {
		return medium;
	}

	public void setMedium(String medium) {
		this.medium = medium;
	}

	public String getInscription() {
		return inscription;
	}

	public void setInscription(String inscription) {
		this.inscription = inscription;
	}

	public String getMarkings() {
		return markings;
	}

	public void setMarkings(String markings) {
		this.markings = markings;
	}

	public String getPortfolio() {
		return portfolio;
	}

	public void setPortfolio(String portfolio) {
		this.portfolio = portfolio;
	}

	public String getDepartmentAbbr() {
		return departmentAbbr;
	}

	public String getPartner2departmentAbbr() {
		return partner2departmentAbbr;
	}

	public void setDepartmentAbbr(String departmentAbbr) {
		this.departmentAbbr = departmentAbbr;
	}

	public String getDimensions() {
		return dimensions;
	}

	public void setDimensions(String dimensions) {
		this.dimensions = dimensions;
	}

	public String getProvenanceText() {
		return provenanceText;
	}

	public void setProvenanceText(String provenance) {
		this.provenanceText = provenance;
	}

	public void setOverviewTextReference() {
		ArtObject o = getArtObject();
		if ( o == null || o.getOverviewText() == null)
			return;
		addReferredToBy(
			new LinkedArtLinguisticObject(getArtObject().getOverviewText(),
				// TODO - turn all of these classifications and possibly classification groups into predefined static objects or drive from a configuration
				// new LinkedArtClassification("http://vocab.getty.edu/page/aat/300026032", "abstracts (summaries)"),
				new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300080091", "description"),
				new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300418049", "brief texts") 
			)
		);
	}

	public String getCuratorialRemarks() {
		return curatorialRemarks;
	}

	public void setCuratorialRemarks(String curatorialRemarks) {
		this.curatorialRemarks = curatorialRemarks;
	}

	public String getWatermarks() {
		return watermarks;
	}

	public void setWatermarks(String watermarks) {
		this.watermarks = watermarks;
	}

	public Boolean getOnView() {
		return onView;
	}

	public void setOnView(Boolean onView) {
		this.onView = onView;
	}

	public String getCatalogueRaisonneRef() {
		return catalogueRaisonneRef;
	}

	public void setCatalogueRaisonneRef(String catalogueRaisonneNumber) {
		this.catalogueRaisonneRef = catalogueRaisonneNumber;
	}

}	

