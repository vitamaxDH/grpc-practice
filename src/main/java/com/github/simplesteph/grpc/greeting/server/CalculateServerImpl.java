package com.github.simplesteph.grpc.greeting.server;

import com.proto.calculate.AverageRequest;
import com.proto.calculate.AverageResponse;
import com.proto.calculate.CalculateServiceGrpc;
import com.proto.calculate.SquareRootRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class CalculateServerImpl extends CalculateServiceGrpc.CalculateServiceImplBase {

    @Override
    public void sum(com.proto.calculate.SumRequest request, StreamObserver<com.proto.calculate.SumResponse> responseObserver) {
        com.proto.calculate.Summing sum = request.getSum();
        int first = sum.getFirst();
        int second = sum.getSecond();

        int result = first + second;

        com.proto.calculate.SumResponse sumResponse = com.proto.calculate.SumResponse.newBuilder()
                .setResult(result)
                .build();

        responseObserver.onNext(sumResponse);

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<AverageRequest> average(StreamObserver<AverageResponse> responseObserver) {
        System.out.println("Request received");
        return new StreamObserver<>() {

            double total;
            int count;

            @Override
            public void onNext(AverageRequest value) {
                total += value.getNumber();
                count++;
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(com.proto.calculate.AverageResponse.newBuilder()
                        .setResult(total / count)
                        .build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<com.proto.calculate.SquareRootResponse> responseObserver) {
        Integer number = request.getNumber();

        if (number >= 0) {
            double numberRoot = Math.sqrt(number);
            responseObserver.onNext(com.proto.calculate.SquareRootResponse.newBuilder()
                    .setNumberRoot(numberRoot)
                    .build());
            responseObserver.onCompleted();
        } else {
            // we construct the exception
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("The number being sent is not positive")
                            .augmentDescription("Number set: " + number)
                            .asRuntimeException()
            );
        }
    }
}
