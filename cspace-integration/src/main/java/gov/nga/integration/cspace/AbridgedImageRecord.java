package gov.nga.integration.cspace;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.ArtObjectAssociation;
import gov.nga.entities.art.Derivative;
import gov.nga.entities.art.Derivative.IMGFORMAT;
import gov.nga.entities.art.Derivative.IMGVIEWTYPE;
import gov.nga.utils.CollectionUtils;

@JsonPropertyOrder({ "namespace", "source", "id", "mimetype", "classification", "width", "height", "title", "lastModified", "references" })
public class AbridgedImageRecord extends Record implements NamespaceInterface {
	
	public static final String defaultNamespace = "image";
	public static final String defaultClassification = "image";

	public enum PREDICATE {
		HASPRIMARYDEPICTION("hasPrimaryDepiction"),
		HASDEPICTION("hasDepiction"),
		PRIMARILYDEPICTS("primarilyDepicts"),
		DEPICTS("depicts"),
		RELATEDASSET("relatedAsset");
		
		private String label;
		public String getLabel() {
			return label;
		}
		
		private PREDICATE(String label) {
			this.label = label;
		};
	};
	
	private String mimetype;
	private String classification;
	private String title;
	private Long width;
	private Long height;
	
	public AbridgedImageRecord(Derivative d) {
		this(d,true);
	}
	
	public AbridgedImageRecord(Derivative d, boolean references) {
		setNamespace("image");
		setSource("web-images-repository");
		setId(d.getImageID());
		setClassification(defaultClassification);
		setTitle(d);
		setWidth(d.getWidth());
		setHeight(d.getHeight());

		if (references)
			setReferences(d);
		
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
		
		setLastModified(d.getCatalogued());
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

	public void setTitle(Derivative d) {
		String title = d.getSourceImageURI().toString();
		if (d.getObject() != null) {
			title = String.format("%s of %s [%s #%s, %dx%d])", 
				d.getFormat().getMimetype(), d.getObject().getAccessionNum(), 
				d.getViewType().getLabel(), d.getSequence(), 
				d.getWidth(), d.getHeight());
		}
		this.title = title;
	}
	
	public void setReferences(Derivative primaryDerivative) {
		List<Reference> rList = CollectionUtils.newArrayList();

		// ART OBJECT RELATIONSHIPS TO THIS IMAGE - WE DON'T CURRENTLY SUPPORT MULTIPLE ASSOCIATIONS BUT WE WILL PROBABLY
		// HAVE TO AT SOME POINT IN THE FUTURE
		ArtObject o = primaryDerivative.getObject();
		if (o != null) {
			PREDICATE p = PREDICATE.DEPICTS;
			// if this image is the primary image for the art object, then it's the primary depiction
			if (primaryDerivative.equals(o.getZoomImage()))
				p = PREDICATE.PRIMARILYDEPICTS;
			AbridgedObjectRecord aor = new AbridgedObjectRecord(o,false);
			rList.add(new Reference(p.getLabel(), aor));

			// RELATED ASSETS WHICH ARE NOT THE PRIMARY DERIVATIVE AND ASSOCIATED WITH THE PRIMARY ART OBJECT OR 
			// THE PARENT OR CHILD OBJECTS OF THIS ART OBJECT, BUT NOT SIBLINGS
			List<ArtObject> relatedObjects = CollectionUtils.newArrayList();
			relatedObjects.add(o);
			ArtObjectAssociation op = o.getParentAssociation();
			if (op != null && op.getAssociatedArtObject() != null)
				relatedObjects.add(op.getAssociatedArtObject());

			List<ArtObjectAssociation> children = o.getChildAssociations();
			if (children != null) {
				for (ArtObjectAssociation cp : children) {
					if (cp != null && cp.getAssociatedArtObject() != null)
						relatedObjects.add(cp.getAssociatedArtObject());
				}
			}

			for (ArtObject ro : relatedObjects) {
				// OTHER ASSETS RELATED TO THIS OBJECT ARE CONSIDERED TO BE RELATED TO THE PRIMARY DERIVATIVE
				for (Derivative d : ro.getLargestImages(IMGVIEWTYPE.allViewTypesExcept(IMGVIEWTYPE.CROPPED))) {
					// if we're not iterating through the images of the object directly associated with the primary derivative
					// or if we are then the primary derivative is not the one we're looking at 
					if (!primaryDerivative.equals(d)) {
						AbridgedImageRecord air = new AbridgedImageRecord(d,false);
						rList.add(new Reference(PREDICATE.RELATEDASSET.getLabel(), air));
					}
				}
			}
		}

		if (rList.isEmpty())
			rList = null;

		setReferences(rList);
	}

}