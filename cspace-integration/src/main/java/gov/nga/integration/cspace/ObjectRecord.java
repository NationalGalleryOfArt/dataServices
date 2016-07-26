package gov.nga.integration.cspace;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.ArtObjectComponent;
import gov.nga.entities.art.Bibliography;
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

@JsonPropertyOrder({ "namespace", 	"source", 			"id", 				"accessionNum", 	"title", 		"classification", 
					 "artistNames", "lastModified",		"attribution", 		"subClassification", 
					 "displayDate", "medium",			"dimensions",		"departmentAbbr",
					 "location",	"homeLocation",		"ownerNames",  		"creditLine",		"description",	"inscription",
					 "markings",	"portfolio",		"provenanceText",	"curatorialRemarks","watermarks",	"bibliography",
					 "catalogueRaisonneRef",			"references" })
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
    
    public ObjectRecord(ArtObject o, Map<Long, Location> locs, CSpaceTestModeService ts, List<CSpaceImage> images) throws InterruptedException, ExecutionException {
    	this(o,locs,true, ts, images);
    }
       
    public ObjectRecord(ArtObject o, Map<Long, Location> locs, boolean references, CSpaceTestModeService ts, List<CSpaceImage> images) throws InterruptedException, ExecutionException {
    	super(o, references, ts, images);
    	if (o == null)
    		return;
    	if (!ts.isTestModeOtherHalfObjects())
    		setSubClassification(o.getSubClassification());
        this.ownerNames = constituentNames(o.getOwners());
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
        
        setAllLocations(o.getComponents(), locs);
        
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

	public void setAllLocations(List<ArtObjectComponent> components, Map<Long, Location> locations) {
		this.location = CollectionUtils.newArrayList();
		this.homeLocation = CollectionUtils.newArrayList();
		if (components != null && locations != null) {
			for (ArtObjectComponent c : components) {
				addComponentLocationDesc(this.location,locations.get(c.getLocationID()), c);
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

	public String getCatalogueRaisonneRef() {
		return catalogueRaisonneRef;
	}

	public void setCatalogueRaisonneRef(String catalogueRaisonneNumber) {
		this.catalogueRaisonneRef = catalogueRaisonneNumber;
	}

}	

/*


"record": {
		"namespace": "cultObj",
		"id": 1138,
		"classification": "painting",
		"title": "The Feast of the Gods",
		"artistNames": "Bellini, Giovanni (artist); Titian (artist)","artistNameList": ["Bellini, Giovanni", "Titian"],
		"attribution": "Giovanni Bellini and Titian",
		"accessionNumber": "1942.9 .1",
		"bibliographyList": ["Hartshorne, Rev.C.H.A Guide to Alnwick Castle.London, 1865: 62.", "<i> Paintings in the Collection of Joseph Widener at Lynnewood Hall. < /i> Intro. by Wilhelm R. Valentiner. Elkins Park, Pennsylvania, 1923: unpaginated, repro., as by Giovanni Bellini."],
		"displayDate": "1514 / 1529",
		"creditLine": "Widener Collection",
		"description": "The Feast of the Gods, Giovanni Bellini and Titian, 1514 / 1529",
		"location": [" 1942.9.1 – West Building Main Floor Gallery 12"],"WB - M12",
		"homeLocation": [" 1942.9.1 – West Building Main Floor Gallery 12"],"WB - M12",
		"inscription": "lower right on wooden tub: joannes bellinus venetus / p MDXIIII",
		"medium": "oil on canvas",
		"departmentAbbr": "DCRS",
		"dimensions": "overall: 170.2 x 188 cm(67 x 74 in .) \n framed: 203.8 x 218.4 x 7.6 cm(80 1 / 4 x 86 x 3 in .)",
		"provenance": "Probably commissioned by Alfonso I d 'Este, Duke of Ferrara [d. 1534);[1] by inheritance to his son, Ercole II d'Este, Duke of Ferrara[d .1559];by inheritance to his son, Alfonso II d 'Este, Duke of Ferrara [d. 1597]; by inheritance to his cousin, Cesare d' Este...",
		"references": [
			{	"predicate": "hasPrimaryDepiction",
				"object": {
					"namespace": "image",
					"id": "d63a8ffd-bdac-498c-b861-a53e11989cef"
				}
			},
			{	"predicate": "hasChild",
				"object": {
					"namespace": "cultObj",
					"id": "1234",
					"title": "",
"classification": "frame"
				}
			}
		]
}


*/