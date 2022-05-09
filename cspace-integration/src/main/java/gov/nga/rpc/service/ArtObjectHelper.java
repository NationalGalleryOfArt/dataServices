package gov.nga.rpc.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.ArtObject;
import gov.nga.common.entities.art.Constituent;
import gov.nga.common.entities.art.QueryResultArtData;
import gov.nga.common.imaging.NGAImage;
import gov.nga.common.proto.messages.ArtObjectMessageFactory;
import gov.nga.common.proto.messages.NGAImageMessageFactory;
import gov.nga.common.rpc.FetchByIDsQuery;
import gov.nga.common.rpc.ArtObjectObjectResult;
import gov.nga.common.rpc.ArtObjectQueryResult;
import gov.nga.common.rpc.NGAImageResult;
import gov.nga.common.rpc.message.QueryMessage;
import gov.nga.entities.art.ArtDataManager;
import gov.nga.utils.CollectionUtils;
import io.grpc.stub.StreamObserver;

public class ArtObjectHelper extends TMSObjectHelper 
{
	private static final Logger LOG = LoggerFactory.getLogger(ArtObjectHelper.class);

	protected ArtObjectHelper(ArtDataManager mgr) 
	{
		super(mgr);
	}
	
	protected void fetchRelatedWorks(final FetchByIDsQuery request, 
			final StreamObserver<ArtObjectQueryResult> responseObserver)
	{
		try
		{
			final List<Long> rslts = CollectionUtils.newArrayList();
			if (request.getIdsCount() > 0)
			{
				ArtObject baseO = getQueryManager().fetchByObjectID(request.getIds(0)).getFirstResult();
				QueryResultArtData<ArtObject> results = getQueryManager().fetchRelatedWorks(baseO, null);
				for (ArtObject obj: results.getResults())
				{
					rslts.add(obj.getObjectID());
				}
			}
			responseObserver.onNext(ArtObjectQueryResult.newBuilder()
					.setTotalResults(rslts.size())
					.addAllIds(rslts).build());
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("fetchRelatedWorks(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
	
	protected void fetchObjectsByRelationships(final FetchByIDsQuery request, 
			final StreamObserver<ArtObjectQueryResult> responseObserver)
	{
		try
		{
			if (request.getIdsCount() > 0)
			{
				final QueryResultArtData<Constituent> rslt = getQueryManager().fetchByConstituentIDs(
								request.getIdsList());
				final Map<Long, ArtObject> aos = CollectionUtils.newHashMap();
				if (rslt.getResultCount() > 0)
				{
					for (Constituent c: rslt.getResults())
					{
	                    List<ArtObject> works = getQueryManager().fetchByObjectIDs(c.getWorksIDs(Constituent.ARTISTWORKRELATIONS.ALLWORKS.getDataLabel())).getResults();
	                    for (ArtObject o : works) {
	                        aos.put(o.getObjectID(), o);
	                    }
					}
				}
				responseObserver.onNext(ArtObjectQueryResult.newBuilder()
									.setTotalResults(aos.size())
									.addAllIds(aos.keySet()).build());
			}
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("fetchObjectsByRelationships(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
	
	protected void fetchArtObjects(final FetchByIDsQuery request,
			final StreamObserver<ArtObjectObjectResult> responseObserver)
	{
		LOG.debug("fetchArtObjects() called..."); 
		try
		{
			final ArtObjectObjectResult.Builder builder = ArtObjectObjectResult.newBuilder();
			final List<ArtObject> rslts = getQueryManager().fetchByObjectIDs(request.getIdsList()).getResults();
			for (ArtObject rslt: rslts)
			{
				try
				{
					builder.setArtobject(ArtObjectMessageFactory.getMessage(rslt));
					responseObserver.onNext(builder.build());
					LOG.info(String.format("fetchArtObjects() sending %s", rslt));
				}
				catch (final Exception err)
				{
					LOG.error("Could not create message object for ArtObject: " + rslt, err);
				}
			}
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("fetchArtObjects(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
	protected void searchArtObjects(final QueryMessage request,
			final StreamObserver<ArtObjectQueryResult> responseObserver)
	{

		LOG.debug("searchArtObjects() called..."); 
		try
		{
			final QueryResultArtData<ArtObject> rslt = processRequest(request);
			final ArtObjectQueryResult.Builder builder = ArtObjectQueryResult.newBuilder();
			if (rslt == null)
			{
				builder.setTotalResults(0);
			}
			else
			{
				builder.setTotalResults(rslt.getResultCount());
				for (ArtObject c: rslt.getResults())
				{
					builder.addIds(c.getObjectID());
				}
			}
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("searchConstituents(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
	
	protected void fetchImagesForObject(final FetchByIDsQuery request,
			final StreamObserver<NGAImageResult> responseObserver)
	{
		LOG.debug("fetchImagesForObject() called..."); 
		try
		{
			final NGAImageResult.Builder builder = NGAImageResult.newBuilder();
			final List<NGAImage> rslts = getQueryManager().fetchImagesForObject(request.getIdsList().get(0)).getResults();
			for (NGAImage rslt: rslts)
			{
				try
				{
					builder.setImage(NGAImageMessageFactory.convertToMessage(rslt));
					responseObserver.onNext(builder.build());
					LOG.debug(String.format("fetchImagesForObject() sending %s", rslt));
				}
				catch (final Exception err)
				{
					LOG.error("Could not create message object for NGAImage: " + rslt, err);
				}
			}
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("fetchImagesForObject(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
	
	private QueryResultArtData<ArtObject> processRequest(final QueryMessage request)
	{

		gov.nga.common.rpc.api.QueryMessage<ArtObject> args = 
				getQueryMessagePOJO(ArtObject.class, ArtObject.SORT.class, ArtObject.SEARCH.class, request);
		
		if (args.getObjectIDs().size() > 0)
		{
			if (args.getOrder() == null)
			{
				LOG.debug("Making fetch call with no sort: " + args.getObjectIDs());
				return getQueryManager().fetchByObjectIDs(args.getObjectIDs());
			}
			else
			{
				LOG.debug(String.format("Making fetch call with sort %s: %s", args.getObjectIDs(), args.getOrder().getSortOrder()));
				return getQueryManager().fetchByObjectIDs(args.getObjectIDs(), 
						args.getOrder().getSortOrder().toArray(new ArtObject.SORT[] {}));
			}
			
		}
		LOG.debug("this is not a fetch call");
		if (args.getSrchHlpr() != null)
		{
			if (args.getOrder() == null)
			{
				LOG.debug("Making search call with no sort: ");
				return getQueryManager().searchArtObjects(args.getSrchHlpr(), args.getPgn(), null);
			}
			else
			{
				LOG.debug(String.format("Making search call with sort: %s", args.getOrder().getSortOrder()));
				return getQueryManager().searchArtObjects(args.getSrchHlpr(), args.getPgn(), null, 
						args.getOrder().getSortOrder().toArray(new ArtObject.SORT[] {}));
			}
			
		}
		return null;
	}

}
