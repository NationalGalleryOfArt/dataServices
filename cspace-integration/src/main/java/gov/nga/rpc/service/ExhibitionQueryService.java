package gov.nga.rpc.service;

import net.devh.boot.grpc.server.service.GrpcService;


import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nga.common.rpc.ArtDataQuerierGrpc;
import gov.nga.common.rpc.ConstituentQueryMessages.ConstituentsObjectResult;
import gov.nga.common.rpc.ConstituentQueryMessages.ConstituentQueryResult;
import gov.nga.common.rpc.ExhibitionObjectResult;
import gov.nga.common.rpc.ExhibitionQueryResult;
import gov.nga.common.rpc.FetchByIDsQuery;
import gov.nga.common.rpc.FetchByStringsQuery;
import gov.nga.common.rpc.LocationObjectResult;
import gov.nga.common.rpc.MediaObjectResult;
import gov.nga.common.rpc.PlaceObjectResult;
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
	

    @PostConstruct
    public void postConstruct() 
    {
    	constHlpr = new ConstituentHelper(artDataManager);
    	exhHlpr = new ExhibitionHelper(artDataManager);
    	mediaHlpr = new MediaHelper(artDataManager);
    	locationHlpr = new LocationHelper(artDataManager);
    	placeHlpr = new PlaceHelper(artDataManager);
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
}
