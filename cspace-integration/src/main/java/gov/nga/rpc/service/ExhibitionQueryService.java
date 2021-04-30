package gov.nga.rpc.service;

import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nga.common.entities.art.Constituent;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.proto.messages.ConstituentMessageFactory;
import gov.nga.common.proto.messages.ExhibitionMessageFactory;
import gov.nga.common.rpc.ArtDataQuerierGrpc;
import gov.nga.common.rpc.ConstituentsObjectResult;
import gov.nga.common.rpc.ConstituentQueryResult;
import gov.nga.common.rpc.ExhibitionQueryResult;
import gov.nga.common.rpc.ExhibitionTestFetchQuery;
import gov.nga.common.rpc.TestFetchIDsQuery;
import gov.nga.common.rpc.TestFetchQuery;
import gov.nga.entities.art.ArtDataManager;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

@GrpcService 
public class ExhibitionQueryService extends ArtDataQuerierGrpc.ArtDataQuerierImplBase
{
	private static final Logger LOG = LoggerFactory.getLogger(ExhibitionQueryService.class);
	
	@Autowired
	private ArtDataManager artDataManager;
	
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
}
