package gov.nga.rpc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.Location;
import gov.nga.common.entities.art.QueryResultArtData;
import gov.nga.common.proto.messages.LocationMessageFactory;
import gov.nga.common.rpc.ExhibitionQueryResult;
import gov.nga.common.rpc.FetchByIDsQuery;
import gov.nga.common.rpc.LocationObjectResult;
import gov.nga.common.rpc.LocationQueryResult;
import gov.nga.common.rpc.message.QueryMessage;
import gov.nga.common.search.SortHelper;
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
	
	protected void searchLocations(final QueryMessage request,
			final StreamObserver<LocationQueryResult> responseObserver)
	{

		LOG.debug("searchLocations() called..."); 
		try
		{
			final QueryResultArtData<Location> rslts = processRequest(request);
			final LocationQueryResult.Builder builder = LocationQueryResult.newBuilder();
			if (rslts == null)
			{
				builder.setTotalResults(0);
			}
			else
			{
				builder.setTotalResults(rslts.getResultCount());
				for (Location rt: rslts.getResults())
				{
					builder.addIds(rt.getLocationID());
				}
			}
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("searchLocations(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
	
	private QueryResultArtData<Location> processRequest(final QueryMessage request)
	{

		gov.nga.common.rpc.api.QueryMessage<Location> args = 
				getQueryMessagePOJO(Location.class, Location.SORT.class, Location.SEARCH.class, request);
		
		if (args.getObjectIDs().size() > 0)
		{
			LOG.debug("Making fetch call with no sort: " + args.getObjectIDs());
			return getQueryManager().fetchByLocationIDs(args.getObjectIDs());			
		}
		
		LOG.debug("this is not a fetch call");
		if (args.getSrchHlpr() != null)
		{
			if (args.getOrder() == null)
			{
				LOG.debug("Making search call with no sort: ");
				return getQueryManager().searchLocations(args.getSrchHlpr(), args.getPgn(), null);
			}
			else
			{
				LOG.debug(String.format("Making search call with sort: %s", args.getOrder().getSortOrder()));
				return getQueryManager().searchLocations(args.getSrchHlpr(), args.getPgn(), new SortHelper<Location>(args.getOrder()));
			}
			
		}
		return null;
	}

}
