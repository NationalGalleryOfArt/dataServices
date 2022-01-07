package gov.nga.rpc.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.Constituent;
import gov.nga.common.entities.art.QueryResultArtData;
import gov.nga.common.proto.messages.ConstituentMessageFactory;
import gov.nga.common.rpc.FetchByIDsQuery;
import gov.nga.common.rpc.ConstituentQueryMessages.ConstituentQueryResult;
import gov.nga.common.rpc.ConstituentQueryMessages.ConstituentsObjectResult;
import gov.nga.common.rpc.message.QueryMessage;
import gov.nga.common.search.SearchHelper;
import gov.nga.entities.art.ArtDataManager;
import io.grpc.stub.StreamObserver;

public class ConstituentHelper extends TMSObjectHelper 
{
	private static final Logger LOG = LoggerFactory.getLogger(ConstituentHelper.class);

	protected ConstituentHelper(ArtDataManager mgr) 
	{
		super(mgr);
	}
	
	protected void fetchConstituents(final FetchByIDsQuery request,
			final StreamObserver<ConstituentsObjectResult> responseObserver)
	{
		LOG.info("fetchConstituents() called..."); 
		try
		{
			final ConstituentsObjectResult.Builder builder = ConstituentsObjectResult.newBuilder();
			final List<Constituent> constituents = getQueryManager().fetchByConstituentIDs(request.getIdsList()).getResults();
			for (Constituent rslt: constituents)
			{
				builder.setConstituent(ConstituentMessageFactory.convertConstituentToMessage(rslt));
				responseObserver.onNext(builder.build());
			}
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("fetchConstituents(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
	protected void searchConstituents(final QueryMessage request,
			final StreamObserver<ConstituentQueryResult> responseObserver)
	{

		LOG.info("searchConstituents() called..."); 
		try
		{
			final QueryResultArtData<Constituent> rslt = processRequest(request);
			final ConstituentQueryResult.Builder builder = ConstituentQueryResult.newBuilder();
			if (rslt == null)
			{
				builder.setTotalResults(0);
			}
			else
			{
				builder.setTotalResults(rslt.getResultCount());
				for (Constituent c: rslt.getResults())
				{
					builder.addIds(c.getConstituentID());
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
	
	private QueryResultArtData<Constituent> processRequest(final QueryMessage request)
	{

		gov.nga.common.rpc.impl.QueryMessage<Constituent> args = 
				getQueryMessagePOJO(Constituent.class, Constituent.SORT.class, Constituent.SEARCH.class, request);
		
		if (args.getObjectIDs().size() > 0)
		{
			if (args.getOrder() == null)
			{
				LOG.info("Making fetch call with no sort: " + args.getObjectIDs());
				return getQueryManager().fetchByConstituentIDs(args.getObjectIDs());
			}
			else
			{
				LOG.info(String.format("Making fetch call with sort %s: %s", args.getObjectIDs(), args.getOrder().getSortOrder()));
				return getQueryManager().fetchByConstituentIDs(args.getObjectIDs(), 
						args.getOrder().getSortOrder().toArray(new Constituent.SORT[] {}));
			}
			
		}
		LOG.info("this is not a fetch call");
		if (args.getSrchHlpr() != null)
		{
			if (args.getOrder() == null)
			{
				LOG.info("Making search call with no sort: ");
				return getQueryManager().searchConstituents(args.getSrchHlpr(), args.getPgn(), null);
			}
			else
			{
				LOG.info(String.format("Making search call with sort: %s", args.getOrder().getSortOrder()));
				return getQueryManager().searchConstituents(args.getSrchHlpr(), args.getPgn(), null, 
						args.getOrder().getSortOrder().toArray(new Constituent.SORT[] {}));
			}
			
		}
		return null;
	}
	
	

}
