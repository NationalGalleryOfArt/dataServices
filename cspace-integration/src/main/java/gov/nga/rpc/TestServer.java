package gov.nga.rpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService 
public class TestServer extends GreeterGrpc.GreeterImplBase 
{

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) 
    {
      HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
      responseObserver.onNext(reply);
      responseObserver.onCompleted();
    }
}

