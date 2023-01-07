package gov.nga.rpc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.Department;
import gov.nga.common.entities.art.Place;
import gov.nga.common.entities.art.QueryResultArtData;
import gov.nga.common.proto.messages.DepartmentMessageFactory;
import gov.nga.common.proto.messages.PlaceMessageFactory;
import gov.nga.common.rpc.FetchByIDsQuery;
import gov.nga.common.rpc.FetchByStringsQuery;
import gov.nga.common.rpc.DepartmentObjectResult;
import gov.nga.entities.art.ArtDataManager;
import gov.nga.integration.cspace.monitoring.GrpcTMSStats;
import gov.nga.integration.cspace.monitoring.GrpcTMSStats.TMSOperation;
import io.grpc.stub.StreamObserver;

public class DepartmentHelper  extends TMSObjectHelper 
{
	private static final Logger LOG = LoggerFactory.getLogger(DepartmentHelper.class);

	protected DepartmentHelper(ArtDataManager mgr, final GrpcTMSStats statsMonitor) 
	{
		super(mgr, statsMonitor);
	}
	
	public void getDepartmentByCode(final FetchByStringsQuery request, 
			final StreamObserver<DepartmentObjectResult> responseObserver)
	{
		try
		{
			final QueryResultArtData<Department> rslts = getQueryManager().fetchDepartmentByCodes(
						request.getParamList());
			for(Department obj: rslts.getResults())
			{
				responseObserver.onNext(DepartmentObjectResult.newBuilder().setDepartment(
						DepartmentMessageFactory.convertToMessage(obj)).build());
			}
			responseObserver.onCompleted();
			reportCall(TMSOperation.DEPARTMENT_FETCH, rslts.getResults().size());
		}
		catch (final Exception err)
		{
			LOG.error("getDepartmentByCode(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
	
	public void getDepartmentByID(final FetchByIDsQuery request, 
			final StreamObserver<DepartmentObjectResult> responseObserver) 
	{
		try
		{
			final QueryResultArtData<Department> rslts = getQueryManager().fetchDepartmentByIDs(
					request.getIdsList());
			for(Department obj: rslts.getResults())
			{
				responseObserver.onNext(DepartmentObjectResult.newBuilder().setDepartment(
						DepartmentMessageFactory.convertToMessage(obj)).build());
			}
			responseObserver.onCompleted();
			reportCall(TMSOperation.DEPARTMENT_FETCH, rslts.getResults().size());
		}
		catch (final Exception err)
		{
			LOG.error("getDepartmentByID(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}

}
