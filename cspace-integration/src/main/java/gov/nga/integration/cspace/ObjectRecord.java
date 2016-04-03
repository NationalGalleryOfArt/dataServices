package gov.nga.integration.cspace;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;

import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.ArtObjectAssociation;
import gov.nga.entities.art.ArtObjectComponent;
import gov.nga.entities.art.ArtObjectConstituent;
import gov.nga.entities.art.ArtObjectImage;
import gov.nga.entities.art.Bibliography;
import gov.nga.entities.art.Derivative;
import gov.nga.entities.art.Location;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.StringUtils;


// TODO need to refactor this to have a single record container surrounding it.  Pretty easy since everything can extend a centralized record and the container can just encapsulate that
public class ObjectRecord extends AbridgedObjectRecord {

	private static final Logger log = LoggerFactory.getLogger(ObjectRecord.class);
	
    private String classification;
    private String subClassification;
    private String title;
    private String artistNames;
    private String ownerNames;
    private String attribution;
    private String accessionNum;
    private String bibliography;
    private String displayDate;
    private String creditLine;
    private String description;
    private List<String> location;
    private List<String> homeLocation;
    private String medium;
    private String inscription;
    private String markings;
    private String portfolio;
    private String departmentAbbr;
    private String dimensions;
    private String provenanceText;
    private String curatorialRemarks;
    private String watermarks;
    private String catalogueRaisonneRef;
    
    // if null, don't include it since we have an option in the search APIs to not include references at all - so we don't want to 
    // make it appear as if there are no references - we just won't include them at all if they are null
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Reference> references;
    
    public ObjectRecord(ArtObject o, Map<Long, Location> locs) {
    	this(o,locs,true);
    }
       
    public ObjectRecord(ArtObject o, Map<Long, Location> locs, boolean includeReferences) {
    	super(o);
    	if (o == null)
    		return;
    	
    	setNamespace("cultObj");
        setId(o.getObjectID().toString());
        setClassification(o.getClassification());
        setSubClassification(o.getSubClassification());
        setTitle(StringUtils.removeMarkup(o.getTitle()));
        this.artistNames = constituentNames(o.getArtists());
        this.ownerNames = constituentNames(o.getOwners());
        setAttribution(o.getAttribution());
        setAccessionNum(o.getAccessionNum());
        setBibliography(o.getBibliography());
        setDisplayDate(o.getDisplayDate());
        setCreditLine(o.getCreditLine());
        setMedium(StringUtils.removeMarkup(o.getMedium()));
        setInscription(StringUtils.removeMarkup(o.getInscription()));
        setMarkings(StringUtils.removeMarkup(o.getMarkings()));
        setPortfolio(o.getPortfolio());
        setDimensions(o.getDimensions());
        setProvenanceText(StringUtils.removeMarkup(o.getProvenanceText()));
        if (includeReferences)
        	setReferences(o);
        setDepartmentAbbr(o.getDepartmentAbbr());
        setDescription(StringUtils.removeMarkup(o.getDescription()));
        setCuratorialRemarks(StringUtils.removeMarkup(o.getCuratorialRemarks()));
        setWatermarks(o.getWatermarks());
        setCatalogueRaisonneRef(o.getCatalogRaisonneRef());
        
        setAllLocations(o.getComponents(), locs);
        
    }

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public String getSubClassification() {
		return subClassification;
	}

	public void setSubClassification(String subClassification) {
		this.subClassification = subClassification;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtistNames() {
		return artistNames;
	}

	public String getOwnerNames() {
		return ownerNames;
	}

	public String constituentNames(List<ArtObjectConstituent> constituents) {
		String cNames=null;
		if (constituents != null) {
			for (ArtObjectConstituent aoc : constituents) {
				String cName = aoc.getConstituent().getForwardDisplayName();
				String roleType = aoc.getRoleType();
				if (cName != null) {
					if (cNames != null)
						cNames += "; ";
					else
						cNames = "";
					cNames += cName;
					if (roleType != null)
						cNames += " (" + roleType + ")";
				}
			}
		}
		return cNames;
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
				log.info("==========================" + c.getLocationID() + "===============================");
				log.info("==========================" + c.getHomeLocationID() + "===============================");
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

	public String getAccessionNum() {
		return accessionNum;
	}

	public void setAccessionNum(String accessionNum) {
		this.accessionNum = accessionNum;
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

	public List<Reference> getReferences() {
		return references;
	}

	public void setReferences(ArtObject o) {
		
		List<Reference> rList = CollectionUtils.newArrayList();
		// first we go through the related objects
		ArtObjectAssociation aop = o.getParentAssociation();
		if (aop != null) {
			AbridgedObjectRecord aor = new AbridgedObjectRecord(aop.getAssociatedArtObject());
			rList.add(new Reference(AbridgedObjectRecord.PREDICATE.HASPARENT.getLabel(), aor));
		}
		List<ArtObjectAssociation> l = o.getChildAssociations();
		if (l != null) {
			for (ArtObjectAssociation aoc : l) {
				if (aoc != null) {
					AbridgedObjectRecord aor = new AbridgedObjectRecord(aoc.getAssociatedArtObject());
					rList.add(new Reference(AbridgedObjectRecord.PREDICATE.HASCHILD.getLabel(), aor));
				}
			}
		}
		l = o.getSiblingAssociations();
		if (l!= null) {
			for (ArtObjectAssociation aos : l) {
				if (aos != null) {
					AbridgedObjectRecord aor = new AbridgedObjectRecord(aos.getAssociatedArtObject());
					rList.add(new Reference(AbridgedObjectRecord.PREDICATE.HASSIBLING.getLabel(), aor));
				}
			}		
		}
		// then we go through the largest images associated with this work and return them as associated images
		for (Derivative d : o.getLargestImages()) {
			AbridgedImageRecord air = new AbridgedImageRecord(d);
			if (ArtObjectImage.isPrimaryView(d)) {
				rList.add(new Reference(AbridgedImageRecord.PREDICATE.HASPRIMARYDEPICTION.getLabel(), air));
			}
			else {
				rList.add(new Reference(AbridgedImageRecord.PREDICATE.HASDEPICTION.getLabel(), air));
			}
		}
		
		if (rList.size() <= 0)
			rList = null;
		
		this.references = rList;
		//this.references = rList.toArray(new Reference[rList.size()]);
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