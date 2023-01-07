package gov.nga.rpc.service;

import net.devh.boot.grpc.server.service.GrpcService;


import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nga.common.rpc.ArtDataQuerierGrpc;
import gov.nga.common.rpc.ArtObjectObjectResult;
import gov.nga.common.rpc.ArtObjectQueryResult;
import gov.nga.common.rpc.ArtDataSuggestionResult;
import gov.nga.common.rpc.CacheFetchQuery;
import gov.nga.common.rpc.ConstituentQueryMessages.ConstituentsObjectResult;
import gov.nga.common.rpc.ConstituentQueryMessages.ConstituentQueryResult;
import gov.nga.common.rpc.DepartmentObjectResult;
import gov.nga.common.rpc.ExhibitionArtObjectResult;
import gov.nga.common.rpc.ExhibitionConstituentResult;
import gov.nga.common.rpc.ExhibitionObjectQuery;
import gov.nga.common.rpc.ExhibitionObjectResult;
import gov.nga.common.rpc.ExhibitionQueryResult;
import gov.nga.common.rpc.FetchByIDsQuery;
import gov.nga.common.rpc.FetchByStringsQuery;
import gov.nga.common.rpc.SuggestArtDataQuery;
import gov.nga.common.rpc.LocationObjectResult;
import gov.nga.common.rpc.LocationQueryResult;
import gov.nga.common.rpc.MediaObjectResult;
import gov.nga.common.rpc.NGAImageResult;
import gov.nga.common.rpc.PlaceObjectResult;
import gov.nga.common.rpc.QueryResult;
import gov.nga.common.rpc.TimeStampRequest;
import gov.nga.common.rpc.TimeStampResponse;
import gov.nga.common.rpc.message.QueryMessage;
import gov.nga.entities.art.ArtDataManager;
import gov.nga.integration.cspace.monitoring.GrpcTMSStats;
import io.grpc.stub.StreamObserver;

@GrpcService 
public class ExhibitionQueryService extends ArtDataQuerierGrpc.ArtDataQuerierImplBase
{
	private static final Logger LOG = LoggerFactory.getLogger(ExhibitionQueryService.class);
	
	@Autowired
	private ArtDataManager artDataManager;
	
	@Autowired
	private GrpcTMSStats statsMonitor;
	
	private ConstituentHelper constHlpr;
	private ExhibitionHelper exhHlpr;
	private MediaHelper mediaHlpr;
	private LocationHelper locationHlpr;
	private PlaceHelper placeHlpr;
	private ArtObjectHelper artObjHlpr;
	private DepartmentHelper departmentHlpr;
	private CacheHelper cacheHlpr;
	private SuggestionHelper suggHlpr;
	

    @PostConstruct
    public void postConstruct() 
    {
    	artObjHlpr = new ArtObjectHelper(artDataManager, statsMonitor);
    	constHlpr = new ConstituentHelper(artDataManager, statsMonitor);
    	exhHlpr = new ExhibitionHelper(artDataManager, statsMonitor);
    	mediaHlpr = new MediaHelper(artDataManager, statsMonitor);
    	locationHlpr = new LocationHelper(artDataManager, statsMonitor);
    	placeHlpr = new PlaceHelper(artDataManager, statsMonitor);
    	suggHlpr = new SuggestionHelper(artDataManager, statsMonitor);
    	cacheHlpr = new CacheHelper(artDataManager, statsMonitor);
    	departmentHlpr = new DepartmentHelper(artDataManager, statsMonitor);
    }
    
    @Override    
	public void fetchExhibitions(final FetchByIDsQuery request,
			final StreamObserver<ExhibitionObjectResult> responseObserver)
	{
    	try
		{
			exhHlpr.fetchExhibitions(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
		
	}
    
	@Override
	public void searchExhibitions(final QueryMessage request,
			final StreamObserver<ExhibitionQueryResult> responseObserver)
	{
		try
		{
			exhHlpr.searchExhibitions(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
		
	}
	
	@Override
	public void fetchExhibitionArtObjects(final ExhibitionObjectQuery request,
			final StreamObserver<ExhibitionArtObjectResult> responseObserver)
	{
		try
		{
			exhHlpr.fetchArtObjects(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
	
	@Override
	public void fetchExhibitionConstituents(final ExhibitionObjectQuery request,
			final StreamObserver<ExhibitionConstituentResult> responseObserver)
	{
		try
		{
			exhHlpr.fetchConstituents(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
    
	@Override
	public void searchLocations(final QueryMessage request,
			final StreamObserver<LocationQueryResult> responseObserver)
	{
		try
		{
			locationHlpr.searchLocations(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
		
	}
    
	@Override
	public void fetchConstituents(final FetchByIDsQuery request,
			final StreamObserver<ConstituentsObjectResult> responseObserver)
	{
		try
		{
			constHlpr.fetchConstituents(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
		
	}
    
	@Override
	public void searchConstituents(final QueryMessage request,
			final StreamObserver<ConstituentQueryResult> responseObserver)
	{
		try
		{
			constHlpr.searchConstituents(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
		
	}

	@Override
	public void getMedia(final FetchByStringsQuery request,
			final StreamObserver<MediaObjectResult> responseObserver)
	{
		try
		{
			mediaHlpr.getMedia(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
	
	@Override
	public void fetchLocations(final FetchByIDsQuery request,
			final StreamObserver<LocationObjectResult> responseObserver)
	{
		try
		{
			locationHlpr.fetchLocations(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}

	@Override
	public void getPlaceByPlaceKey(final FetchByStringsQuery request, 
			final StreamObserver<PlaceObjectResult> responseObserver)
	{
		try
		{
			placeHlpr.getPlaceByPlaceKey(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
	
	@Override
	public void getPlaceByTMSLocationID(final FetchByIDsQuery request, 
			final StreamObserver<PlaceObjectResult> responseObserver) 
	{
		try
		{
			placeHlpr.getPlaceByTMSLocationID(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
	
	@Override
	public void fetchArtObjects(final FetchByIDsQuery request, 
			final StreamObserver<ArtObjectObjectResult> responseObserver)
	{
		try
		{
			artObjHlpr.fetchArtObjects(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
	
	@Override
	public void fetchRelatedWorks(final FetchByIDsQuery request, 
			final StreamObserver<ArtObjectQueryResult> responseObserver)
	{
		try
		{
			artObjHlpr.fetchRelatedWorks(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
	
	@Override
	public void fetchObjectsByRelationships(final FetchByIDsQuery request, 
			final StreamObserver<ArtObjectQueryResult> responseObserver)
	{
		try
		{
			artObjHlpr.fetchObjectsByRelationships(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
	
	@Override
	public void searchArtObjects(final QueryMessage request, 
			final StreamObserver<ArtObjectQueryResult> responseObserver)
	{
		try
		{
			artObjHlpr.searchArtObjects(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
	
	@Override
	public void getLastSyncTime(final TimeStampRequest request,
					final StreamObserver<TimeStampResponse> responseObserver)
	{
		try
		{
			cacheHlpr.getLastSyncTime(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
	
	@Override
	public void fetchAllLocationIds(final CacheFetchQuery request, 
					final StreamObserver<QueryResult> responseObserver)
	{
		try
		{
			cacheHlpr.fetchAllLocationIds(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
	
	@Override
	public void fetchDepartmentByID(final FetchByIDsQuery request, 
			final StreamObserver<DepartmentObjectResult> responseObserver)
	{
		try
		{
			departmentHlpr.getDepartmentByID(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
	
	@Override
	public void fetchDepartmentByCode(final FetchByStringsQuery request, 
			final StreamObserver<DepartmentObjectResult> responseObserver)
	{
		try
		{
			departmentHlpr.getDepartmentByCode(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
		
	@Override	
	public void fetchAllDepartmentsIds(final CacheFetchQuery request, 
			final StreamObserver<QueryResult> responseObserver)
	{
		try
		{
			cacheHlpr.fetchAllDepartmentIds(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
	
	@Override
	public void fetchAllExhibitionIds(final CacheFetchQuery request, 
					final StreamObserver<QueryResult> responseObserver)
	{
		try
		{
			cacheHlpr.fetchAllExhibitionIds(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
	
	@Override
	public void fetchAllConstituentIds(final CacheFetchQuery request, 
					final StreamObserver<QueryResult> responseObserver)
	{
		try
		{
			cacheHlpr.fetchAllConstituentIds(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
	
	@Override
	public void fetchAllArtObjectIds(final CacheFetchQuery request, 
					final StreamObserver<QueryResult> responseObserver)
	{
		try
		{
			cacheHlpr.fetchAllArtObjectIds(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
	
	@Override
	public void fetchImagesForArtObject(final FetchByIDsQuery request,
			final StreamObserver<NGAImageResult> responseObserver)
	{
		try
		{
			artObjHlpr.fetchImagesForObject(request, responseObserver);
		}
		catch (final Exception err)
		{
			LOG.error("Exception caught while processing request.", err);
		}
	}
	
	@Override
	public void getSuggestions(final SuggestArtDataQuery request, 
			final StreamObserver<ArtDataSuggestionResult> responseObserver)
	{
		suggHlpr.getSuggestions(request, responseObserver);
	}

}
