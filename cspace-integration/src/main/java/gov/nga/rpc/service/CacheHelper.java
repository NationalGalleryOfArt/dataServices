package gov.nga.rpc.service;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.Department;
import gov.nga.common.rpc.CacheFetchQuery;
import gov.nga.common.rpc.QueryResult;
import gov.nga.common.rpc.TimeStampRequest;
import gov.nga.common.rpc.TimeStampResponse;
import gov.nga.entities.art.ArtDataManager;
import gov.nga.utils.CollectionUtils;
import io.grpc.stub.StreamObserver;

public class CacheHelper extends TMSObjectHelper
{
	private static final Logger LOG = LoggerFactory.getLogger(CacheHelper.class);

	protected CacheHelper(ArtDataManager mgr) {
		super(mgr);
	}
	
	public void getLastSyncTime(final TimeStampRequest request,
			final StreamObserver<TimeStampResponse> responseObserver)
	{
		try
		{
			Long dateStamp = getManager().synchronizationFinishedAt();
			if (dateStamp == null) dateStamp = 0L;
			responseObserver.onNext(TimeStampResponse.newBuilder()
										.setTimestamp(dateStamp.longValue())
										.build());
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("getLastSyncTime(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}

	public void fetchAllDepartmentIds(final CacheFetchQuery request, 
			final StreamObserver<QueryResult> responseObserver)
	{
		try
		{
			final Collection<Long> ids = getManager().getArtDataCacher().getDepartmentMap().keySet();
			final QueryResult.Builder builder = QueryResult.newBuilder();
			builder.addAllIds(ids);
			builder.setTotalResults(ids.size());
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("fetchAllLocationIds(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}

	public void fetchAllLocationIds(final CacheFetchQuery request, 
			final StreamObserver<QueryResult> responseObserver)
	{
		try
		{
			final Collection<Long> ids = getManager().getArtDataCacher().getLocationsMap().keySet();
			final QueryResult.Builder builder = QueryResult.newBuilder();
			builder.addAllIds(ids);
			builder.setTotalResults(ids.size());
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("fetchAllLocationIds(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}

	public void fetchAllExhibitionIds(final CacheFetchQuery request, 
					final StreamObserver<QueryResult> responseObserver)
	{
		try
		{
			final Collection<Long> ids = getManager().getArtDataCacher().getExhibitionMap().keySet();
			final QueryResult.Builder builder = QueryResult.newBuilder();
			builder.addAllIds(ids);
			builder.setTotalResults(ids.size());
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("fetchAllExhibitionIds(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
	
	public void fetchAllConstituentIds(final CacheFetchQuery request, 
					final StreamObserver<QueryResult> responseObserver)
	{
		try
		{
			final Collection<Long> ids = getManager().getArtDataCacher().getConstituentMap().keySet();
			final QueryResult.Builder builder = QueryResult.newBuilder();
			builder.addAllIds(ids);
			builder.setTotalResults(ids.size());
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("fetchAllConstituentIds(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
	
	public void fetchAllArtObjectIds(final CacheFetchQuery request, 
			final StreamObserver<QueryResult> responseObserver)
	{
		try
		{
			final Collection<Long> ids = getManager().getArtDataCacher().getArtObjectMap().keySet();
			final QueryResult.Builder builder = QueryResult.newBuilder();
			builder.addAllIds(ids);
			builder.setTotalResults(ids.size());
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("fetchAllArtObjectIds(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
	
	
	
	
	
	
}
