package gov.nga.rpc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.Place;
import gov.nga.common.entities.art.QueryResultArtData;
import gov.nga.common.proto.messages.PlaceMessageFactory;
import gov.nga.common.rpc.FetchByIDsQuery;
import gov.nga.common.rpc.FetchByStringsQuery;
import gov.nga.common.rpc.PlaceObjectResult;
import gov.nga.entities.art.ArtDataManager;
import io.grpc.stub.StreamObserver;

public class PlaceHelper extends TMSObjectHelper 
{
	private static final Logger LOG = LoggerFactory.getLogger(PlaceHelper.class);

	protected PlaceHelper(ArtDataManager mgr) 
	{
		super(mgr);
	}

	public void getPlaceByPlaceKey(final FetchByStringsQuery request, 
			final StreamObserver<PlaceObjectResult> responseObserver)
	{
		try
		{
			final QueryResultArtData<Place> rslts = getQueryManager().fetchByPlaceKey(
						request.getParam(0));
			for(Place obj: rslts.getResults())
			{
				responseObserver.onNext(PlaceObjectResult.newBuilder().setPlace(
						PlaceMessageFactory.createMessage(obj)).build());
			}
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("getPlaceByPlaceKey(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
	
	public void getPlaceByTMSLocationID(final FetchByIDsQuery request, 
			final StreamObserver<PlaceObjectResult> responseObserver) 
	{
		try
		{
			final QueryResultArtData<Place> rslts = getQueryManager().fetchByTMSLocationID(
					request.getIds(0));
			for(Place obj: rslts.getResults())
			{
				responseObserver.onNext(PlaceObjectResult.newBuilder().setPlace(
						PlaceMessageFactory.createMessage(obj)).build());
			}
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("getMedia(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
}
