package gov.nga.rpc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.Media;
import gov.nga.common.entities.art.QueryResultArtData;
import gov.nga.common.proto.messages.MediaMessageFactory;
import gov.nga.common.rpc.FetchByStringsQuery;
import gov.nga.common.rpc.MediaObjectResult;
import gov.nga.entities.art.ArtDataManager;
import gov.nga.integration.cspace.monitoring.GrpcTMSStats;
import gov.nga.integration.cspace.monitoring.GrpcTMSStats.TMSOperation;
import io.grpc.stub.StreamObserver;

public class MediaHelper extends TMSObjectHelper 
{
	private static final Logger LOG = LoggerFactory.getLogger(MediaHelper.class);

	protected MediaHelper(ArtDataManager mgr, final GrpcTMSStats statsMonitor) 
	{
		super(mgr, statsMonitor);
	}
	
	public void getMedia(final FetchByStringsQuery request,
			final StreamObserver<MediaObjectResult> responseObserver)
	{
		try
		{
			final QueryResultArtData<Media> rslts = getQueryManager()
					.getMediaByEntityRelationship(request.getParam(0));
			for (Media obj: rslts.getResults())
			{
				responseObserver.onNext(MediaObjectResult.newBuilder().setMedia(
						MediaMessageFactory.createMessage(obj)).build());
			}
			responseObserver.onCompleted();
			reportCall(TMSOperation.MEDIA_FETCH, rslts.getResults().size());
		}
		catch (final Exception err)
		{
			LOG.error("getMedia(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}

}
