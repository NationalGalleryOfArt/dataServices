package gov.nga.entities.art;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.CollationKey;
import java.util.*;

import javax.measure.Measure;
import javax.measure.quantity.Quantity;

import gov.nga.utils.stringfilter.StringFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.entities.art.ArtObjectAssociation.ARTOBJECTRELATIONSHIPROLE;
import gov.nga.entities.art.ArtObjectAssociation;
import gov.nga.entities.art.ArtObjectDimension.DIMENSION_TYPE;
import gov.nga.entities.art.ArtObjectHistoricalData.HISTORICAL_DATA_TYPE;
import gov.nga.entities.art.ArtObjectTerm.TERMTYPES;
import gov.nga.entities.art.Derivative.IMAGECLASS;
import gov.nga.entities.art.Derivative.IMGVIEWTYPE;
import gov.nga.entities.art.Derivative.ImgSearchOpts;
import gov.nga.entities.art.TextEntry.TEXT_ENTRY_TYPE;
import gov.nga.search.Faceted;
import gov.nga.search.SearchFilter;
import gov.nga.search.Searchable;
import gov.nga.search.SortHelper;
import gov.nga.search.SortOrder;
import gov.nga.search.Sortable;
import gov.nga.search.Sorter;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.DateUtils;
import gov.nga.utils.MutableInt;
import gov.nga.utils.StringUtils;
import gov.nga.utils.TypeUtils;

import static gov.nga.utils.StringUtils.htmlToMarkdown;
import static gov.nga.utils.StringUtils.sanitizeHtml;

public class ArtObject extends ArtEntityImpl implements Searchable, Sortable, Faceted {

	private static final Logger log = LoggerFactory.getLogger(ArtObject.class);

	static public final String JCRBasePath = "/content/nga/tms/ArtObject";

	// art object facets fields
	public static enum FACET {
		VISUALBROWSERTIMESPAN,
		VISUALBROWSERNATIONALITY,
		VISUALBROWSERTHEME,
		VISUALBROWSERSTYLE,
		VISUALBROWSERCLASSIFICATION,
		SCHOOL,
		ONVIEW,
		HASLARGERIMAGERY,
		OSCICATALOGUE
	}

	// art object search fields
	public static enum SEARCH {
		ARTIST_DISPLAYNAME,
		ARTIST_ALLNAMES,
		OWNER_ALLNAMES,
		HASLARGERIMAGERY,
		HASTHUMBNAIL,
		ONVIEW,
		TITLE,
		YEARS_SPAN,
		YEARS_BEGIN,
		MEDIUM,
		OBJECTID,
		ACCESSIONNUM,
		ATTRIBUTION_INV,
		PROVENANCE,
		CREDITLINE,
		LOCATION_ID,
		LOCATION_SITE,
		LOCATION_ROOM,
		LOCATION_UNITPOSITION,
		LOCATION_DESCRIPTION,
		DONORCONSTITUENTID,
		OWNERCONSTITUENTID,
		SUBCLASSIFICATION,
		VISUALBROWSERTIMESPAN,
		VISUALBROWSERTHEME,
		VISUALBROWSERSCHOOL,
		VISUALBROWSERSTYLE,
		VISUALBROWSERNATIONALITY,
		VISUALBROWSERCLASSIFICATION, 
		VISUALBROWSERTHEMEORKEYWORD,
		NATIONALITY,
		LASTDETECTEDMODIFICATION
	}

	// free text searches
	public static enum FREETEXTSEARCH {
		ALLDATAFIELDS,
		CREDITLINE,
		MEDIUM,
		PROVENANCETEXT,
		DIMENSIONS,
		INSCRIPTION,
		MARKINGS,
		CATALOGRAISONNEREF,
		IMAGECOPYRIGHT,
		ARTISTS,
		ARTISTNATIONALITIES,
		OWNERS,
		DONORS,
		TERMS,
		BIBLIOGRAPHYTEXT,
		OVERVIEWTEXT,
		CONSERVATIONNOTES,
		SYSCATTEXT,
		EXHIBITIONHISTORY
	}

	public static enum SORT {

		// only for comparing two art objects
		YEAR_ASC,                               // sorts by year
		CLASSIFICATION_ASC,                     // sorts by classification alphabetically
		ATTRIBUTIONINV_ASC,                     // sorts by inverted attribution alphabetically
		TITLE_ASC,                              // sorts by title alphabetically
		TITLE_DESC,                              // sorts by title alphabetically
		OBJECTID_ASC,                           // sorts by object id numerically
		OBJECTID_DESC,                           // sorts by object id numerically
		HASLARGERIMAGERY_DESC,                  // sorts based on whether object has image restrictions
		HASTHUMBNAIL_DESC,                      // sorts based on whether a thumbnail image is available
		ONVIEW_DESC,                            // sorts based on whether object is on view or not
		NUMARTISTS_ASC,                         // sorts based on number of artist associations in ascending order
		ATTRIBUTIONINV_ARTISTNAME_MATCH_ASC,    // sorts based on whether the inverted attribution exactly matches
												// the preferred display name of an associated artist
		ACCESSIONNUM_ASC,                       // added for searches using an accession number
		ACCESSIONNUM_DESC,                       // added for searches using an accession number
		VISUALBROWSERCLASSIFICATION_CUSTOM,     // john's custom classification sort
		LASTDETECTEDMODIFICATION_ASC,
		LASTDETECTEDMODIFICATION_DESC,
		FIRST_ARTIST_ASC,
		FIRST_ARTIST_DESC,
		
		// only for comparing two art objects against a third base art object ( set using SortHelper.setBaseEntity() )
		YEAR_MATCH,                             // sorts a vs. b based on matching each to the year of object c
		CLASSIFICATION_MATCH,                   // "" but for classification
		ATTRIBUTIONINV_MATCH,                   // "" but for inverted attribution
		TITLE_MATCH,                            // "" but for title
		ONVIEW_MATCH,                           // "" but for on view status
		OBJECTID_MATCH,                         // "" but for object id match (probably no use cases for this)
		HASLARGERIMAGERY_MATCH,                 // "" but for same image restriction status
		HASTHUMBNAIL_MATCH,                     // "" but for same thumbnail status
		ARTISTS_MATCH,                          // sorts a vs. b based on whether artists match that of object c
		NUMARTISTSINCOMMON_MATCH_DESC,          // sorts a vs. b based on number of artists in common with object c
		NUMTHEMESINCOMMON_MATCH_DESC,           // "" but for themes
		NUMSTYLESINCOMMON_MATCH_DESC,           // "" but for styles
		NUMDONORSINCOMMON_MATCH_DESC,           // "" but for donors
		NUMARTISTNATIONALITIESINCOMMON_MATCH_DESC   // "" but for nationalities of artists
		

	}


	protected static final String baseConstituentsQuery = 
			ArtObjectConstituent.fetchAllObjectsConstituentsQuery +
			"WHERE oc.objectID @@ "; 

	protected static final String baseTermsQuery =
			ArtObjectTerm.fetchAllObjectTermsQuery + " WHERE ot.objectID @@ ";

	// this is far too inefficient for searches and sorts - plus it removes all diacritics unnecessarily
	//protected static StringFilter removeMarkupFilter = new RemoveMarkupFilter();

	// NOTES ABOUT CONCURRENCY
	// In the artobjectmanager and constituentmanager classes, we make sure
	// to block all calls for data access until after the TMS data is loaded
	// in memory by the OSGi bundle.  We also take the approach of using copies
	// of arraylists when we return lists of items to the caller.  That ensures
	// we never have to worry about concurrency problems.

	protected ArtObject(ArtDataManagerService manager) {
		super(manager);
	}

	protected static final String fetchAllObjectsQuery = 
			"SELECT fingerprint, objectID, accessioned, accessionNum, locationID, " +
					"       title, displayDate, beginYear, endYear, visualBrowserTimespan, " +
					"       attributionInverted, attribution," +
					"       creditLine, isIAD, " +
					"       classification, subClassification, visualBrowserClassification, " +
					"       canShowImagery, parentID, thumbnailsProhibited, maxDerivativeExtent, " +
					"       medium, provenanceText, " +
					"       objectLeonardoID, dimensions, inscription, " +
					"       markings, catalogRaisonneRef, imageCopyright, " +
					"       oldAccessionNum, zoomPermissionGranted, " + 
					"       ngaimages.TMSObjectID AS downloadID, isVirtual, departmentAbbr, " + 
					"		description, portfolio, curatorialRemarks, watermarks, lastDetectedModification, isPublic " +
					"FROM data.objects " +
					"LEFT JOIN data.objects_ngaimages_status ngaimages ON ngaimages.TMSObjectID = objectID "
//					"LEFT JOIN data.objects_ngaimages_status ngaimages ON ngaimages.TMSObjectID = objectID WHERE objectID < 5000 "
					;
					//+ "WHERE objectid = 1046 ";

	protected static final String briefObjectQuery = 
			fetchAllObjectsQuery + " WHERE objectID @@ "; 

	// create an art object from an existing result set row
	public ArtObject(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		super(manager,                TypeUtils.getLong(rs, 1));
		objectID                    = TypeUtils.getLong(rs, 2);
		accessioned                 = TypeUtils.getLong(rs, 3);
		accessionNum                = rs.getString(4);
		locationID                  = TypeUtils.getLong(rs, 5);
		title                       = htmlToMarkdown(rs.getString(6));
		displayDate                 = rs.getString(7);
		beginYear                   = TypeUtils.getLong(rs, 8);
		endYear                     = TypeUtils.getLong(rs, 9);
		visualBrowserTimeSpan       = rs.getString(10);
		attributionInverted         = rs.getString(11);
		attribution                 = rs.getString(12);
		creditLine                  = rs.getString(13);
		isIAD                       = TypeUtils.getLong(rs, 14);
		classification              = rs.getString(15);
		subClassification           = rs.getString(16);
		visualBrowserClassification = rs.getString(17);
		canShowImagery              = TypeUtils.getLong(rs, 18);
		parentID                    = TypeUtils.getLong(rs, 19);
		thumbnailsProhibited        = TypeUtils.getLong(rs, 20);
		maxDerivativeExtent         = TypeUtils.getLong(rs, 21);
		medium                      = htmlToMarkdown(sanitizeHtml(rs.getString(22)));
		provenanceText              = htmlToMarkdown(sanitizeHtml(rs.getString(23)));

		// the following were originally details, but not any longer
		objectLeonardoID            = rs.getString(24);
		dimensionsDesc              = rs.getString(25);
		inscription                 = htmlToMarkdown(sanitizeHtml(rs.getString(26)));
		markings                    = htmlToMarkdown(sanitizeHtml(rs.getString(27)));
		catalogRaisonneRef          = rs.getString(28);
		imageCopyright              = rs.getString(29);
		oldAccessionNum             = rs.getString(30);
		zoomPermissionGranted       = TypeUtils.getLong(rs, 31);
		Long downloadID             = TypeUtils.getLong(rs, 32);
		downloadAvailable           = (downloadID == null || !downloadID.equals(objectID)) ? Long.valueOf(0) : Long.valueOf(1);
		virtual       				= TypeUtils.getLong(rs, 33);
		departmentAbbr				= rs.getString(34);
		description					= htmlToMarkdown(sanitizeHtml(rs.getString(35)));
		portfolio					= rs.getString(36);
		curatorialRemarks			= htmlToMarkdown(sanitizeHtml(rs.getString(37)));
		watermarks					= rs.getString(38);
		// TODO consider making below a timestamp rather than a string for faster comparisons
		// although this will require a number of new filter tests in search filter I think
		lastDetectedModification	= DateUtils.formatDate(DateUtils.DATE_FORMAT_ISO_8601_WITH_TIME_AND_TZ, rs.getTimestamp(39));
		isPublic					= TypeUtils.getLong(rs, 40);
	
		// pre-compute commonly used collation keys to speed up sort routines
		formatFreeTitle				= StringUtils.removeOnlyHTMLAndFormatting(getTitle());
		formatFreeMedium			= StringUtils.removeOnlyHTMLAndFormatting(getMedium());
		strippedTitleCKey 			= StringUtils.getDefaultCollator().getCollationKey(stripLeadingArticle(StringUtils.removeDiacritics(getTitle())));
		attributionInvertedCKey 	= StringUtils.getDefaultCollator().getCollationKey(StringUtils.removeDiacritics(attributionInverted));
	}

	private String stripLeadingArticle(String text)	{
		if ( text == null ) 
			return null;
		//text = text.replaceAll("/[^\\w\\s]|_/g", "").replaceAll("/\\s+/g", " ").replaceAll("^(The|the|A|a|An|an) ", "");
		//return text;
		text = text.replaceAll("[^a-zA-Z0-9\\s]", "");
		return text.replaceAll("^(The|the|A|a|An|an) ", "").trim();
	}


	// copy constructor used by classes extending ArtObject and adding addition fields
	// on top of an existing art object instance - composition might be a better approach here
	// although the transparency is pretty nice as well - depending on how things go, we might
	// want to change approaches in the future, but for now this is clean and works fine
	// there were some advantages to extending from ArtObject, particularly with the search
	// implementation, so it could make sense to just carry the original object by composition and
	// override the methods to provide a passthrough to the object stored as part of the composition
	// although that approach potentially requires an awfully large number of pass through methods
	// if we instead stored the properties in a bean that could be set and passed separately as a chunk
	// of data from one class to another, that might make it easier to work with - have to think more 
	// about the best pattern for this 
	protected ArtObject(ArtObject source) throws SQLException {
		super(source.getManager(), source.getFingerprint());

		// don't fall into the reflection trap in an effort to make this easier, at least with 
		// java 1.7 and below as it's 1000 times slower in a reflection loop with Field assignment
		// than it is to just assign the members directly.
		this.accessioned                = source.accessioned;
		this.accessionNum               = source.accessionNum;
		this.allDimensions			 	= source.allDimensions;
		this.artists 				 	= source.artists;
		this.attributionInverted        = source.attributionInverted;
		this.attribution                = source.attribution;
		this.beginYear                  = source.beginYear;
		this.canShowImagery             = source.canShowImagery;
		this.catalogRaisonneRef         = source.catalogRaisonneRef;
		this.parentChildAssociations	= source.parentChildAssociations; 
		this.classification             = source.classification;
		this.constituents 			 	= source.constituents;
		this.creditLine                 = source.creditLine;
		this.dimensionsDesc             = source.dimensionsDesc;
		this.displayArtists 			= source.displayArtists;
		this.displayDate                = source.displayDate;
		this.endYear                    = source.endYear;
		this.donors 					= source.donors; 
		this.downloadAvailable 			= source.downloadAvailable;
		this.historicalData 			= source.historicalData;
		this.images 					= source.images; 
		this.isAttributionInvertedAnArtistName = source.isAttributionInvertedAnArtistName;
		this.isIAD 						= source.isIAD;
		this.imageCopyright             = source.imageCopyright;
		this.inscription                = source.inscription;
		this.isIAD                      = source.isIAD;
		this.location 					= source.location;
		this.locationID                 = source.locationID;
		this.markings                   = source.markings;
		this.maxDerivativeExtent        = source.maxDerivativeExtent;
		this.medium                     = source.medium;
		this.objectID                   = source.objectID;
		this.objectLeonardoID 			= source.objectLeonardoID;
		this.oldAccessionNum 			= source.oldAccessionNum;
		this.owners 					= source.owners;
		this.parentID                   = source.parentID;
		this.provenanceText             = source.provenanceText;
		this.terms 						= source.terms;
		this.textEntries            	= source.textEntries;
		this.virtual 					= source.virtual;
		this.subClassification          = source.subClassification;
		this.thumbnailsProhibited       = source.thumbnailsProhibited;
		this.title                      = source.title;
		this.visualBrowserClassification= source.visualBrowserClassification;
		this.visualBrowserTimeSpan      = source.visualBrowserTimeSpan;
		this.zoomPermissionGranted      = source.zoomPermissionGranted;
		this.departmentAbbr				= source.departmentAbbr;
		this.description				= source.description;
		this.portfolio 					= source.portfolio;
		this.curatorialRemarks			= source.curatorialRemarks;
		this.watermarks					= source.watermarks;
		this.lastDetectedModification	= source.lastDetectedModification;
		this.isPublic					= source.isPublic;
	}


	/*  private static final String detailObjectQuery = 
        "SELECT objectLeonardoID, dimensions, inscription, " +
                "markings, catalogRaisonneRef, imageCopyright " +
        "FROM data.objects " +
        "WHERE objectID @@ ";
	 */

	/*public void setAdditionalProperties(ResultSet rs) throws SQLException {
		objectLeonardoID    = rs.getString(1);
		dimensionsDesc      = rs.getString(2);
		inscription         = rs.getString(3);
		markings            = rs.getString(4);
		catalogRaisonneRef  = rs.getString(5);
		imageCopyright      = rs.getString(6);
	}*/

	public ArtObject factory(ResultSet rs) throws SQLException {
		ArtObject ao = new ArtObject(this.getManager(),rs);
		return ao;
	} 

	private static final String JCRNODENAME = "ArtObject";

	protected enum RELATEDASPECT {
		STYLE(100000000),
		THEME(1000000),
		CREATIONYEAR(100000),
		MEDIUM(10000),
		ARTISTNATIONALITY(100),
		RELATEDDONOR(1);

		protected Integer weight=null;

		private RELATEDASPECT(Integer aspectWeight) {
			weight = aspectWeight;
		}
	}

	protected static enum VISUALBROWSERCLASSIFICATION_SCORES {
		PAINTING            ("painting",            Integer.parseInt("0000000001")),
		SCULPTURE           ("sculpture",           Integer.parseInt("0000000010")),
		DRAWING             ("drawing",             Integer.parseInt("0000000100")),
		NEW_MEDIA           ("new media",           Integer.parseInt("0000001000")),
		DECORATIVE_ART      ("decorative art",      Integer.parseInt("0000010000")),
		PRINT               ("print",               Integer.parseInt("0000100000")),
		PORTFOLIO           ("portfolio",           Integer.parseInt("0001000000")),
		VOLUME              ("volume",              Integer.parseInt("0010000000")),
		PHOTOGRAPH          ("photograph",          Integer.parseInt("0100000000")),
		TECHNICAL_MATERIAL  ("technical material",  Integer.parseInt("1000000000"));

		protected Integer weight=null;
		protected String label=null;

		private VISUALBROWSERCLASSIFICATION_SCORES(String label, Integer weight) {
			this.label = label;
			this.weight = weight;
		}

		public static int score(String label) {
			if (label == null)
				return 0;
			for (VISUALBROWSERCLASSIFICATION_SCORES vbs : VISUALBROWSERCLASSIFICATION_SCORES.values()) {
				if (vbs.label.equals(label))
					return vbs.weight;
			}
			return 0;
		}

	}

	private enum ArtObjectConstituentType {ARTIST, DONOR};

	// it's important to use a reasonably fast default sort order
	// because this will be called every time a list of objects is
	// generated by a search
	private static SortOrder defaultSortOrder = new SortOrder(
			SORT.ATTRIBUTIONINV_ASC,
			SORT.TITLE_ASC,
			SORT.OBJECTID_ASC
			);

	// it's important to use a fast default sort order 
	private static SortOrder naturalSortOrder = new SortOrder(SORT.OBJECTID_ASC);

	public SortOrder getDefaultSortOrder() {
		return defaultSortOrder;
	}

	public SortOrder getNaturalSortOrder() {
		return naturalSortOrder;
	}

	public List<ArtObject> otherWorksBySameArtists(Boolean matchesArtistsExactly) {
		// fetch all the objects that belong to all artists of this work, 
		// and if exactMatch is specified, then narrow the list
		// to only those works with the exact number of artists in common as the
		// number of Artists of this work
		List<ArtObject> otherWorks = getManager().fetchObjectsByRelationships(getArtistsRaw());

		// remove this object from the list
		otherWorks.remove(this);
		// if we are to fetch based solely on exact match
		if (!matchesArtistsExactly)
			return otherWorks;

		// filter out any objects that don't match exactly the same list of artists
		List<ArtObject> works = CollectionUtils.newArrayList();
		for (ArtObject o : otherWorks) {
			// if we have the same number of common artists as we have artists
			// then this is an exact match
			if (numCommonArtists(o) == numArtists())
				works.add(o);
		}
		return works;
	}

	private Long numCommonArtObjectConstituents(ArtObject ao, ArtObjectConstituentType t) {
		long cnt = 0;
		List<ArtObjectConstituent> ourList = null;
		List<ArtObjectConstituent> theirList = null;

		switch (t) {
		case ARTIST:
			ourList = getArtistsRaw();
			theirList = ao.getArtistsRaw();
			break;
		case DONOR:
			ourList = getDonorsRaw();
			theirList = ao.getDonorsRaw();
		}

		if (ourList == null || theirList == null)
			return null;

		Map<Long, MutableInt> myMap = CollectionUtils.newHashMap(); //new HashMap<Long, Long>();
		for (ArtObjectConstituent ourItem : ourList )
			myMap.put(ourItem.getConstituentID(), new MutableInt(0));

		for (ArtObjectConstituent theirItem : theirList ) {
			MutableInt tst = myMap.get(theirItem.getConstituentID());
			if (tst != null)
				tst.inc();
		}

		for (MutableInt l : myMap.values())
			cnt += l.get();
		return Long.valueOf(cnt);
	}

	public Long numCommonArtists(ArtObject ao) {
		return numCommonArtObjectConstituents(ao, ArtObjectConstituentType.ARTIST);
	}

	public Long numCommonDonors(ArtObject ao) {
		return numCommonArtObjectConstituents(ao, ArtObjectConstituentType.DONOR);
	}

	protected Map<String, MutableInt> getNationalities() {

		List<ArtObjectConstituent> ourList = getArtistsRaw();
		Map<String, MutableInt> myMap = CollectionUtils.newHashMap();
		for (ArtObjectConstituent ourItem : ourList ) {
			Constituent c = ourItem.getConstituent();
			if (c != null) {
				String n = c.getNationality();
				if (n != null) {
					MutableInt cnt = myMap.get(n);
					if (cnt == null) {
						myMap.put(n, new MutableInt(0));
					}
					else {
						cnt.inc();
					}
				}
			}
		}
		return myMap;
	}

	protected Long numCommonArtistNationalities(ArtObject ao, Map<String, MutableInt> nationalities) {
		long cnt = 0;
		// must ensure that nationality counts are set to zero since we use them
		// for keeping track of the number of like-nationalities
		for (String n : nationalities.keySet())
			nationalities.put(n, new MutableInt(0));

		List<ArtObjectConstituent> theirList = ao.getArtistsRaw();
		for (ArtObjectConstituent theirItem : theirList ) {
			Constituent c = theirItem.getConstituent();
			if (c != null) {
				String n = c.getNationality();
				MutableInt tst = nationalities.get(n);
				if (tst != null)
					tst.inc();
			}
		}

		for (MutableInt l : nationalities.values())
			cnt += l.get();
		return Long.valueOf(cnt);   
	}

	private Long numCommonArtistNationalities(ArtObject ao) {
		return numCommonArtistNationalities(ao, getNationalities());
	}

	private Long numCommonTerms(ArtObject ao, TERMTYPES... ttypes) {
		long cnt = 0;
		for (ArtObjectTerm ourTerm : getTermsRaw() ) {
			for (ArtObjectTerm theirTerm : ao.getTermsRaw()) {
				if (ourTerm.getTermID().equals(theirTerm.getTermID())) {
					for (TERMTYPES ttype : ttypes) {
						if (    ourTerm.getTermType() == ttype 
								&& theirTerm.getTermType() == ttype )
							cnt++;
					}
				}
			}
		}
		return Long.valueOf(cnt);
	}

	private void calculateAttributionArtistNameMatch() {
		Integer score = null;
		for (ArtObjectConstituent oc : getArtistsRaw()) {
			Constituent c = oc.getConstituent();
			if (c == null)
				continue;
			int s = SortHelper.compareObjectsDiacritical(getAttributionInvertedCKey(), c.getPreferredDisplayNameCKey());
			// null is returned if the two objects are equal (rather than 0) 
			// so we set s = 0 for those cases
			s = (s == Sorter.NULL || s == 0) ? 0 : 1;
			if (score == null || score > s)
				score = s;
			// no point in continuing if we already found the lowest possible value
			if (score == 0)
				break;
		}
		setIsAttributionInvertedAnArtistName(score);
	}

	// determine whether not the given art object matches this art object
	// in any of a fixed number of dimensions
	// returns a positive Long representing the closeness of a match - zero for no match
	// or null if a comparison cannot be made on the given dimension
	public Long matchesAspect(Object ae, Object order) {

		ArtObject ao = (ArtObject) ae;

		if (ao == null || order == null)
			return null;
		switch ((SORT) order) {
		case YEAR_MATCH: 
			if (getBeginYear() == null || ao.getBeginYear() == null)
				return null;
			return new Long(getBeginYear().equals(ao.getBeginYear()) ? 1 : 0);
		case CLASSIFICATION_MATCH:
			if (getClassification() == null || ao.getClassification() == null)
				return null;
			return new Long(getClassification().equals(ao.getClassification()) ? 1 : 0);
		case NUMTHEMESINCOMMON_MATCH_DESC:
			return numCommonTerms(ao,TERMTYPES.THEME);
		case NUMSTYLESINCOMMON_MATCH_DESC:
			return numCommonTerms(ao, TERMTYPES.STYLE);
		case NUMDONORSINCOMMON_MATCH_DESC:
			return numCommonDonors(ao);
		case ARTISTS_MATCH:
			Long aMatch = new Long( numCommonArtists(ao) == ao.numArtists() ? 1 : 0);
			return aMatch;
		case NUMARTISTSINCOMMON_MATCH_DESC:
			return numCommonArtists(ao);
		case NUMARTISTNATIONALITIESINCOMMON_MATCH_DESC:
			return numCommonArtistNationalities(ao);
		case ATTRIBUTIONINV_ASC:
			if (getAttributionInverted() == null || ao.getAttributionInverted() == null)
				return null;
			return new Long(getAttributionInverted().equals(ao.getAttributionInverted()) ? 1 : 0);
		case TITLE_MATCH:
			if (getTitle() == null || ao.getTitle() == null)
				return null;
			return new Long(getTitle().equals(ao.getTitle()) ? 1 : 0);
		case OBJECTID_MATCH:
			if (getObjectID() == null || ao.getObjectID() == null)
				return null;
			return new Long(getObjectID().equals(ao.getObjectID()) ? 1 : 0);
		case HASLARGERIMAGERY_MATCH:
			if (hasImagery() == null || ao.hasImagery() == null)
				return null;
			return new Long(hasImagery().equals(ao.hasImagery()) ? 1 : 0);
		case HASTHUMBNAIL_MATCH:
			if (hasThumbnail() == null || ao.hasThumbnail() == null)
				return null;
			return new Long(hasThumbnail().equals(ao.hasThumbnail()) ? 1 : 0);
		case ONVIEW_MATCH:
			if (isOnView() == null || ao.isOnView() == null)
				return null;
			return new Long(isOnView().equals(ao.isOnView()) ? 1 : 0);
		case ACCESSIONNUM_ASC:
			Long retVal = null;
			if (getAccessionNum() != null && ao.getAccessionNum() != null)
				retVal = new Long(getAccessionNum().equals(ao.getAccessionNum()) ? 1 : 0);
			return retVal;
		case LASTDETECTEDMODIFICATION_DESC:
			retVal = null;
			if (getLastDetectedModification() != null && ao.getLastDetectedModification() != null)
				retVal = new Long(getLastDetectedModification().equals(ao.getLastDetectedModification()) ? 1 : 0);
			return retVal;
		default:
			break;
		}
		return null;
	}

	// determine whether not the given art object matches this art object
	// in any of a fixed number of dimensions
	// returns 1 if a match is found, 0 if one is not found
	// or null if a comparison cannot be made on the given dimension
	public int aspectScore(Object ae, Object order, String matchString) {

		ArtObject ao = (ArtObject) ae;

		if (ao == null || order == null)
			return Sorter.NULL;
		switch ((SORT) order) {
		case YEAR_ASC: 
			return SortHelper.compareObjects(getBeginYear(), ao.getBeginYear());
		case CLASSIFICATION_ASC:
			return SortHelper.compareObjects(getClassification(), ao.getClassification());
		case ATTRIBUTIONINV_ASC:
			return SortHelper.compareObjectsDiacritical(getAttributionInvertedCKey(), ao.getAttributionInvertedCKey());
		case TITLE_ASC:
			return SortHelper.compareObjectsDiacritical(getStrippedTitleCKey(), ao.getStrippedTitleCKey());
		case TITLE_DESC:
			return SortHelper.compareObjectsDiacritical(ao.getStrippedTitleCKey(), getStrippedTitleCKey());
		case ACCESSIONNUM_ASC:
			return SortHelper.compareObjects(getAccessionNum(), ao.getAccessionNum());
		case ACCESSIONNUM_DESC:
			return SortHelper.compareObjects(ao.getAccessionNum(), getAccessionNum());
		case LASTDETECTEDMODIFICATION_ASC:
			return SortHelper.compareObjects(getLastDetectedModification(), ao.getLastDetectedModification());
		case LASTDETECTEDMODIFICATION_DESC:
			return SortHelper.compareObjects(ao.getLastDetectedModification(), getLastDetectedModification());
		case FIRST_ARTIST_ASC:
			Constituent c = getFirstArtist();
			Constituent ac = ao.getFirstArtist();
			String cName = (c == null) ? null : c.getPreferredDisplayName();
			String aName = (ac == null) ? null : ac.getPreferredDisplayName();
			return SortHelper.compareObjectsDiacriticalAutoCache(cName,aName);
		case FIRST_ARTIST_DESC:
			c = getFirstArtist();
			ac = ao.getFirstArtist();
			cName = (c == null) ? null : c.getPreferredDisplayName();
			aName = (ac == null) ? null : ac.getPreferredDisplayName();
			return SortHelper.compareObjectsDiacriticalAutoCache(aName,cName);
		case OBJECTID_ASC:
			return SortHelper.compareObjects(getObjectID(), ao.getObjectID());
		case OBJECTID_DESC:
			return SortHelper.compareObjects(ao.getObjectID(), getObjectID());
		case HASLARGERIMAGERY_DESC:
			int a = hasImagery() ? 0 : 1;
			int b = 0;
			if (ao.hasImagery())
			{
				b = 1;
			}
			return SortHelper.compareObjects(a,b);
		case HASTHUMBNAIL_DESC:
			a = hasThumbnail() ? 0 : 1;
			b = 0;
			if (ao.hasThumbnail())
			{
				b = 1;
			}
			return SortHelper.compareObjects(a,b);
		case ONVIEW_DESC:
			a = isOnView() ? 0 : 1;
			b = 0;
			if (ao.isOnView())
			{
				b = 1;
			}
			return SortHelper.compareObjects(a,b);
		case NUMARTISTS_ASC:
			a = numArtists().intValue();
			b = ao.numArtists().intValue();
			return SortHelper.compareObjects(a,b);
		case ATTRIBUTIONINV_ARTISTNAME_MATCH_ASC:
			Integer aInt = this.isAttributionInvertedAnArtistName();
			Integer bInt = ao.isAttributionInvertedAnArtistName();
			return SortHelper.compareObjects(aInt,bInt);
		case VISUALBROWSERCLASSIFICATION_CUSTOM:
			a = VISUALBROWSERCLASSIFICATION_SCORES.score(this.getVisualBrowserClassification());
			b = VISUALBROWSERCLASSIFICATION_SCORES.score(ao.getVisualBrowserClassification());
			if (a == b)
				return SortHelper.compareObjects(this.getVisualBrowserClassification(), ao.getVisualBrowserClassification());
			return SortHelper.compareObjects(a,b);
			/*      case ATTRIBUTIONINV_MATCH_A_STRING:
            if (matchString != null) {
                a = getAttributionInverted() != null && getAttributionInverted().equals(matchString) ? 0 : 1;
                b = ao.getAttributionInverted() != null && ao.getAttributionInverted().equals(matchString) ? 0 : 1;
                return SortHelper.compareObjects(a,b);
            }
            return null;
			 */
		case ARTISTS_MATCH:
			break;
		case ATTRIBUTIONINV_MATCH:
			break;
		case CLASSIFICATION_MATCH:
			break;
		case HASLARGERIMAGERY_MATCH:
			break;
		case HASTHUMBNAIL_MATCH:
			break;
		case NUMARTISTNATIONALITIESINCOMMON_MATCH_DESC:
			break;
		case NUMARTISTSINCOMMON_MATCH_DESC:
			break;
		case NUMDONORSINCOMMON_MATCH_DESC:
			break;
		case NUMSTYLESINCOMMON_MATCH_DESC:
			break;
		case NUMTHEMESINCOMMON_MATCH_DESC:
			break;
		case OBJECTID_MATCH:
			break;
		case ONVIEW_MATCH:
			break;
		case TITLE_MATCH:
			break;
		case YEAR_MATCH:
			break;
		default:
			break; 
		}
		return Sorter.NULL;
	}

	protected Long relatedTotalScore(ArtObject o, Map<String, MutableInt> data) {
		Long cnt = Long.valueOf(0);
		for (RELATEDASPECT a : RELATEDASPECT.values() )
			cnt += relatedAspectScore(o, a, data);
		return cnt;
	}

	protected Long relatedAspectScore(ArtObject o, RELATEDASPECT aspect, Map<String, MutableInt> data) {
		long aspScore;
		switch (aspect)  {
		case STYLE:
			aspScore = aspect.weight * numCommonTerms(o,TERMTYPES.STYLE);
			return aspScore;
		case THEME:
			aspScore = aspect.weight * numCommonTerms(o,TERMTYPES.THEME);
			return aspScore;
		case CREATIONYEAR:
			Long year = getBeginYear();
			Long oYear = o.getBeginYear();
			if (year == null || oYear == null)
				return Long.valueOf(0);
			if ( oYear.equals(year) )
				return Long.valueOf(aspect.weight);
			return Long.valueOf(0);
		case MEDIUM:
			String medium = getMedium();
			String oMedium = o.getMedium();
			if (medium == null | oMedium == null)
				return Long.valueOf(0);
			if (oMedium.equals(medium))
				return Long.valueOf(aspect.weight);
			return Long.valueOf(0);
		case ARTISTNATIONALITY:
			Map<String, MutableInt> nationalities = data;
			return aspect.weight * numCommonArtistNationalities(o,nationalities);
		case RELATEDDONOR:
			return aspect.weight * numCommonDonors(o);
		}
		return Long.valueOf(0);
	}

	public boolean hasDonorConstituentID(SearchFilter f) {
		for (ArtObjectConstituent oc : getDonorsRaw()) {
			Boolean res = f.filterMatch(oc.getConstituentID().toString());
			if (res != null && res == true)
				return res;
		}
		return false;
	}

	public boolean hasOwnerConstituentID(SearchFilter f) {
		for (ArtObjectConstituent oc : getOwnersRaw()) {
			Boolean res = f.filterMatch(oc.getConstituentID().toString());
			if (res != null && res == true)
				return res;
		}
		return false;
	}

	public boolean hasSubClassification(SearchFilter f) {
		return f.filterMatch(subClassification);
	}


	/*  public boolean hasArtistDisplayName(SearchFilter f) {
        return hasArtistName(f, false);
    }

    public boolean hasArtistName(SearchFilter f, boolean altNames) {
        for (ArtObjectConstituent oc : getArtistsRaw()) {
            Constituent c = oc.getConstituent(); 
            if (c == null)
                continue;
            String val = c.getPreferredDisplayName();
            Boolean res = f.filterMatch(val);
            if (res != null && res == true)
                return res;
            if (altNames) {
                List<ConstituentAltName> names = c.getAltNames();
                if (names != null) {
                    for (ConstituentAltName name : names) {
                        val = name.getDisplayName();
                        res = f.filterMatch(val);
                        if (res != null && res == true)
                            return res;
                    }
                }
            }
        }
        return false;
    }
	 */

	public boolean hasConstituentName(List<ArtObjectConstituent> constituentList, SearchFilter f, boolean altNames) {
		for (ArtObjectConstituent oc : constituentList) {
			Constituent c = oc.getConstituent(); 
			if (c == null)
				continue;
			String val = c.getPreferredDisplayName();
			Boolean res = f.filterMatch(val);
			if (res != null && res == true)
				return res;
			if (altNames) {
				List<ConstituentAltName> names = c.getAltNames();
				if (names != null) {
					for (ConstituentAltName name : names) {
						val = name.getDisplayName();
						res = f.filterMatch(val);
						if (res != null && res == true)
							return res;
					}
				}
			}
		}
		return false;
	}

	public boolean hasArtistNationality(SearchFilter f, boolean rawNationality) {
		boolean triedMatch = false;
		for (ArtObjectConstituent oc : getArtistsRaw()) {
			Constituent c = oc.getConstituent();
			if (c == null)
				continue;
			String val = rawNationality ? c.getNationality() : c.getVisualBrowserNationality();
			Boolean res = f.filterMatch(val);
			if (res != null && res == true)
				return true;
			triedMatch = true;
		}

		// try to match on null if we were unable to attempt any matches above
		if (!triedMatch) {
			Boolean res = f.filterMatch(null);
			if (res != null && res == true)
				return true;
		}
		return false;
	}

	public boolean hasArtistNationality(SearchFilter f) {
		return hasArtistNationality(f, false);
	}

	public List<String> getArtistNationalities() {
		Map<String,Object> nationalities = CollectionUtils.newHashMap();
		// this could be called by multiple threads concurrently
		// so we synchronize on artists to prevent multiple simultaneous iterations
		for (ArtObjectConstituent oc : getArtistsRaw()) {
			Constituent c = oc.getConstituent();
			if (c == null)
				continue;
			nationalities.put(c.getVisualBrowserNationality(), null);
		}
		return CollectionUtils.newArrayList(nationalities.keySet());
	}

	public List<ArtObjectTerm> getTerms(TERMTYPES... ttypes) {
		List<ArtObjectTerm> l = CollectionUtils.newArrayList();
		for (ArtObjectTerm t : getTermsRaw()) {
			for (TERMTYPES ttype : ttypes) {
				if (t.getTermType() == ttype )
					l.add(t);
			}
		}
		return l;
	}

	public List<String> getNormalizedTerms(TERMTYPES... ttypes) {
		Map<String,Object> m = CollectionUtils.newHashMap();
		for (ArtObjectTerm t : getTermsRaw()) {
			for (TERMTYPES ttype : ttypes) {
				if (ttype != null && t.getTermType() == ttype) {
					switch (ttype) {
					case STYLE: 
						m.put(t.getVisualBrowserStyle(), null);
						break;
					case THEME: 
						m.put(t.getVisualBrowserTheme(), null); 
						break;
					default: 
						m.put(t.getTerm(), null);
						break;
					}
				}
				else if (ttype == null)
					m.put(t.getTerm(), null);
			}
		}
		return CollectionUtils.newArrayList(m.keySet());
	}

	// check to see whether this object has a term matching any of a number of given termtypes
	private boolean hasNormalizedTerm(SearchFilter f, TERMTYPES... ttypes) {
		boolean triedMatch = false;
		for (ArtObjectTerm t : getTermsRaw()) {
			for (TERMTYPES ttype : ttypes) {
				if (ttype == null || t.getTermType() == ttype) {
					String val = null;
					if (ttype != null) {
						switch (ttype) {
						case STYLE: 
							val = t.getVisualBrowserStyle();
							break;
						case THEME: 
							val = t.getVisualBrowserTheme();    
							break;
						default: 
							val = t.getTerm();
							break;
						}
					}
					else
						val = t.getTerm();

					Boolean res = f.filterMatch(val);
					if (res != null && res == true)
						return res;
					triedMatch = true;
				}
			}
		}

		// we couldn't even attempt a match meaning the object doesn't even have
		// any of the requested terms, so we attempt to match on value == null in
		// that case
		if (!triedMatch) {
			Boolean res = f.filterMatch(null);
			if (res != null && res == true)
				return res;
		}

		return false;
	}

	protected boolean hasNormalizedTheme(SearchFilter f) {
		return hasNormalizedTerm(f, TERMTYPES.THEME);
	}

	protected boolean hasNormalizedSchool(SearchFilter f) {
		return hasNormalizedTerm(f, TERMTYPES.SCHOOL);
	}

	protected boolean hasNormalizedThemeOrKeyword(SearchFilter f) {
		return hasNormalizedTerm(f, TERMTYPES.KEYWORD);
	}

	protected boolean hasNormalizedStyle(SearchFilter f) {
		return hasNormalizedTerm(f, TERMTYPES.STYLE);
	}

	public String freeTextSearchToNodePropertyName(Object field) {
		switch ( (FREETEXTSEARCH) field ) {
		case ALLDATAFIELDS          : return "*";
		case ARTISTNATIONALITIES    : return "nationalities";
		case ARTISTS                : return "artists";
		case BIBLIOGRAPHYTEXT       : return "bibliographyText";
		case CATALOGRAISONNEREF     : return "catalogRaisonneRef";
		case CONSERVATIONNOTES      : return "conservationNotes";
		case CREDITLINE             : return "creditLine";
		case DIMENSIONS             : return "dimensions";
		case DONORS                 : return "donors";
		case EXHIBITIONHISTORY      : return "exhibitionHistory";
		case IMAGECOPYRIGHT         : return "imageCopyright";
		case INSCRIPTION            : return "inscription";
		case MARKINGS               : return "markings";
		case MEDIUM                 : return "medium";
		case OVERVIEWTEXT           : return "overviewText";
		case OWNERS                 : return "owners";
		case PROVENANCETEXT         : return "provenanceText";
		case SYSCATTEXT             : return "syscatText";
		case TERMS                  : return "terms";
		}
		return null;
	}

	public Boolean matchesFilter(SearchFilter f) {
		switch ( (SEARCH) f.getField()) {
		case ARTIST_DISPLAYNAME: 
			return hasConstituentName(getArtistsRaw(), f, false);
		case ARTIST_ALLNAMES: 
			return hasConstituentName(getArtistsRaw(), f, true);
		case OWNER_ALLNAMES:
			return hasConstituentName(getOwnersRaw(), f, true);
		case HASLARGERIMAGERY: 
			return f.filterMatch(hasImagery().toString());
		case HASTHUMBNAIL: 
			return f.filterMatch(hasThumbnail().toString());
		case ONVIEW: 
			return f.filterMatch(isOnView().toString());
		case TITLE:
			return f.filterMatch(getFormatFreeTitle());
		case OBJECTID:
			return f.filterMatch(getObjectID().toString());
		case MEDIUM:
			return f.filterMatch(getFormatFreeMedium());
		case YEARS_SPAN:
			Long by = getBeginYear();
			Long ey = getEndYear();
			String v1 = ( (by == null) ? null : by.toString() );
			String v2 = ( (ey == null) ? null : ey.toString() );
			return f.filterMatch(v1,v2);
		case YEARS_BEGIN:
			by = getBeginYear();
			v1 = by == null ? "" : by.toString();
			return f.filterMatch(v1);
		case ACCESSIONNUM:
			return f.filterMatch(getAccessionNum());
		case LASTDETECTEDMODIFICATION:
			return getLastDetectedModification() == null ? false : f.filterMatch(getLastDetectedModification().toString());
		case ATTRIBUTION_INV:
			return f.filterMatch(getAttributionInverted());
		case PROVENANCE:
			return f.filterMatch(StringUtils.removeOnlyHTMLAndFormatting(getProvenanceText()));
		case CREDITLINE:
			return f.filterMatch(creditLine);
		case DONORCONSTITUENTID:
			return hasDonorConstituentID(f);
		case OWNERCONSTITUENTID:
			return hasOwnerConstituentID(f);            
		case LOCATION_ID:
			Long i = getLocationID();
			return f.filterMatch(i != null ? i.toString() : null);
		case LOCATION_SITE:
			Location l = getLocation();
			return f.filterMatch(l != null ? l.getSite() : null);
		case LOCATION_ROOM:
			l = getLocation();
			return f.filterMatch(l != null ? l.getRoom() : null);
		case LOCATION_DESCRIPTION:
			l = getLocation();
			return f.filterMatch(l != null ? l.getDescription() : null);
		case LOCATION_UNITPOSITION:
			l = getLocation();
			return f.filterMatch(l != null ? l.getUnitPosition() : null);
		case SUBCLASSIFICATION:
			return hasSubClassification(f);
		case VISUALBROWSERTIMESPAN:
			return f.filterMatch(getVisualBrowserTimeSpan());
		case VISUALBROWSERTHEME:
			return hasNormalizedTheme(f);
		case VISUALBROWSERTHEMEORKEYWORD:
			return hasNormalizedThemeOrKeyword(f);
		case VISUALBROWSERSCHOOL :
			return hasNormalizedSchool(f);
		case VISUALBROWSERSTYLE:
			return hasNormalizedStyle(f);
		case VISUALBROWSERNATIONALITY:
			return hasArtistNationality(f);
		case VISUALBROWSERCLASSIFICATION:
			return f.filterMatch(getVisualBrowserClassification());
		case NATIONALITY:
			return hasArtistNationality(f, true);
		}
		return false;
	}

	public List<String> getFacetValue(Object f) {
		List<String> values = CollectionUtils.newArrayList();
		switch ((FACET) f) {
		case VISUALBROWSERTIMESPAN:
			values = StringUtils.stringToList(getVisualBrowserTimeSpan());
			break;
		case VISUALBROWSERCLASSIFICATION:
			values = StringUtils.stringToList(getVisualBrowserClassification());
			break;
		case VISUALBROWSERNATIONALITY:
			values = getArtistNationalities();
			break;
		case SCHOOL:
			values = getNormalizedTerms(TERMTYPES.SCHOOL);
			break;
		case ONVIEW : 
			values = StringUtils.stringToList(isOnView().toString());
			break;
		case VISUALBROWSERSTYLE:
			values = getNormalizedTerms(TERMTYPES.STYLE);
			break;
		case VISUALBROWSERTHEME:
			values = getNormalizedTerms(TERMTYPES.THEME);
			break;
		case HASLARGERIMAGERY:
			values = StringUtils.stringToList(hasImagery().toString());
			break;
		case OSCICATALOGUE:
			break;
		default:
			break;
		}
		//log.debug("(" + getObjectID() + ")" + "Facet (" + f + "): " + values);
		return values;
	}

	public String getOverviewText() {
		return getOverviewText(getDefaultFilter());
	}
	public String getOverviewText(StringFilter sf) {
		return sf.getFilteredString(TextEntry.firstTextOfType(getTextEntriesRaw(), TEXT_ENTRY_TYPE.BRIEF_NARRATIVE));
	}

	public String getConservationNotes() {
		return getConservationNotes(getDefaultFilter());
	}
	public String getConservationNotes(StringFilter sf) {
		return sf.getFilteredString(TextEntry.firstTextOfType(getTextEntriesRaw(), TEXT_ENTRY_TYPE.CONSERVATION_NOTE));
	}

	public String getSysCat() {
		return getSysCat(getDefaultFilter());
	}
	public String getSysCat(StringFilter sf) {
		return sf.getFilteredString(TextEntry.firstTextOfType(getTextEntriesRaw(), TEXT_ENTRY_TYPE.SYSTEMATIC_CATALOGUE));
	}

	private void loadArtists() {
		artists = CollectionUtils.newArrayList();
		for (ArtObjectConstituent oc : getConstituentsRaw() ) {
			if (oc.getRoleType().equals("artist")) {
				artists.add(oc);
			}
		}
		calculateDisplayArtists();
	}

	private void loadDonors() {
		donors = CollectionUtils.newArrayList();
		for (ArtObjectConstituent oc : getConstituentsRaw() ) {
			if (oc.getRoleType().equals("donor") && oc.getRole().equals("donor")) {
				// log.info(oc.getObjectID() + ":" + oc.getRole() + ":" + oc.getRoleType());
				donors.add(oc);
			}
		}
	}

	private void loadOwners() {
		owners = CollectionUtils.newArrayList();
		for (ArtObjectConstituent oc : getConstituentsRaw() ) {
			if (oc.getRoleType().equals("owner") ) {
				owners.add(oc);
			}
		}
	}

	synchronized protected void setTerms(List<ArtObjectTerm> newTerms) {
		terms = newTerms;
	}

	protected void setTerms() {
		List<ArtObjectTerm> newTerms = CollectionUtils.newArrayList();
		setTerms(newTerms);
	}

	synchronized protected void setConstituents(List<ArtObjectConstituent> newConstituents) {
		constituents = newConstituents;
		// also create a separate list of artists and donors based off the constituents
		// since those are based off the same data
		loadArtists();
		loadDonors();
		loadOwners();
	}

	protected void setConstituents() {
		List<ArtObjectConstituent> list = CollectionUtils.newArrayList();
		setConstituents(list);
	}

	synchronized protected void setImages() {
		images = CollectionUtils.newHashMap();
		List<ArtObjectImage> newArtObjectImages = CollectionUtils.newArrayList();
		List<ResearchImage> newResearchDerivatives = CollectionUtils.newArrayList();
		setImages(newArtObjectImages, new ArtObjectImage(null));
		setImages(newResearchDerivatives, new ResearchImage(null));
	}

	synchronized protected <T extends Derivative> void setImages(List<T> newImages, T seed) {
		if (seed.getImageClass()==IMAGECLASS.ARTOBJECTIMAGE && isThumbnailProhibited())
			return;
		images.put(seed.getImageClass(), newImages);
	}

	private boolean inPrivateOperatingMode() {
		return getManager().getOperatingMode() == OperatingMode.PRIVATE;
	}

	protected boolean imageOK(Derivative d) {
		// we can only show an image if we're in private operating mode OR 
			// if the object is a public object
			// and thumbnails are not prohibited
			// and if a zoom image, we can show zooms
			// and either we can show imagery in general or the size constitutes fair use 
		return  inPrivateOperatingMode() || 							// in private operating mode OR...
					( 	isPublic() &&										// public object AND 
						!isThumbnailProhibited() &&                         // thumbnails not prohibited AND
						( !d.isZoom() || isZoomImageryPermitted() ) &&      // if zoom image, ensure we can show zooms for this object AND
						( canShowImagery() ||                               // either we can show imagery in general or the size is fair use or smaller
								(                                           
										d.getWidth()  <= Derivative.FAIRUSEMAXEXTENT && 
										d.getHeight() <= Derivative.FAIRUSEMAXEXTENT
								)
						)
					);
	}

	private Derivative getBestImageForBox(long w, long h, IMGVIEWTYPE vt, String seq, ImgSearchOpts... opts) {
		Derivative d = getImageExactTargetBoxMatch(w,h,vt,seq,opts);
		// if we don't find an exact match then based on the fallback plan, 
		// search for an alternative
		if (d == null && opts != null) {
			for (ImgSearchOpts opt : opts) {
				if (opt == ImgSearchOpts.FALLBACKTOLARGESTFIT) {
					d = getLargestImageFittingTarget(w,h);
					break;
				}
			}
		}
		return d;
	}

	public Derivative getSmallThumbnail(IMGVIEWTYPE vt, String seq, ImgSearchOpts... opts) {
		return getBestImageForBox(50,50,vt,seq,opts);
	}

	public Derivative getSmallThumbnail(ImgSearchOpts... opts) {
		return getSmallThumbnail(IMGVIEWTYPE.PRIMARY,ArtObjectImage.PRIMARY_OBJECT_IMAGE_SEQUENCE,opts);
	}

	public Derivative getSmallThumbnail() {
		return getSmallThumbnail(ImgSearchOpts.NOOP);
	}

	public Derivative getRolloverThumbnail(IMGVIEWTYPE vt, String seq, ImgSearchOpts... opts) {
		return getBestImageForBox(140,90,vt,seq,opts);
	}

	public Derivative getRolloverThumbnail(ImgSearchOpts... opts) {
		return getRolloverThumbnail(IMGVIEWTYPE.PRIMARY,ArtObjectImage.PRIMARY_OBJECT_IMAGE_SEQUENCE,opts);
	}

	public Derivative getLargeThumbnail(IMGVIEWTYPE vt, String seq, ImgSearchOpts... opts) {
		// we prefer crops for the large thumb nail in all cases (else Laszlo buys beer)
		if (opts == null || opts.length == 0) {
			opts = new ImgSearchOpts[1];
			opts[0] = ImgSearchOpts.PREFERCROP;
		}
		return getBestImageForBox(90,90,vt,seq,opts);
	}

	// returns a list of the largest image available for all of the available view types for this object
	public List<Derivative> getLargestImages() {
		return getLargestImages(getAvailableImageViewTypes());
	}

	// returns a list of the largest image available for each of a particular collection of types for all sequence permutations
	public List<Derivative> getLargestImages(Collection<IMGVIEWTYPE> limitViewTypes ) {
		List<Derivative> largestImages = CollectionUtils.newArrayList();
		for (IMGVIEWTYPE vt : limitViewTypes) {
			for (String seq : getAvailableImageSequences(vt)) {
				Derivative d = Derivative.getLargestImage(getImages(), vt, seq);
				if (d != null)
					largestImages.add(d);
			}
		}
		return largestImages;
	}
	
	public List<String> getAvailableImageSequences(IMGVIEWTYPE vt) {
		return Derivative.getAvailableImageSequences(getImages(), vt);
	}

	public List<IMGVIEWTYPE> getAvailableImageViewTypes() {
		return Derivative.getAvailableImageViewTypes(getImages());
	}

	public Derivative getLargeThumbnail(ImgSearchOpts... opts) {
		return getLargeThumbnail(IMGVIEWTYPE.PRIMARY,ArtObjectImage.PRIMARY_OBJECT_IMAGE_SEQUENCE,opts);
	}

	public Derivative getSmallImage(IMGVIEWTYPE vt, String seq, ImgSearchOpts... opts) {
		return getBestImageForBox(325,370,vt,seq,opts);
	}

	public Derivative getSmallImage(ImgSearchOpts... opts) {
		return getSmallImage(IMGVIEWTYPE.PRIMARY,ArtObjectImage.PRIMARY_OBJECT_IMAGE_SEQUENCE,opts);
	}

	public Derivative getMediumImage(IMGVIEWTYPE vt, String seq, ImgSearchOpts... opts) {
		return getBestImageForBox(440,400, vt, seq, opts);
	}

	public Derivative getMediumImage(ImgSearchOpts... opts) {
		return getMediumImage(IMGVIEWTYPE.PRIMARY,ArtObjectImage.PRIMARY_OBJECT_IMAGE_SEQUENCE,opts);
	}

	public Derivative getLargeImage(IMGVIEWTYPE vt, String seq, ImgSearchOpts... opts) {
		return getBestImageForBox(740,560,vt,seq,opts);
	}

	public Derivative getLargeImage(ImgSearchOpts... opts) {
		return getLargeImage(IMGVIEWTYPE.PRIMARY,ArtObjectImage.PRIMARY_OBJECT_IMAGE_SEQUENCE,opts);
	}

	public Derivative getZoomImage(IMGVIEWTYPE vt, String seq) {
		return Derivative.getLargestZoomImage(getImages(), vt, seq);
	}

	public List<Derivative> getTombstoneImages() {
		List<Derivative> result = new ArrayList<Derivative>();
		for (IMGVIEWTYPE vt: getAvailableImageViewTypes())
		{
			if (IMGVIEWTYPE.PRIMARY.equals(vt) || IMGVIEWTYPE.ALTERNATE.equals(vt))
			{
				for (String seq: getAvailableImageSequences(vt))
				{
					Derivative img = getMediumImage(vt, seq);
					if (img!=null)
						result.add(img);
				}
			}
		}
		//compare by view type and then by sequence but primary view type is always first 
		Collections.sort(result, new Comparator<Derivative>()
				{
			@Override
			public int compare(Derivative d1, Derivative d2)
			{
				if (IMGVIEWTYPE.PRIMARY.equals(d1.getViewType()) && IMGVIEWTYPE.PRIMARY.equals(d2.getViewType()))
					return d1.getSequence().compareTo(d2.getSequence());
				else if (IMGVIEWTYPE.PRIMARY.equals(d1.getViewType()))
					return -1;
				else if (IMGVIEWTYPE.PRIMARY.equals(d2.getViewType()))
					return 1;
				else {
					int result = d1.getViewType().getLabel().compareTo(d2.getViewType().getLabel()); 
					if (result==0)
						result = d1.getSequence().compareTo(d2.getSequence());
					return result;
				}
			}
				});
		return result;
	}

	public Derivative getZoomImage() {
		return getZoomImage(IMGVIEWTYPE.PRIMARY,ArtObjectImage.PRIMARY_OBJECT_IMAGE_SEQUENCE);
	}

	public Derivative getImageExactTargetBoxMatch(long targetWidth, long targetHeight, IMGVIEWTYPE vt, String seq, ImgSearchOpts... opts) {
		return Derivative.getImageExactTargetBoxMatch(getImages(), targetWidth, targetHeight, vt, seq, opts);
	}

	public Derivative getImageExactTargetBoxMatch(long targetWidth, long targetHeight, ImgSearchOpts... opts) {
		return getImageExactTargetBoxMatch(targetWidth, targetHeight, IMGVIEWTYPE.PRIMARY, ArtObjectImage.PRIMARY_OBJECT_IMAGE_SEQUENCE, opts);
	}

	public Derivative getLargestImageFittingTarget(long targetWidth, long targetHeight, IMGVIEWTYPE vt, String seq, ImgSearchOpts... opts) {
		return Derivative.getLargestImageFittingTarget(getImages(), targetWidth, targetHeight, vt, seq, opts);
	}

	public Derivative getLargestImageFittingTarget(long targetWidth, long targetHeight, ImgSearchOpts... opts) {
		return getLargestImageFittingTarget(targetWidth, targetHeight, IMGVIEWTYPE.PRIMARY, ArtObjectImage.PRIMARY_OBJECT_IMAGE_SEQUENCE, opts);
	}

	public Derivative getImageClosestPixels(long numPixels, IMGVIEWTYPE vt, String seq, ImgSearchOpts... opts) {
		return Derivative.getImageClosestPixels(getImages(), numPixels, vt, seq, opts);
	}

	private Map<IMAGECLASS, List<? extends Derivative>> images;

	private List<? extends Derivative> getImagesRaw() {
		if (images != null)
			return images.get(IMAGECLASS.ARTOBJECTIMAGE);
		else
			return null;
	}

	public List<Derivative> getImages() {
		if (getImagesRaw() == null)
			return null;
		return new ArrayList<Derivative>(getImagesRaw());
	}

	private List<ResearchImage> getResearchImagesRaw() {
		List<ResearchImage> rList = CollectionUtils.newArrayList();
		for (Derivative d : images.get(IMAGECLASS.RESEARCHIMAGE)) {
			if ( d instanceof ResearchImage)
				rList.add( (ResearchImage) d);
		} 
		return rList;
	} 

	public List<ResearchImage> getResearchImages() { 
		return CollectionUtils.newArrayList(getResearchImagesRaw());
	}

	public List<ResearchImage> getInscriptionImages(long targetWidth, long targetHeight) {
		return Derivative.getLargestImagesFittingTarget(getResearchImagesRaw(), targetWidth, targetHeight, Derivative.IMGVIEWTYPE.INSCRIPTION, ImgSearchOpts.NOOP);
	}

	public List<ArtObjectConstituent> getConstituents() {
		return CollectionUtils.newArrayList(getConstituentsRaw());
	}

	private List<ArtObjectConstituent> constituents = null;
	private List<ArtObjectConstituent> getConstituentsRaw() {
		return constituents;
	}

	public ArtObjectAssociation getParentAssociation() {
		ArtObjectAssociation ctx = null;
		// get list of associations where this object is a child
		List<ArtObjectAssociation> parents = ArtObjectAssociation.filterByRole(getParentChildAssociationsRaw(), this, ARTOBJECTRELATIONSHIPROLE.CHILD);
		if (parents != null && parents.size() > 0) {
			if (parents.size() > 1)
				log.error("Multiple parent object associations found for object ID " + this.getObjectID());
			ctx = parents.get(0);
			if ( ctx != null && !ctx.getAssociatedArtObject().equals(this.getParent()))
				log.error("The physical parent ID " + this.getParentID() + " for object ID " + this.getObjectID() + " does not match the data from the art object associations table. ");
		} 
		return ctx;
	}

	private List<ArtObjectAssociationRecord> parentChildAssociations = CollectionUtils.newArrayList();
	synchronized void setAssociations(List<ArtObjectAssociationRecord> associations) { 
		this.parentChildAssociations = associations;
	}
	private List<ArtObjectAssociationRecord> getParentChildAssociationsRaw() {
		return parentChildAssociations;
	}

	public List<ArtObjectAssociation> getArtObjectAssociations() {
		List<ArtObjectAssociation> childAssociations = getChildAssociations();
		List<ArtObjectAssociation> siblingAssociations = getSiblingAssociations();
		ArtObjectAssociation parentAssociation = getParentAssociation();

		List<ArtObjectAssociation> allAssociations = CollectionUtils.newArrayList();
		if ( parentAssociation != null )
			allAssociations.add(parentAssociation);
		if ( childAssociations != null )
			allAssociations.addAll(childAssociations);
		if ( siblingAssociations != null )
			allAssociations.addAll(siblingAssociations);
		if ( allAssociations.size() > 0 ) {
			Collections.sort(allAssociations,ArtObjectAssociation.defaultSorter);
			return allAssociations;
		}
		return null;
	}

	public List<ArtObjectAssociation> getChildAssociations() {
		List<ArtObjectAssociation> children = 
				ArtObjectAssociation.filterByRole(getParentChildAssociationsRaw(), this, ARTOBJECTRELATIONSHIPROLE.PARENT);
		if (children == null || children.size() < 1)
			return null;
		Collections.sort(children, ArtObjectAssociation.defaultSorter);
		return children;
	}

	// return this object's parent's list of children, but don't include my association with my parent
	public List<ArtObjectAssociation> getSiblingAssociations() {
		ArtObjectAssociation parentAssociation = getParentAssociation();
		if (parentAssociation == null)
			return null;
		ArtObject p = parentAssociation.getAssociatedArtObject();
		// get all children of the parent then removes self from the list

		List<ArtObjectAssociation> siblings = 
				ArtObjectAssociation.filterByRole(p.getParentChildAssociationsRaw(), this, ARTOBJECTRELATIONSHIPROLE.SIBLING);
		if (siblings == null || siblings.size() < 1)
			return null;
		Collections.sort(siblings, ArtObjectAssociation.defaultSorter);

		return siblings;
	}

	private List<ArtObjectConstituent> displayArtists = null;
	public List<ArtObjectConstituent> getArtists() {
		if (displayArtists != null)
			return CollectionUtils.newArrayList(displayArtists);
		else
			return CollectionUtils.newArrayList();
	}
	
	public Constituent getFirstArtist() {
		if (getArtistsRaw() != null && getArtistsRaw().size() > 0) {
			return getArtistsRaw().get(0).getConstituent();
		}
		return null;
	}

	private void calculateDisplayArtists() {
		// assemble a list of artists associated with this object
		// and filter out cases where the same constituent is listed twice
		// in a row such as object ID 9874 where Rembrandt is listed as both
		// an artist and then a related artist.  The display order has the artist
		// relationship listed first, so that's the relationship that gets returned here

		Map<Long, List<ArtObjectConstituent>> artistMap = CollectionUtils.newHashMap();
		for (ArtObjectConstituent oc : getArtistsRaw()) {

			List<ArtObjectConstituent> l = artistMap.get(oc.getConstituentID());
			if (l == null) {
				l = CollectionUtils.newArrayList();
				l.add(oc);
				artistMap.put(oc.getConstituentID(), l);
			}

			else {
				boolean oc_related = oc.getRole().equals("related artist");
				boolean list_has_nonrelated = false;

				List<ArtObjectConstituent> removeList = CollectionUtils.newArrayList();
				for (ArtObjectConstituent oj : l) {
					boolean oj_related = oj.getRole().equals("related artist");
					list_has_nonrelated = list_has_nonrelated || !oj_related;
					// remove elements from the existing list if they are related artist roles
					// and oc is not a related artist role
					if (!oc_related && oj_related) {
						removeList.add(oj);
					}
				}

				// if the current relationship is not a related artist role
				// then we add it nomatter what.  If it is a related artist role
				// then we only add it if the list does not contain any roles
				// other than related artist
				if (!oc_related || !list_has_nonrelated) {
					l.add(oc);
				}

				for (ArtObjectConstituent j : removeList) {
					l.remove(j);
				}

			}
		}
		displayArtists = CollectionUtils.newArrayList();
		for (List<ArtObjectConstituent> l : artistMap.values()) {
			for (ArtObjectConstituent oc : l) {
				displayArtists.add(oc);
			}
		}
		Collections.sort(displayArtists, ArtObjectConstituent.sortByDisplayOrderAsc);
	}

	private List<ArtObjectConstituent> artists = null;
	protected List<ArtObjectConstituent> getArtistsRaw() {
		return artists;
	}

	private Long numArtists() {
		return Long.valueOf(getArtistsRaw().size());
	}

	public List<ArtObjectConstituent> getDonors() {
		return CollectionUtils.newArrayList(getDonorsRaw());
	}

	private List<ArtObjectConstituent> donors = null;
	private List<ArtObjectConstituent> getDonorsRaw() {
		return donors;
	}

	public List<ArtObjectConstituent> getOwners() {
		return CollectionUtils.newArrayList(getOwnersRaw());
	}

	private List<ArtObjectConstituent> owners = null;
	protected List<ArtObjectConstituent> getOwnersRaw() {
		return owners;
	}

	// purely for backwards compatibility
	public List<ArtObjectExhibition> getExhibitionHistory() {
		List<ArtObjectExhibition> bList = CollectionUtils.newArrayList();
		for (ArtObjectTextEntry ce : TextEntry.filterByTextType(getTextEntriesRaw(), TEXT_ENTRY_TYPE.EXHIBITION_HISTORY)) {
			if ( ce instanceof ArtObjectExhibition)
				bList.add( (ArtObjectExhibition) ce);
		}
		return bList;
	}

	// purely for backwards compatibility
	public List<Bibliography> getBibliography() {
		List<Bibliography> bList = CollectionUtils.newArrayList();
		List<Bibliography> bListNilYears = null;
		for (ArtObjectTextEntry ce : TextEntry.filterByTextType(getTextEntriesRaw(), TEXT_ENTRY_TYPE.BIBLIOGRAPHY)) {
			if ( ce instanceof Bibliography) {
				Bibliography be = (Bibliography)ce;

				//Add bibliography entries with unknown year to different list
				//and then push all of them at the beginning of the result list
				if (be.getYearPublished() != 0L) {
					bList.add( be );
				} else {
					if (bListNilYears == null) {
						bListNilYears = CollectionUtils.newArrayList();
					}
					bListNilYears.add(be);
				}
			}
		}

		if (bListNilYears != null) {
			bList.addAll(0, bListNilYears);
		}
		return bList;
	}

	public List<ArtObjectTerm> getTerms() {
		return CollectionUtils.newArrayList(getTermsRaw());
	}

	private List<ArtObjectTerm> terms = null;
	private List<ArtObjectTerm> getTermsRaw() {
		return terms;
	}

	private Long objectID = null;
	// BASIC FIELDS LOADED AT FIRST
	public Long getObjectID() {
		return objectID;
	}

	public Long getEntityID() {
		return getObjectID();
	}

	private String title = null;
	public String getTitle() {
		return title;
	}
	
	private String formatFreeTitle = null;
	public String getFormatFreeTitle() {
		return formatFreeTitle;
	}

//	public String getTitle(StringFilter sf) {
//		return sf.getFilteredString(title);
//	}
	private Long locationID = null;
	public Long getLocationID() {
		return locationID;
	}

	Location location = null;
	public Location getLocation() {
		if (location != null)
			return location;

		Long lid = getLocationID();
		if (lid == null)
			return null;

		location = getManager().fetchByLocationID(lid);

		return location;
	}

	public Boolean isOnView() {
		Location loc = getLocation();
		return loc == null ? false : loc.isPublicLocation();
	}

	private String displayDate = null;
	public String getDisplayDate() {
		return displayDate;
	}

	private Long accessioned = null;
	public Long getAccessioned() {
		return accessioned;
	}

	public Boolean isAccessioned() {
		return TypeUtils.longToBoolean(getAccessioned());
	}

	private Long beginYear = null;
	public Long getBeginYear() {
		return beginYear;
	}

	private Long endYear = null;
	public Long getEndYear() {
		return endYear;
	}

	private Long canShowImagery = null;
	private Long getCanShowImagery() {
		return canShowImagery;
	}

	private Boolean canShowImagery() {
		// we can always show any image when in a private context or when specifically enabled (or not disabled) as a permission
		return inPrivateOperatingMode() || TypeUtils.longToBoolean(getCanShowImagery());
	}

	private Long parentID = null;
	private Long getParentID() {
		return parentID;
	}

	private ArtObject getParent() {
		return getManager().fetchByObjectID(this.getParentID());
	}

	private String classification = null;
	public String getClassification() {
		return classification;
	}

	private String subClassification = null;
	public String getSubClassification() {
		return subClassification;
	}

	private String visualBrowserClassification = null;
	public String getVisualBrowserClassification() {
		return visualBrowserClassification;
	}

	private String attributionInverted = null;
	public String getAttributionInverted() {
		return attributionInverted;
	}

	private String attribution = null;
	public String getAttribution() {
		return attribution;
	}

	private String visualBrowserTimeSpan = null;
	public String getVisualBrowserTimeSpan() {
		return visualBrowserTimeSpan;
	}

	private Long thumbnailsProhibited = null;
	private Long getThumbnailsProhibited() {
		return thumbnailsProhibited;
	}

	// thumbnails are only prohibited if we're not in private operating mode AND showing the thumbnail is prohibited
	public Boolean isThumbnailProhibited() {
		return !inPrivateOperatingMode() && TypeUtils.longToBoolean(getThumbnailsProhibited());
	}

	private Long isIAD = null;
	private Long getIsIAD() {
		return isIAD;
	}

	public Boolean isIADObject() {
		return TypeUtils.longToBoolean(getIsIAD());
	}

	private String accessionNum = null;
	public String getAccessionNum() {
		return accessionNum;
	}

	private String creditLine = null;
	public String getCreditLine() {
		return creditLine;
	}

	String medium = null;
	public String getMedium() {
		return medium;
	}
	public String getMedium(StringFilter sf) {
		return sf.getFilteredString(medium);
	}
	
	private String formatFreeMedium = null;
	public String getFormatFreeMedium() {
		return formatFreeMedium;
	}

	// the TMS extract does not consider a crop or a research image
	// to be a derivative - it pulls only from the published web images
	private Long maxDerivativeExtent = null;
	public Long getMaxDerivativeExtent () {
		return maxDerivativeExtent;
	}

	private String provenanceText = null;
	public String getProvenanceText() {
		return getProvenanceText(getDefaultFilter());
	}

	public String getProvenanceText(StringFilter sf) {
		return sf.getFilteredString(provenanceText);
	}

	private Integer isAttributionInvertedAnArtistName = null;
	public Integer isAttributionInvertedAnArtistName() {
		if (isAttributionInvertedAnArtistName == null)
			calculateAttributionArtistNameMatch();
		return isAttributionInvertedAnArtistName;
	}
	synchronized private void setIsAttributionInvertedAnArtistName(Integer i) {
		isAttributionInvertedAnArtistName = i;
	}

	// DETAIL FIELDS STORED WITH OBJECT THAT ARE LOADED ON DEMAND 
	// AT ONCE IF ANY ARE REQUESTED
	private String imageCopyright = null;
	public String getImageCopyright() {
		//loadDetails();
		return imageCopyright;
	}

	private String objectLeonardoID = null;
	public String getObjectLeonardoID() {
		//loadDetails();
		return objectLeonardoID;
	}

	private String dimensionsDesc = null;
	public String getDimensions() {
		//loadDetails();
		return dimensionsDesc;
	}

	private String inscription = null;
	public String getInscription() {
		return inscription;
	}
	public String getInscription(StringFilter sf) {
		return sf.getFilteredString(inscription);
	}

	private String markings = null;
	public String getMarkings() {
		return markings;
	}
	public String getMarkings(StringFilter sf) {
		return sf.getFilteredString(markings);
	}

	private String portfolio = null;
	public String getPortfolio() {
		return portfolio;
	}

	private String departmentAbbr = null;
	public String getDepartmentAbbr() {
		return departmentAbbr;
	}
	
	private String catalogRaisonneRef = null;
	public String getCatalogRaisonneRef() {
		//loadDetails();
		return catalogRaisonneRef; 
	}
	
	private String description = null;
	public String getDescription() {
		return description; 
	}

	// indicate whether we are permitted to display zoom images for an object
	public boolean isZoomImageryPresent() {
		return hasImagery() && isZoomImageryPermitted();
	}

	// we only have images if the object actually has images AND we're able to show them
	protected Boolean hasImagery() {
		return hasThumbnail() && canShowImagery(); 
	}

	protected Boolean hasThumbnail() {
		// max derivative extent only applies to published web images, not crops and research images
		// so a full complement of derivatives exists if this is not null or zero
		Long mde = getMaxDerivativeExtent();
		// if the largest derivative size is greater than zero and we can show thumbnails
		// then we "have a thumbnail"
		if ( mde != null && mde > 0 && !isThumbnailProhibited() )
			return true;
		return false;
	}

	/****** ADDED FOR NEW WEB FUNCTIONALITY *******/

	private Long zoomPermissionGranted = null;
	private Long getZoomPermissionGranted () {
		return zoomPermissionGranted;
	}

	// we can display zoom imagery if in private operating mode or if the permission is explicitly granted
	public Boolean isZoomImageryPermitted() {
		return inPrivateOperatingMode() || TypeUtils.longToBoolean(getZoomPermissionGranted());
	}

	private Long downloadAvailable = null;
	private Long getDownloadAvailable() {
		return downloadAvailable;
	}

	public Boolean isDownloadAvailable() {
		return TypeUtils.longToBoolean(getDownloadAvailable());
	}

	private Long virtual = null;
	private Long getVirtual() {
		return virtual;
	}

	public Boolean isVirtual() {
		return TypeUtils.longToBoolean(getVirtual());
	}

	private Long isPublic = null;
	private Long getIsPublic() {
		return isPublic;
	}

	public Boolean isPublic() {
		return TypeUtils.longToBoolean(getIsPublic());
	}
	
	/****** ADDED FOR OSCI *******/

	public String getJCREntityType() 
	{
		return JCRNODENAME;
	}

	private String oldAccessionNum;
	public String getOldAccessionNum() {
		return oldAccessionNum;
	}

	private List<ArtObjectHistoricalData> historicalData = null;
	private List<ArtObjectHistoricalData> getHistoricalDataRaw() {
		return historicalData; 
	}
	synchronized protected void addHistoricalData(ArtObjectHistoricalData histEntry) {
		if (historicalData == null)
			historicalData = CollectionUtils.newArrayList();
		historicalData.add(histEntry);
	}

	public <T extends Quantity> Measure<Double, T> getDimension(DIMENSION_TYPE dType) {
		return ArtObjectDimension.findDimension(getAllDimensionsRaw(), dType);
	}

	private List<ArtObjectDimension> allDimensions = null;
	private List<ArtObjectDimension> getAllDimensionsRaw() {
		return allDimensions; 
	}
	synchronized protected void addDimensions(ArtObjectDimension dimension) {
		if (allDimensions == null)
			allDimensions = CollectionUtils.newArrayList();
		allDimensions.add(dimension);
	}

	// possible replacement for getBibliography if templates are adapted to use the more generic ArtObjectTextEntry object
	public List<ArtObjectHistoricalData> getPreviousAttributions() {
		return ArtObjectHistoricalData.filterByDataType(getHistoricalDataRaw(), HISTORICAL_DATA_TYPE.PREVIOUS_ATTRIBUTION);
	}

	public List<ArtObjectHistoricalData> getPreviousTitles() {
		return ArtObjectHistoricalData.filterByDataType(getHistoricalDataRaw(), HISTORICAL_DATA_TYPE.PREVIOUS_TITLE);
	}

	private List<ArtObjectTextEntry> textEntries = null;
	private List<ArtObjectTextEntry> getTextEntriesRaw() {
		return textEntries; 
	}
	synchronized protected void addTextEntry(ArtObjectTextEntry te) {
		if (textEntries == null)
			textEntries = CollectionUtils.newArrayList();
		textEntries.add(te);
	}

	// possible replacement for getExhibitionHistory if templates are adapted to use the more generic ArtObjectTextEntry object
	public List<ArtObjectTextEntry> getExhibitionEntries() {
		return ArtObjectTextEntry.filterByTextType(getTextEntriesRaw(), TEXT_ENTRY_TYPE.EXHIBITION_HISTORY);
	}

	// possible replacement for getBibliography if templates are adapted to use the more generic ArtObjectTextEntry object
	public List<ArtObjectTextEntry> getBibliographyEntries() {
		return ArtObjectTextEntry.filterByTextType(getTextEntriesRaw(), TEXT_ENTRY_TYPE.BIBLIOGRAPHY);
	}

	public List<ArtObjectTextEntry> getExhibitionFootnoteEntries() {
		return ArtObjectTextEntry.filterByTextType(getTextEntriesRaw(), TEXT_ENTRY_TYPE.EXHIBITION_HISTORY_FOOTNOTE);
	}

	private List<ArtObjectComponent> artObjectComponents = null;
	private List<ArtObjectComponent> getArtObjectComponentsRaw() {
		return artObjectComponents; 
	}
	synchronized protected void addComponent(ArtObjectComponent c) {
		if (artObjectComponents == null)
			artObjectComponents = CollectionUtils.newArrayList();
		artObjectComponents.add(c);
	}

	public List<ArtObjectComponent> getComponents() {
		return new ArrayList<ArtObjectComponent>(getArtObjectComponentsRaw());
	}

	private String curatorialRemarks=null;
	public String getCuratorialRemarks() {
		return curatorialRemarks;
	}

	private String watermarks=null;
	public String getWatermarks() {
		return watermarks;
	}

	private String lastDetectedModification=null;
	public String getLastDetectedModification() {
		return lastDetectedModification;
	}

	private CollationKey strippedTitleCKey = null;
	public CollationKey getStrippedTitleCKey() {
		return strippedTitleCKey;
	}

	private CollationKey attributionInvertedCKey = null;
	public CollationKey getAttributionInvertedCKey() {
		return attributionInvertedCKey;
	}
	
}



















