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
	

    @PostConstruct
    public void postConstruct() 
    {
    	constHlpr = new ConstituentHelper(artDataManager);
    	exhHlpr = new ExhibitionHelper(artDataManager);
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

	/*
	@Override
	public void fetchTestConstituents(final TestFetchIDsQuery request,
			final StreamObserver<ConstituentsObjectResult> responseObserver)
	{
		LOG.info("fetchTestConstituents() called..."); 
		try
		{
			final ConstituentsObjectResult.Builder builder = ConstituentsObjectResult.newBuilder();
			final List<Constituent> constituents = artDataManager.getArtDataQuerier().fetchByConstituentIDs(request.getIdsList()).getResults();
			for (Constituent rslt: constituents)
			{
				builder.setConstituent(ConstituentMessageFactory.convertConstituentToMessage(rslt));
				responseObserver.onNext(builder.build());
			}
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("fetchTestConstituents(): Exception Caught", err);
			responseObserver.onError(err);
		}
		
	}
	
	@Override
	public void fetchTestConstituentData(final TestFetchQuery request, 
					final StreamObserver<ConstituentQueryResult> responseObserver)
	{
		LOG.info("fetchTestConstituentData() called..."); 
		try
		{
			final int countLimit = request.getNumboerOfObjects();
			int count = 0;
			final ConstituentQueryResult.Builder builder = ConstituentQueryResult.newBuilder();
			final List<Constituent> exhibits = artDataManager.getArtDataCacher().getConstituentsRaw();
			for (; count < countLimit; count++)
			{
				builder.addIds(exhibits.get(count).getConstituentID());
				//builder.addResults(ConstituentMessageFactory.convertConstituentToMessage(exhibits.get(count)));
			}
			builder.setTotalResults(count);
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("fetchTestConstituentData(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}

	@Override
	public void fetchTestExhibitionData(final ExhibitionTestFetchQuery request,
					final StreamObserver<ExhibitionQueryResult> responseObserver)
	{
		LOG.info("fetchTestExhibitionData() called..."); 
		try
		{
			final int countLimit = request.getNumberOfExhibits();
			int count = 0;
			final ExhibitionQueryResult.Builder builder = ExhibitionQueryResult.newBuilder();
			final List<Exhibition> exhibits = artDataManager.getArtDataCacher().getExhibitionsRaw();
			for (; count < countLimit; count++)
			{
				builder.addResults(ExhibitionMessageFactory.createMessage(exhibits.get(count)));
			}
			builder.setTotalResults(count);
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("fetchTestExhibitionData(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
	*/
}
