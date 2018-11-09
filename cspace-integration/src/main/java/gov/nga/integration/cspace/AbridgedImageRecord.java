package gov.nga.integration.cspace;


import java.net.MalformedURLException;

import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.OperatingModeService;
import gov.nga.entities.art.Derivative;
import gov.nga.entities.art.Derivative.IMGFORMAT;
import gov.nga.integration.cspace.MediaPredicates.MEDIAPREDICATES;
import gov.nga.utils.CollectionUtils;


// See ImageRecord for details of alignment between this implementation and Sirma's CS integration services implementation
@JsonPropertyOrder({ "type", "format", "conforms_to", "iiifURL", "classified_as", "referred_to_by", 
	 "namespace", "source", "id", "mimetype", "classification", "url", "fingerprint", 
	 "width", "height", "title", "lastModified", 
	 "viewType", "partner2ViewType", "sequence", "filename", "description", "subjectWidthCM", "subjectHeightCM", 
	 "originalSource", "originalSourceType", "originalFilename", "projectDescription", "lightQuality",
	 "spectrum", "captureDevice", "originalSourceInstitution", "photographer", "productionDate", "creator", 
	 "viewDescription", "treatmentPhase", "productType", "references" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbridgedImageRecord extends LinkedArtVisualItem implements NamespaceInterface {

	public static final String defaultNamespace = "image";
	public static final String defaultClassification = "image";
	
	private static final Logger log = LoggerFactory.getLogger(AbridgedImageRecord.class);
		
	// TODO I really need to refactor all this so it works with the native objects rather than copy container
	private String 
		mimetype,		// optional field, but will probably be used in search header for NGA so need to include here
		classification,	// mandatory field
		title,			// mandatory field
		viewType,		// mandatory for publishedImages only -
		sequence,		// needed for GuideKick
		partner2ViewType,
		treatmentPhase,
		spectrum,
		lightQuality,
		viewDescription,
		productionDate,
		description,
		filename;		// we should have this for the most part
	
	private URL iiifURL;
	
	private Long 
		width,			// not specified in CS model, but probably will and would be used in search header for NGA so need to include here
		height;			// not specified in CS model, but probably will and would be used in search header for NGA so need to include here

	
	private Derivative derivative = null;
	
	public AbridgedImageRecord(CSpaceImage d, boolean references, OperatingModeService om, CSpaceTestModeService ts, String[] urlParts) throws InterruptedException, ExecutionException, MalformedURLException {
		if ( d == null)
			return;
		
		this.derivative = d;

    	BeanUtils.copyProperties(d, this);

    	if (d.getViewType() != null) {
    		if (ts.isTestModeOtherHalfObjects())
    			partner2ViewType = d.getViewType().getLabel();
    		else
    			setViewType(d.getViewType().getLabel());
    	}
    	
    	setSequence(d.getSequence());

		setNamespace("image");
		setSource(d.getSource());
		setId(d.getImageID());
		setClassification(d.getClassification());
		setTitle(d.getTitle());
		setWidth(d.getWidth());
		setHeight(d.getHeight());

		if (references)
			setReferences(d,om,ts,urlParts);
		
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
		
		URL objectURL = null;
		
		try {
			if (urlParts[2] != null)
				objectURL = new URL(urlParts[0], urlParts[1], Integer.parseInt(urlParts[2]),"/media/" + d.getSource() + "/images/" + d.getImageID() + ".json");
			else
				objectURL = new URL(urlParts[0], urlParts[1], "/art/" + d.getSource() + "/objects/" + d.getImageID() + ".json");
			setUrl(objectURL);
		}
		catch (MalformedURLException me) {
			log.error("Problem creating object URL: " + me.getMessage());
		}
		
		setIIIFImageAPIURL(d);
		
		addClassifiedAs(new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300215302","digital images"));
		
		switch (derivative.getViewType()) {
		case PRIMARY : 
			addClassifiedAs(new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300404450", "primary (general designation)"));
			addClassifiedAs(new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300210700", "visible spectrum"));
			break;
		case ALTERNATE:
			addClassifiedAs(new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300034364", "auxilliary views" ));
			addClassifiedAs(new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300210700", "visible spectrum" ));
			break;
		case COMPFIG:
			addClassifiedAs(new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300210700", "visible spectrum" ));
			break;
		case CROPPED:
			addClassifiedAs(new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300015551", "partial views" ));
			addClassifiedAs(new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300210700", "visible spectrum"));
			break;
		case INSCRIPTION:
			addClassifiedAs(new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300028702", "inscriptions"));
			addClassifiedAs(new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300210700", "visible spectrum"));
			break;
		case TECHNICAL:
			addClassifiedAs(new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300179535", "spectroscopy"));
			break;
		default:
			addClassifiedAs(new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300210700", "visible spectrum"));
			break;
		
		}
	
		if (getIIIFUrl() != null) {
			addClassifiedAs(new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300404413", "full-size"));
			addConforms_to(
				new LinkedArtType("http://iiif.io/api/image", "IIIF Image")
			);
		}
		
		LinkedArtInformationObject io = new LinkedArtInformationObject();
		io.setLabel("sequence");
		io.addClassifiedAs(
				new LinkedArtClassifiedType("Type", "sequences", "http://vocab.getty.edu/aat/300192339")
		);
		io.setValue(derivative.getSequence());
		addReferredToBy(io);
		
		// this.addRepresentation(v);
		
	}
	
	public String getPartner2ViewType() {
		return partner2ViewType;
	}
	
//	  "representation": [
//	                     {
//	                       "id": "http://iiif.example.org/image/1", 
//	                       "type": "VisualItem", 
//	                       "label": "IIIF Image API for Sculpture", 
//	                       "conforms_to": {"id": "http://iiif.io/api/image"}
//	                     }
//	                   ]
	public void populate() {

		
	}
	
	@JsonProperty("iiifURL")
	public URL getIIIFUrl() {
		return this.iiifURL;
		
		/*String serverScheme = cs.getString(Derivative.imagingServerSchemePropertyName);
		String serverURL = cs.getString(Derivative.imagingServerURLPropertyName);
		String publicIIP = cs.getString(IIIFAuthConfigs.iipPublicPrefixPropertyName);
		String privateIIP = cs.getString(IIIFAuthConfigs.iipPrivatePrefixPropertyName);
		String proxyURL = String.format("%s:%s%s?%s", serverScheme, serverURL, request.getRequestURI().replaceAll(publicIIP,  privateIIP), request.getQueryString());

		return defaultClassification.
		*/
	}
	
	private void setIIIFImageAPIURL(CSpaceImage d) throws MalformedURLException {
		//d.getProtocolRelativeiiifURL();
		this.iiifURL = d.getIIIFBaseURL();
	}

	public String getViewType() {
		return viewType;
	}

	private void setViewType(String viewType) {
		this.viewType = viewType;
	}
	
	public String getSequence() {
		return sequence;
	}

	private void setSequence(String sequence) {
		this.sequence = sequence;
	}
	
	public static String getDefaultNamespace() {
		return defaultNamespace;
	}
	
	public String getFormat() {
		return mimetype;
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

	public void setReferences(CSpaceImage image, OperatingModeService om, CSpaceTestModeService ts, String[] urlParts) throws InterruptedException, ExecutionException, MalformedURLException {
		List<Reference> rList = CollectionUtils.newArrayList();

		// ART OBJECT RELATIONSHIPS TO THIS IMAGE - WE DON'T CURRENTLY SUPPORT MULTIPLE ASSOCIATIONS BUT WE WILL PROBABLY
		// HAVE TO AT SOME POINT IN THE FUTURE
		ArtObject o = image.getArtObject();
		if (o != null) {
			MEDIAPREDICATES p = MEDIAPREDICATES.DEPICTS;
			// if this image is the primary image for the art object, then it's the primary depiction
			Derivative d = o.getZoomImage();
			if (d != null && image.getSource().equals(d.getSource()) && image.getImageID().equals(d.getImageID()))
				p = MEDIAPREDICATES.PRIMARILYDEPICTS;
			AbridgedObjectRecord aor = new AbridgedObjectRecord(o,om,ts,urlParts);
			rList.add(new Reference(p.getLabels(), aor));
		}

		if (rList.isEmpty())
			rList = null;

		setReferences(rList);
	}

}