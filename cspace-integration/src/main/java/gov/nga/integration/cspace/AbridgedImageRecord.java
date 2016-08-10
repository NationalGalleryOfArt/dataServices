package gov.nga.integration.cspace;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.Derivative;
import gov.nga.entities.art.Derivative.IMGFORMAT;
import gov.nga.utils.CollectionUtils;

// See ImageRecord for details of alignment between this implementation and Sirma's CS integration services implementation

@JsonPropertyOrder({ "namespace", "source", "id", "mimetype", "classification", "fingerprint", "viewType", "partner2ViewType", 
					 "width", "height", "title", "lastModified", "sequence", "filename", "description", 
					 "lightQuality", "spectrum", "productionDate", "viewDescription", "treatmentPhase", 
					 "references" })

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbridgedImageRecord extends Record implements NamespaceInterface {
	
	public static final String defaultNamespace = "image";
	public static final String defaultClassification = "image";
	
	private static boolean testmode = false;

	public enum PREDICATE {
		HASPRIMARYDEPICTION("hasPrimaryDepiction"),
		HASDEPICTION("hasDepiction"),
		PRIMARILYDEPICTS("primarilyDepicts"),
		DEPICTS("depicts"),
		RELATEDASSET("relatedAsset");
		
		private String label;
		public String getLabel() {
			if (testmode)
				return "partner2" + label;
			return label;
		}
		
		private PREDICATE(String label) {
			this.label = label;
		};
	};
	
	// TODO I really need to refactor all this so it works with the native objects rather than copy container
	private String 
		mimetype,		// optional field, but will probably be used in search header for NGA so need to include here
		classification,	// mandatory field
		title,			// mandatory field
		viewType,		// mandatory for publishedImages only - 
		partner2ViewType,
		treatmentPhase,
		spectrum,
		lightQuality,
		viewDescription,
		productionDate,
		description,
		filename;		// we should have this for the most part

	private Long 
		width,			// not specified in CS model, but probably will and would be used in search header for NGA so need to include here
		height;			// not specified in CS model, but probably will and would be used in search header for NGA so need to include here

	
	public AbridgedImageRecord(CSpaceImage d, boolean references, CSpaceTestModeService ts) throws InterruptedException, ExecutionException {
		testmode = ts.isTestModeOtherHalfObjects();

    	BeanUtils.copyProperties(d, this);

    	if (d.getViewType() != null) {
    		if (ts.isTestModeOtherHalfObjects())
    			partner2ViewType = d.getViewType().getLabel();
    		else
    			setViewType(d.getViewType().getLabel());
    	}

		setNamespace("image");
		setSource(d.getSource());
		setId(d.getImageID());
		setClassification(d.getClassification());
		setTitle(d.getTitle());
		setWidth(d.getWidth());
		setHeight(d.getHeight());

		if (references)
			setReferences(d,ts);
		
		// A BETTER WAY OF DETERMINING MIME-TYPES
		// String mimeType = Magic.getMagicMatch(file, false).getMimeType(); from the jMimeMagic library
		
		// AND A SOMEWHAT LESS RELIABLE METHOD
		// is = new BufferedInputStream(new FileInputStream(fileName));
		//  String mimeType = URLConnection.guessContentTypeFromStream(is);
		//  if(mimeType == null) {
		//    throw new IOException("can't get mime type of image");
		//  }
		IMGFORMAT imgFormat = d.getFormat();
		if (imgFormat != null) {
			setMimetype(imgFormat.getMimetype());
		}
		
		// TODO - might need to diverge here - the cataloged date is not necessarily what we're going for here 
		setLastModified(d.getCatalogued());
		setFingerprint(d.getCatalogued());
	}
	
	public String getPartner2ViewType() {
		return partner2ViewType;
	}

	public String getViewType() {
		return viewType;
	}

	private void setViewType(String viewType) {
		this.viewType = viewType;
	}
	
	public static String getDefaultNamespace() {
		return defaultNamespace;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}
	
	public Long getWidth() {
		return width;
	}

	public Long getHeight() {
		return height;
	}

	public void setHeight(Long height) {
		this.height = height;
	}

	public void setWidth(Long width) {
		this.width = width;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getViewDescription() {
		return viewDescription;
	}

	public void setViewDescription(String viewDescription) {
		this.viewDescription = viewDescription;
	}

	public String getLightQuality() {
		return lightQuality;
	}

	public void setLightQuality(String lightQuality) {
		this.lightQuality = lightQuality;
	}

	public String getSpectrum() {
		return spectrum;
	}

	public void setSpectrum(String spectrum) {
		this.spectrum = spectrum;
	}

	public String getTreatmentPhase() {
		return treatmentPhase;
	}

	public void setTreatmentPhase(String treatmentPhase) {
		this.treatmentPhase = treatmentPhase;
	}

	public String getProductionDate() {
		return productionDate;
	}

	public void setProductionDate(String productionDate) {
		this.productionDate = productionDate;
	}

	public void setReferences(CSpaceImage image, CSpaceTestModeService ts) throws InterruptedException, ExecutionException {
		List<Reference> rList = CollectionUtils.newArrayList();

		// ART OBJECT RELATIONSHIPS TO THIS IMAGE - WE DON'T CURRENTLY SUPPORT MULTIPLE ASSOCIATIONS BUT WE WILL PROBABLY
		// HAVE TO AT SOME POINT IN THE FUTURE
		ArtObject o = image.getArtObject();
		if (o != null) {
			PREDICATE p = PREDICATE.DEPICTS;
			// if this image is the primary image for the art object, then it's the primary depiction
			Derivative d = o.getZoomImage();
			if (d != null && image.getSource().equals(d.getSource()) && image.getImageID().equals(d.getImageID()))
				p = PREDICATE.PRIMARILYDEPICTS;
			AbridgedObjectRecord aor = new AbridgedObjectRecord(o,ts);
			rList.add(new Reference(p.getLabel(), aor));
		}

		if (rList.isEmpty())
			rList = null;

		setReferences(rList);
	}

}