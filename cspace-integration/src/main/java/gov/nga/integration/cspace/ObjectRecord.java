package gov.nga.integration.cspace;

import java.util.List;
import java.util.Map;

import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.ArtObjectAssociation;
import gov.nga.entities.art.ArtObjectComponent;
import gov.nga.entities.art.ArtObjectConstituent;
import gov.nga.entities.art.ArtObjectImage;
import gov.nga.entities.art.Bibliography;
import gov.nga.entities.art.Derivative;
import gov.nga.entities.art.Location;
import gov.nga.utils.CollectionUtils;

public class ObjectRecord extends AbridgedObjectRecord {

    private String classification;
    private String title;
    private String artistNames;
    private String attribution;
    private String accessionNumber;
    private String bibliography;
    private String displayDate;
    private String creditLine;
    private String description;
    private List<String> locations;
    private List<String> homeLocations;
    private String medium;
    private String departmentAbbr;
    private String dimensions;
    private String provenance;
    private List<Reference> references;
    
    public ObjectRecord(ArtObject o, Map<Long, Location> locs) {
    	super(o);
    	if (o == null)
    		return;
    	setNamespace("cultObj");
        setId(o.getObjectID().toString());
        setClassification(o.getClassification());
        setTitle(o.getTitle());
        setArtistNames(o.getArtists());
        setAttribution(o.getAttribution());
        setAccessionNumber(o.getAccessionNum());
        setBibliography(o.getBibliography());
        setDisplayDate(o.getDisplayDate());
        setCreditLine(o.getCreditLine());
        setMedium(o.getMedium());
        setDimensions(o.getDimensions());
        setProvenance(o.getProvenanceText());
        setReferences(o);
        setDepartmentAbbr(o.getDepartmentAbbr());
        setDescription(o.getDescription());
        setAllLocations(o.getComponents(), locs);
        
        // TODO - make the TMS data manager configurable to run in a certain mode via configs and 
        // SQL params and setting would be contingent upon private or public extract
          
    }

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
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

	public void setArtistNames(List<ArtObjectConstituent> artists) {
		String aNames=null;
		if (artists != null) {
			for (ArtObjectConstituent aoc : artists) {
				String cName = aoc.getConstituent().getForwardDisplayName();
				String roleType = aoc.getRoleType();
				if (cName != null) {
					if (aNames != null)
						aNames += "; ";
					else
						aNames = "";
					aNames += cName;
					if (roleType != null)
						aNames += " (" + roleType + ")";
				}
			}
		}
		this.artistNames = aNames;
	}
	
	private void addComponentLocationDesc(List<String> locationList, Location location, ArtObjectComponent component) {
		if (location != null && component != null) {
			locationList.add(component.getComponentNumber() + ": " + location.getDescription());
		}
	}

	public void setAllLocations(List<ArtObjectComponent> components, Map<Long, Location> locations) {
		this.locations = CollectionUtils.newArrayList();
		this.homeLocations = CollectionUtils.newArrayList();
		if (components != null && locations != null) {
			for (ArtObjectComponent c : components) {
				addComponentLocationDesc(this.locations,locations.get(c.getLocationID()), c);
				addComponentLocationDesc(this.homeLocations,locations.get(c.getHomeLocationID()), c);
			}
		}
	}

	public String getAttribution() {
		return attribution;
	}

	public void setAttribution(String attribution) {
		this.attribution = attribution;
	}

	public String getAccessionNumber() {
		return accessionNumber;
	}

	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
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

	public List<String> getLocations() {
		return locations;
	}

	public List<String> getHomeLocations() {
		return homeLocations;
	}

	public String getMedium() {
		return medium;
	}

	public void setMedium(String medium) {
		this.medium = medium;
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

	public String getProvenance() {
		return provenance;
	}

	public void setProvenance(String provenance) {
		this.provenance = provenance;
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