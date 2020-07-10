package gov.nga.entities.art;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.AlternateNumberData;
import gov.nga.common.entities.art.ArtObjectLocation;
import gov.nga.common.entities.art.Department;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.entities.art.OperatingModeService.OperatingMode;
import gov.nga.common.entities.art.Location;
import gov.nga.common.utils.CollectionUtils;
import gov.nga.entities.art.sync.tms.ArtObjectLocationFactory;
import gov.nga.entities.art.sync.tms.DepartmentFactory;
import gov.nga.entities.art.sync.tms.ExhibitionFactoryImpl;
import gov.nga.entities.art.sync.tms.TMSAlternateNumber;
import gov.nga.entities.art.sync.tms.TMSExhibitionFactory;
import gov.nga.entities.art.sync.tms.TMSLocation;
import gov.nga.utils.SystemUtils;
import gov.nga.utils.db.DataSourceService;

public class TMSFetcher {
	private static final Logger log = LoggerFactory.getLogger(TMSFetcher.class);
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd H:m:ss");
    
    private final DataSourceService poolService;
    private final boolean isPrivateConfigured;
    private final ArtDataManagerService manager;
    
    protected TMSFetcher(final DataSourceService ps, final OperatingMode mode, final ArtDataManagerService mgr)
    {
        poolService = ps;
        isPrivateConfigured = mode == OperatingMode.PRIVATE;
        manager = mgr;
    }
    
    private DataSourceService getPoolService()
    {
        return poolService;
    }
    
    synchronized protected TMSData load() throws Exception {

        log.info("AAAAAAAAAAAAAAAAAAAAAA: Beginning load of art object data");
        log.info(SystemUtils.freeMemorySummary());

        // load all art object constituent data first, then pass that
        // map to both the object manager and constituent manager
        TMSData newData = new TMSData();
        try {
            
            if (getIsPrivateConfigured())
            {
                log.info("Loading all departments");
                newData.departments = (new DepartmentFactory()).buildDepartments(getPoolService());
            }
            else
            {
                newData.departments = CollectionUtils.newHashMap();
            }
            
            log.info("Loading all object constituent relationships");
            EntityQuery<ArtObjectConstituent> eq = new EntityQuery<ArtObjectConstituent>(getPoolService());
            List<ArtObjectConstituent> ocs = eq.fetchAll(ArtObjectConstituent.fetchAllObjectsConstituentsQuery, new ArtObjectConstituent(manager));
            log.info("found this many object constituent relationships: " + ocs.size());
            log.info(SystemUtils.freeMemorySummary());

            log.info("Loading all location and related data");
            newData.locations = loadLocations();
            newData.newPlaces = loadPlaces();
            newData.newPlacesTMSLocations = loadPlacesTMSLocations(newData.newPlaces);
            log.info(SystemUtils.freeMemorySummary());
            
            log.info("Loading all media and media relationshps");
            newData.newMediaItems = loadMediaItems();
            newData.newMediaRelationshps = loadMediaRelationships(newData.newMediaItems);

            log.info("Loading all components");
            List<ArtObjectComponent> aocomps = loadComponents();
            
            log.info("Loading all art object text entries");
            EntityQuery<ArtObjectTextEntry> teq = new EntityQuery<ArtObjectTextEntry>(getPoolService());
            List<ArtObjectTextEntry> teList = teq.fetchAll(ArtObjectTextEntry.allTextEntryQuery, new ArtObjectTextEntry(manager));
            log.info("found this many art object text entries: " + teList.size());

            log.info("Loading all art object historical data entries");
            EntityQuery<ArtObjectHistoricalData> aohistq = new EntityQuery<ArtObjectHistoricalData>(getPoolService());
            List<ArtObjectHistoricalData> aohist = aohistq.fetchAll(ArtObjectHistoricalData.allHistoricalDataQuery, new ArtObjectHistoricalData(manager));
            log.info("found this many art object historical data entries: " + aohist.size());

            log.info("Loading all art object dimensions");
            EntityQuery<ArtObjectDimension> aoDimsq = new EntityQuery<ArtObjectDimension>(getPoolService());
            List<ArtObjectDimension> aoDims = aoDimsq.fetchAll(ArtObjectDimension.allObjectsDimensionsQuery, new ArtObjectDimension(manager));
            log.info("found this many art object dimensions: " + aoDims.size());

            log.info("Loading all inter art object associations");
            EntityQuery<ArtObjectAssociationRecord> aq = new EntityQuery<ArtObjectAssociationRecord>(getPoolService());
            List<ArtObjectAssociationRecord> aoas = aq.fetchAll(ArtObjectAssociationRecord.fetchAllArtObjectAssociationsQuery, new ArtObjectAssociationRecord(manager));
            log.info("found this many art object assocations: " + aoas.size());

            log.info("Loading all art objects and related data");
            newData.artObjects = getArtObjects(ocs, teList, aohist, aoDims, aoas, newData.getLocationData(), aocomps);
            log.info(SystemUtils.freeMemorySummary());
            
            EntityQuery<ConstituentAltName> ceq = new EntityQuery<ConstituentAltName>(getPoolService());
            log.info("Loading all constituent alternate names");
            List<ConstituentAltName> alts = ceq.fetchAll(ConstituentAltName.fetchAllConstituentAltNamessQuery, new ConstituentAltName(manager));
            log.info("found this many constituent alternate names: " + alts.size());

            log.info("Loading all constituent text entries");
            EntityQuery<ConstituentTextEntry> cte = new EntityQuery<ConstituentTextEntry>(getPoolService());
            List<ConstituentTextEntry> ctes = cte.fetchAll(ConstituentTextEntry.allTextEntryQuery, new ConstituentTextEntry(manager));
            log.info("found this many constituent text entries: " + ctes.size());

            log.info("Loading all constituent and related data");
            newData.constituents = getConstituents(ocs, alts, ctes, newData.getArtObjectData());
            log.info(SystemUtils.freeMemorySummary());
            
            if (isPrivateConfigured)
            {
                log.info("Loading all exhibitions data");
                TMSExhibitionFactory srv = new ExhibitionFactoryImpl();
                if (srv != null)
                {
                    newData.exhibitions = srv.getExhibitions(newData.artObjects, newData.constituents, poolService);
                    log.info(SystemUtils.freeMemorySummary());
                }
            }
            else
            {
                newData.exhibitions = CollectionUtils.newHashMap();
            }
            // then set it again
            log.info("Syncing complete.");
            // we can start serving queries again now
            /**
            // pre-calculate all art object facet counts for use by the initial visual browser page
            log.info("Pre-caching all art object facet counts");
            //getArtObjectFacetCounts();
            log.info(SystemUtils.freeMemorySummary());

            // pre-calculate the facet ranges for the index of artists
            log.info("Pre-caching all facet ranges for index of artists");
            //getIndexOfArtistsRanges();
            log.info(SystemUtils.freeMemorySummary());
            log.info("**** FINISHED LOADING ART OBJECT DATA ****");
            **/
        }
        // if we are unable to fetch the data we need, then we reschedule
        // ourselves which will attempt to kick off another refresh immediately
        catch (SQLException se) {
            log.error("ERROR Loading TMS Data: " + se.getMessage(), se );
        }
        return newData;
    }

    // load all art object components
    synchronized protected List<ArtObjectComponent> loadComponents() throws SQLException {
        EntityQuery<ArtObjectComponent> eq = new EntityQuery<ArtObjectComponent>(getPoolService());
        log.info("Starting pre-fetch of all components");
        List<ArtObjectComponent> newComponents = eq.fetchAll(ArtObjectComponent.fetchAllComponentsQuery, new ArtObjectComponent(manager));
        log.info("found this many components: " + newComponents.size());
        return newComponents;
    }
    
    // load all web defined places which (mostly) have TMS objects residing in them
    synchronized protected Map<Long, Media> loadMediaItems() throws SQLException {
        Map<Long, Media> newMediaMap = CollectionUtils.newHashMap();
        EntityQuery<Media> eq = new EntityQuery<Media>(getPoolService());
        log.info("Starting pre-fetch of all media definitions");
        List<Media> newMediaList = eq.fetchAll(Media.fetchAllMediaQuery, new Media(manager));
        log.info("found this many place definitions: " + newMediaList.size());
        for (Media m : newMediaList) {
            newMediaMap.put(m.getMediaID(), m);
        }
        return newMediaMap;
    }

    // load all web defined place to tms object location associations
    synchronized protected Map<String, List<Media>> loadMediaRelationships(Map<Long, Media> mediaItems) throws SQLException {
        Map<String, List<Media>> newRelationshipsMap = CollectionUtils.newHashMap();
        EntityQuery<MediaRelationship> eq = new EntityQuery<MediaRelationship>(getPoolService());
        log.info("Starting pre-fetch of all Media relationships");
        List<MediaRelationship> newRelationshipsList = eq.fetchAll(MediaRelationship.fetchAllMediaRelationshipsQuery, new MediaRelationship(manager));
        log.info("found this many Media relationships: " + newRelationshipsList.size());
        for (MediaRelationship mr : newRelationshipsList) {
        	List<Media> l = newRelationshipsMap.get(mr.getEntityUniqueID());
        	if ( l == null ) {
        		l = CollectionUtils.newArrayList();
        		newRelationshipsMap.put(mr.getEntityUniqueID(), l);
        	}
        	l.add(mediaItems.get(mr.getMediaID()));
        }
        return newRelationshipsMap;
    }

    // load all web defined places which (mostly) have TMS objects residing in them
    synchronized protected Map<String, Place> loadPlaces() throws SQLException {
        Map<String, Place> newPlacesMap = CollectionUtils.newHashMap();
        EntityQuery<Place> eq = new EntityQuery<Place>(getPoolService());
        log.info("Starting pre-fetch of all place definitions");
        List<Place> newPlacesList= eq.fetchAll(Place.fetchAllPlacesQuery, new Place(manager));
        log.info("found this many place definitions: " + newPlacesList.size());
        for (Place l : newPlacesList) {
            newPlacesMap.put(l.getPlaceKey(), l);
        }
        return newPlacesMap;
    }

    // load all web defined place to tms object location associations
    synchronized protected Map<Long, Place> loadPlacesTMSLocations(Map<String, Place> places) throws SQLException {
        Map<Long, Place> newPlaceTMSLocations = CollectionUtils.newHashMap();
        EntityQuery<PlaceRelationships> eq = new EntityQuery<PlaceRelationships>(getPoolService());
        log.info("Starting pre-fetch of all place to TMS Location relationships");
        List<PlaceRelationships> newLocations = eq.fetchAll(PlaceRelationships.fetchAllPlaceTMSLocationsQuery, new PlaceRelationships(manager));
        log.info("found this many place to tms location relationships: " + newLocations.size());
        for (PlaceRelationships l : newLocations) {
            newPlaceTMSLocations.put(l.getTMSLocationID(), places.get(l.getPlaceKey()));
        }
        return newPlaceTMSLocations;
    }
    
    // load all art object locations
    synchronized protected Map<Long, Location> loadLocations() throws SQLException {
        Map<Long, Location> newLocations = CollectionUtils.newHashMap();
        EntityQuery<TMSLocation> eq = new EntityQuery<TMSLocation>(getPoolService());
        log.info("Starting pre-fetch of all locations");
        List<TMSLocation> newObjectLocations = eq.fetchAll(TMSLocation.fetchAllLocationsQuery, new TMSLocation(manager));
        log.info("found this many locations: " + newObjectLocations.size());
        for (Location l : newObjectLocations) {
            newLocations.put(l.getLocationID(), l);
        }
        return newLocations;
    }
    
    
 // load all constituent data
    synchronized protected Map<Long, Constituent> getConstituents(List<ArtObjectConstituent> ocs, List<ConstituentAltName> alts, 
            List<ConstituentTextEntry> ctes, Map<Long, ArtObject> newArtObjects) throws SQLException {

        Map<Long, Constituent> newConstituents = CollectionUtils.newHashMap();

        EntityQuery<Constituent> eq = new EntityQuery<Constituent>(getPoolService());
        log.info("Starting pre-fetch of all constituents");
        List<Constituent> list = eq.fetchAll(Constituent.fetchAllConstituentsQuery, new Constituent(manager));
        log.debug("found this many constituents: " + list.size());

        // store constituents in a map, indexed by constituent ID
        for (Constituent c : list ) {
            newConstituents.put(c.getConstituentID(), c);
        }

        // distribute the constituent alt names to the constituents
        log.info("Assigning all alternate names to constituents");
        for (ConstituentAltName alt : alts) {
            Constituent c = newConstituents.get(alt.getConstituentID());
            if (c != null) {
                c.addAltName(alt);
            }
            else {
                log.error("Could not locate constituent " + alt.getConstituentID() + " to place alternate name data");
            }
        }
        
        log.info("Assigning Constituent Bibliography Entries");
        for (ConstituentTextEntry e : ctes) {
            Constituent c = newConstituents.get(e.getConstituentID());
            if (c != null)
                c.addTextEntry(e);
        }
        
        // load the constituents into a huge map of lists indexed by object id
        // and into another huge map indexed by constituent ids
        log.info("Assigning all object roles to all constituents");
        Map<Long, List<ArtObjectConstituent>> mapByConstituent = CollectionUtils.newHashMap(); //new HashMap<Long, List<ArtObjectConstituent>>();
        for (ArtObjectConstituent oc : ocs) {
            oc.constituent = newConstituents.get(oc.getConstituentID());
            //oc.object = newArtObjects.get(oc.getObjectID());
            List<ArtObjectConstituent> loc = mapByConstituent.get(oc.getConstituentID());
            if (loc == null) {
                loc = CollectionUtils.newArrayList();
                mapByConstituent.put(oc.getConstituentID(), loc);
            }
            // add this object constituent relationship to the list of relationships for its constituent
            // sometimes, an artist can be listed with multiple roles such as both an artist and a related artist
            // and we must store all of them - individual methods such as getWorks() should remove any duplicated works
            // or duplicate artists as necessary (and if necessary)
            loc.add(oc);
        }

        // set the list of art object relationships for each constituent
        // with no pre-defined sort order
        for (Long id : mapByConstituent.keySet()) {
            Constituent c = newConstituents.get(id);
            List<ArtObjectConstituent> loc = mapByConstituent.get(id);
            if (c != null && loc != null) {
                c.setObjectRoles(loc);
            }
        }

        return newConstituents;
    }

    // load all of the art object data into our cached map
    synchronized protected Map<Long, ArtObject> getArtObjects(
            List<ArtObjectConstituent> ocs,
            List<ArtObjectTextEntry> textEntries,
            List<ArtObjectHistoricalData> aohist,
            List<ArtObjectDimension> aoDims,
            List<ArtObjectAssociationRecord> aoas,
            Map<Long, Location> aoLocations,
            List<ArtObjectComponent> aocomps
    ) throws SQLException {

        Map<Long, List<ArtObjectAssociationRecord>> associations = CollectionUtils.newHashMap();
        for (ArtObjectAssociationRecord aoa : aoas) {
            List<ArtObjectAssociationRecord> objAssociations = associations.get(aoa.getParentObjectID());
            if (objAssociations == null) {
                objAssociations = CollectionUtils.newArrayList();
                associations.put(aoa.getParentObjectID(), objAssociations);
            } 
            objAssociations.add(aoa);

            objAssociations = associations.get(aoa.getChildObjectID());
            if (objAssociations == null) {
                objAssociations = CollectionUtils.newArrayList();
                associations.put(aoa.getChildObjectID(), objAssociations);
            } 
            objAssociations.add(aoa);
        }

        // OBJECTS THEMSELVES
        Map<Long, ArtObject> newArtObjects = CollectionUtils.newHashMap();
        EntityQuery<ArtObject> eq = new EntityQuery<ArtObject>(getPoolService());
        log.info("Starting pre-fetch of all objects");
        ArtObject.setFetchAllObjectsQuery(OperatingMode.PRIVATE);
        List<ArtObject> newObjects = eq.fetchAll(ArtObject.fetchAllObjectsQuery, new ArtObject(manager));
        log.info("found this many objects: " + newObjects.size());
        for (ArtObject o : newObjects) {
            // create blank lists for all objects by default so that
            // we don't try to load them again later if they're actually blank
            o.setConstituents();
            o.setTerms();
            o.setImages();
            o.setAssociations(associations.get(o.getObjectID()));
            newArtObjects.put(o.getObjectID(), o);
        }

        // CONSTITUENT RELATIONSHIPS
        // load the constituents into a huge map of lists indexed by object id
        Map<Long, List<ArtObjectConstituent>> mapByObject = CollectionUtils.newHashMap(); //new HashMap<Long, List<ArtObjectConstituent>>();
        for (ArtObjectConstituent oc : ocs) {
            List<ArtObjectConstituent> loc = mapByObject.get(oc.getObjectID());
            if (loc == null) {
                loc = CollectionUtils.newArrayList();
                mapByObject.put(oc.getObjectID(), loc);
            }
            loc.add(oc);
        }

        // set the list of constituents for each art object
        // pre-sorting the list by the displayOrder contained in the relationship
        for (Long id : mapByObject.keySet()) {
            ArtObject o = (ArtObject)newArtObjects.get(id);
            List<ArtObjectConstituent> l = mapByObject.get(id);
            if (o != null && l != null) {
                Collections.sort(l,ArtObjectConstituent.sortByDisplayOrderAsc);
                o.setConstituents(l);
            }
        }
        

        //Location Information
        log.debug("Loading and setting ArtObject locaion information");
        new ArtObjectLocationFactory().buildLocations(newArtObjects, aoLocations, getPoolService(), this);
        

        // OBJECT IMAGES
        loadImagery(newArtObjects, new ArtObjectImage(manager));
        loadImagery(newArtObjects, new ResearchImage(manager));
        
        checkImageSizes(newArtObjects);
        
        // OBJECT TERMS
        EntityQuery<ArtObjectTerm> teq = new EntityQuery<ArtObjectTerm>(getPoolService());
        log.info("Starting pre-fetch of all object terms");
        List<ArtObjectTerm> newTerms = teq.fetchAll(ArtObjectTerm.fetchAllObjectTermsQuery, new ArtObjectTerm(manager));
        log.info("found this many object terms: " + newTerms.size());

        // separate the terms into a map indexed by object id
        // and store a list of object IDs for each term for fast access
        Map<Long, List<ArtObjectTerm>> termsByObject = CollectionUtils.newHashMap();//new HashMap<Long, List<ArtObjectTerm>>();
        for (ArtObjectTerm t : newTerms) {
            ArtObject o = newArtObjects.get(t.getObjectID());
            if ( o != null ) {
                List<ArtObjectTerm> lt = termsByObject.get(t.getObjectID());
                if (lt == null) {
                    lt = CollectionUtils.newArrayList();
                    termsByObject.put(t.getObjectID(), lt);
                }
                lt.add(t);
            }
        }

        // then assign those lists of terms to each object
        for (Long id : termsByObject.keySet()) {
            ArtObject o = (ArtObject)newArtObjects.get(id);
            List<ArtObjectTerm> l = termsByObject.get(id);
            if (o != null && l != null) {
                o.setTerms(l);
            }
        }
        
        log.info("Assigning text entries to art objects");
        for (ArtObjectTextEntry te : textEntries) {
            ArtObject o = (ArtObject)newArtObjects.get(te.getObjectID());
            if (o != null)
                o.addTextEntry(te);
        }

        log.info("Assigning historical data entries to art objects");
        for (ArtObjectHistoricalData h : aohist) {
            ArtObject o = (ArtObject)newArtObjects.get(h.getObjectID());
            if (o != null)
                o.addHistoricalData(h);
        }
        
        log.info("Assigning dimensions to art objects");
        for (ArtObjectDimension d : aoDims) {
            ArtObject o = (ArtObject)newArtObjects.get(d.getObjectID());
            if (o != null)
                o.addDimensions(d);
        }
        
        log.info("Assigning components to art objects");
        for (ArtObjectComponent c : aocomps) {
            ArtObject o = (ArtObject)newArtObjects.get(c.getObjectID());
            if (o != null)
                o.addComponent(c);
        }

        return newArtObjects;

    }
    
    synchronized protected <T extends Derivative> void loadImagery(Map<Long, ArtObject> newArtObjects, T seed) throws SQLException {
        
        EntityQuery<T> deq = new EntityQuery<T>(getPoolService());
        log.info("Starting pre-fetch of all " + seed.getClass().getName() + " images");
        List<T> newImages = deq.fetchAll(seed.getAllImagesQuery(), seed);
        log.info("found this many " + seed.getClass().getName() + " images: " + newImages.size());
        
        // add to the derivatives list
//        derivativesRaw.addAll(newImages);
        
        // separate the images into a map indexed by object id
        Map<Long, List<T>> imgByObject = CollectionUtils.newHashMap();
        for (T d : newImages) {
        	//  DPB - I don't think we need this actually 
        	//  derivativesByImageID.put(d.getImageID(), d);
            ArtObject o = (ArtObject)newArtObjects.get(d.getArtObjectID());
            if ( o != null && o.imageOK(d) ) {
                List<T> ld = imgByObject.get(d.getArtObjectID());
                if (ld == null) {
                    ld = CollectionUtils.newArrayList();
                    imgByObject.put(d.getArtObjectID(), ld);
                }
                ld.add(d);
            }
        }

        // then assign those lists of images to each object
        for (Long id : imgByObject.keySet()) {
            ArtObject o = (ArtObject)newArtObjects.get(id);
            List<T> l = imgByObject.get(id);
            if (o != null && l != null) {
                o.setImages(l, seed);
            }
        } 

    }

    private void checkImageSizes(final Map<Long, ArtObject> newArtObjects)
    {
        try
        {
            log.info("Starting checking zoom image sizes");
            for (ArtObject ao : newArtObjects.values())
            {
                List<ResearchImage> techImages = new ArrayList<ResearchImage>();
                Set<String> seqs = new HashSet<String>();
                for (ResearchImage image : ao.getResearchImages())
                {
                    if (Derivative.IMGFORMAT.PTIF.equals(image.getFormat())
                            && Derivative.IMGVIEWTYPE.TECHNICAL.equals(image.getViewType())
                            && image.getAltImageRef() == null
                            && image.getIsZoomable())
                    {
                        techImages.add(image);
                        seqs.add(org.apache.commons.lang3.StringUtils.substringBefore(image.getSequence(), "."));
                    }
                }

                nextSequence:
                for (String seq : seqs)
                {
                    List<ResearchImage> sameSeq = findBySequenceNumber(techImages, seq);
                    if (sameSeq.size() > 1)
                    {
                        Long width = sameSeq.get(0).getWidth();
                        Long height = sameSeq.get(0).getHeight();
                        for (ResearchImage im : sameSeq)
                        {
                            if (!width.equals(im.getWidth()) || !height.equals(im.getHeight()))
                            {
                                log.error("Some images have different sizes for ObjID={} and sequence={} ", im.getArtObjectID(), seq);
                                break nextSequence;
                            }
                        }
                    }
                }
            }
            log.info("Completed checking zoom image sizes");
        } catch (Exception e)
        {
            log.error("Error in checkImageSizes", e);
        }
    }

    private List<ResearchImage> findBySequenceNumber(List<ResearchImage> allImages, String sequence)
    {
        List<ResearchImage> result = new ArrayList<ResearchImage>();
        for (ResearchImage image : allImages)
        {
            if (sequence.equals(org.apache.commons.lang3.StringUtils.substringBefore(image.getSequence(), ".")))
                result.add(image);
        }
        return result;
    }
    
    public boolean getIsPrivateConfigured()
    {
        return isPrivateConfigured;
    }
    
    class TMSData
    {
        Map<Long, ArtObject> artObjects;
        Map<Long, Constituent> constituents;
        Map<String, Place> newPlaces;
        Map<Long, Place> newPlacesTMSLocations;
        Map<Long, Location> locations;
        Map<Long, Media> newMediaItems;
        Map<String, List<Media>> newMediaRelationshps;
        List<ArtObjectComponent> aocomps;
        Map<Long, Exhibition> exhibitions;
        Map<Long, Department> departments;
        
        
        protected Map<Long, ArtObject> getArtObjectData()
        {
            return artObjects;
        }
        
        protected Map<Long, Constituent> getConstituentData()
        {
            return constituents;
        }
        
        protected Map<Long, Location> getLocationData()
        {
            return locations;
        }
        
        protected Map<Long, Exhibition> getExhbitionData()
        {
            return exhibitions;
        }
        protected Map<Long, Department> getDepartments()
        {
            return departments;
        }
    }
    
    public void setCurretnLocation(ArtObject obj, ArtObjectLocation loc)
    {
        obj.currentLocation = loc;
    }
    
    public void setHomeLocation(ArtObject obj, ArtObjectLocation loc)
    {
        obj.homeLocation = loc;
    }
}
