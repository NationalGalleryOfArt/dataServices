package gov.nga.integration.cspace;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.Derivative;
import gov.nga.entities.art.Derivative.IMGFORMAT;
import gov.nga.utils.CollectionUtils;

// See ImageRecord for details of alignment between this implementation and Sirma's CS integration services implementation

@JsonPropertyOrder({ "namespace", "source", "id", "mimetype", "classification", "width", "height", "title", "lastModified", "references" })
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
	
	private String mimetype;		// optional field, but will probably be used in search header for NGA so need to include here
	private String classification;	// mandatory field
	private String title;			// mandatory field
	private Long width;				// not specified in CS model, but probably will and would be used in search header for NGA so need to include here
	private Long height;			// not specified in CS model, but probably will and would be used in search header for NGA so need to include here
	
//	public AbridgedImageRecord(CSpaceImage d) {
//		this(d,true);
//	}
	
	public AbridgedImageRecord(CSpaceImage d, boolean references, CSpaceTestModeService ts) {
		testmode = ts.isTestModeOtherHalfObjects();
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
		
		// TODO - might need to diverge here - the catalogued date is not necessarily what we're going for here 
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

	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setReferences(CSpaceImage image, CSpaceTestModeService ts) {
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
			AbridgedObjectRecord aor = new AbridgedObjectRecord(o,false,ts);
			rList.add(new Reference(p.getLabel(), aor));
		}

		if (rList.isEmpty())
			rList = null;

		setReferences(rList);

//			ONLY USED FOR TESTING RELATED ASSETS - NGA DOESN'T ACTUALLY HAVE ANY RELATED ASSETS

//			// RELATED ASSETS WHICH ARE NOT THE PRIMARY DERIVATIVE AND ASSOCIATED WITH THE PRIMARY ART OBJECT OR 
//			// THE PARENT OR CHILD OBJECTS OF THIS ART OBJECT, BUT NOT SIBLINGS
//			List<ArtObject> relatedObjects = CollectionUtils.newArrayList();
//			relatedObjects.add(o);
//			ArtObjectAssociation op = o.getParentAssociation();
//			if (op != null && op.getAssociatedArtObject() != null)
//				relatedObjects.add(op.getAssociatedArtObject());
//
//			List<ArtObjectAssociation> children = o.getChildAssociations();
//			if (children != null) {
//				for (ArtObjectAssociation cp : children) {
//					if (cp != null && cp.getAssociatedArtObject() != null)
//						relatedObjects.add(cp.getAssociatedArtObject());
//				}
//			}
//
//			for (ArtObject ro : relatedObjects) {
//				// OTHER ASSETS RELATED TO THIS OBJECT ARE CONSIDERED TO BE RELATED TO THE PRIMARY DERIVATIVE
//				for (Derivative d : ro.getLargestImages(IMGVIEWTYPE.allViewTypesExcept(IMGVIEWTYPE.CROPPED))) {
//					// if we're not iterating through the images of the object directly associated with the primary derivative
//					// or if we are then the primary derivative is not the one we're looking at 
//					if (!primaryImage.equals(d)) {
//						if (d != null) {
//							WebImage wi = WebImage.factory(d);
//							AbridgedImageRecord air = new AbridgedImageRecord(wi,false);
//							rList.add(new Reference(PREDICATE.RELATEDASSET.getLabel(), air));
//						}
//					}
//				}
//			}
	}

}