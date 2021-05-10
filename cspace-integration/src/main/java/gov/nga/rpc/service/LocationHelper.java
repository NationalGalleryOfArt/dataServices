package gov.nga.rpc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.Location;
import gov.nga.common.entities.art.QueryResultArtData;
import gov.nga.common.proto.messages.LocationMessageFactory;
import gov.nga.common.rpc.FetchByIDsQuery;
import gov.nga.common.rpc.LocationObjectResult;
import gov.nga.entities.art.ArtDataManager;
import io.grpc.stub.StreamObserver;

public class LocationHelper extends TMSObjectHelper 
{
	private static final Logger LOG = LoggerFactory.getLogger(LocationHelper.class);

	protected LocationHelper(ArtDataManager mgr) 
	{
		super(mgr);
	}
	
	public void fetchLocations(final FetchByIDsQuery request,
			final StreamObserver<LocationObjectResult> responseObserver)
	{
		try
		{
			final QueryResultArtData<Location> rslts = getQueryManager()
					.fetchByLocationIDs(request.getIdsList());
			for (Location obj: rslts.getResults())
			{
				responseObserver.onNext(LocationObjectResult.newBuilder().setLocation(
						LocationMessageFactory.getMessage(obj)).build());
			}
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("fetchLocations(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}

}
