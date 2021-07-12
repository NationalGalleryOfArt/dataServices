package gov.nga.rpc.service;

import net.devh.boot.grpc.server.service.GrpcService;


import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nga.common.rpc.ArtDataQuerierGrpc;
import gov.nga.common.rpc.ArtObjectObjectResult;
import gov.nga.common.rpc.ArtObjectQueryResult;
import gov.nga.common.rpc.CacheFetchQuery;
import gov.nga.common.rpc.ConstituentQueryMessages.ConstituentsObjectResult;
import gov.nga.common.rpc.ConstituentQueryMessages.ConstituentQueryResult;
import gov.nga.common.rpc.ExhibitionObjectResult;
import gov.nga.common.rpc.ExhibitionQueryResult;
import gov.nga.common.rpc.FetchByIDsQuery;
import gov.nga.common.rpc.FetchByStringsQuery;
import gov.nga.common.rpc.LocationObjectResult;
import gov.nga.common.rpc.MediaObjectResult;
import gov.nga.common.rpc.PlaceObjectResult;
import gov.nga.common.rpc.QueryResult;
import gov.nga.common.rpc.TimeStampRequest;
import gov.nga.common.rpc.TimeStampResponse;
import gov.nga.common.rpc.message.QueryMessage;
import gov.nga.entities.art.ArtDataManager;
import io.grpc.stub.StreamObserver;

@GrpcService 
public class ExhibitionQueryService extends ArtDataQuerierGrpc.ArtDataQuerierImplBase
{
	private static final Logger LOG = LoggerFactory.getLogger(ExhibitionQueryService.class);
	
	@Autowired
	private ArtDataManager artDataManager;
	
	private ConstituentHelper constHlpr;
	private ExhibitionHelper exhHlpr;
	private MediaHelper mediaHlpr;
	private LocationHelper locationHlpr;
	private PlaceHelper placeHlpr;
	private ArtObjectHelper artObjHlpr;
	private CacheHelper cacheHlpr;
	

    @PostConstruct
    public void postConstruct() 
    {
    	artObjHlpr = new ArtObjectHelper(artDataManager);
    	constHlpr = new ConstituentHelper(artDataManager);
    	exhHlpr = new ExhibitionHelper(artDataManager);
    	mediaHlpr = new MediaHelper(artDataManager);
    	locationHlpr = new LocationHelper(artDataManager);
    	placeHlpr = new PlaceHelper(artDataManager);
    	cacheHlpr = new CacheHelper(artDataManager);
    }
    
    @Override    
	public void fetchExhibitions(final FetchByIDsQuery request,
			final StreamObserver<ExhibitionObjectResult> responseObserver)
	{
		exhHlpr.fetchExhibitions(request, responseObserver);
		
	}
    
	@Override
	public void searchExhibitions(final QueryMessage request,
			final StreamObserver<ExhibitionQueryResult> responseObserver)
	{
		exhHlpr.searchExhibitions(request, responseObserver);
		
	}
    
	@Override
	public void fetchConstituents(final FetchByIDsQuery request,
			final StreamObserver<ConstituentsObjectResult> responseObserver)
	{
		constHlpr.fetchConstituents(request, responseObserver);
		
	}
    
	@Override
	public void searchConstituents(final QueryMessage request,
			final StreamObserver<ConstituentQueryResult> responseObserver)
	{
		constHlpr.searchConstituents(request, responseObserver);
		
	}

	@Override
	public void getMedia(final FetchByStringsQuery request,
			final StreamObserver<MediaObjectResult> responseObserver)
	{
		mediaHlpr.getMedia(request, responseObserver);
	}
	
	@Override
	public void fetchLocations(final FetchByIDsQuery request,
			final StreamObserver<LocationObjectResult> responseObserver)
	{
		locationHlpr.fetchLocations(request, responseObserver);
	}

	@Override
	public void getPlaceByPlaceKey(final FetchByStringsQuery request, 
			final StreamObserver<PlaceObjectResult> responseObserver)
	{
		placeHlpr.getPlaceByPlaceKey(request, responseObserver);
	}
	
	@Override
	public void getPlaceByTMSLocationID(final FetchByIDsQuery request, 
			final StreamObserver<PlaceObjectResult> responseObserver) 
	{
		placeHlpr.getPlaceByTMSLocationID(request, responseObserver);
	}
	
	@Override
	public void fetchArtObjects(final FetchByIDsQuery request, 
			final StreamObserver<ArtObjectObjectResult> responseObserver)
	{
		artObjHlpr.fetchArtObjects(request, responseObserver);
	}
	
	@Override
	public void fetchRelatedWorks(final FetchByIDsQuery request, 
			final StreamObserver<ArtObjectQueryResult> responseObserver)
	{
		artObjHlpr.fetchRelatedWorks(request, responseObserver);
	}
	
	@Override
	public void fetchObjectsByRelationships(final FetchByIDsQuery request, 
			final StreamObserver<ArtObjectQueryResult> responseObserver)
	{
		artObjHlpr.fetchObjectsByRelationships(request, responseObserver);
	}
	
	@Override
	public void searchArtObjects(final QueryMessage request, 
			final StreamObserver<ArtObjectQueryResult> responseObserver)
	{
		artObjHlpr.searchArtObjects(request, responseObserver);
	}
	
	@Override
	public void getLastSyncTime(final TimeStampRequest request,
					final StreamObserver<TimeStampResponse> responseObserver)
	{
		cacheHlpr.getLastSyncTime(request, responseObserver);
	}
	
	@Override
	public void fetchAllLocationIds(final CacheFetchQuery request, 
					final StreamObserver<QueryResult> responseObserver)
	{
		cacheHlpr.fetchAllLocationIds(request, responseObserver);
	}
	
	@Override
	public void fetchAllExhibitionIds(final CacheFetchQuery request, 
					final StreamObserver<QueryResult> responseObserver)
	{
		cacheHlpr.fetchAllExhibitionIds(request, responseObserver);
	}
	
	@Override
	public void fetchAllConstituentIds(final CacheFetchQuery request, 
					final StreamObserver<QueryResult> responseObserver)
	{
		cacheHlpr.fetchAllConstituentIds(request, responseObserver);
	}
	
	@Override
	public void fetchAllArtObjectIds(final CacheFetchQuery request, 
					final StreamObserver<QueryResult> responseObserver)
	{
		cacheHlpr.fetchAllArtObjectIds(request, responseObserver);
	}

}
