package com.github.simplesteph.grpc.greeting.client;

import com.proto.calculate.AverageRequest;
import com.proto.calculate.AverageResponse;
import com.proto.calculate.CalculateServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculateClient {

    ManagedChannel channel;

    public void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9999)
                .usePlaintext()
                .build();

//        calculateSum(channel);
//        calculateAvg(channel);
        doErrorCall(channel);
    }


    private void calculateSum(ManagedChannel channel) {
        CalculateServiceGrpc.CalculateServiceBlockingStub calculateClient = CalculateServiceGrpc.newBlockingStub(channel);

        int first = 5;
        int second = 10;
        com.proto.calculate.Summing summing = com.proto.calculate.Summing.newBuilder()
                .setFirst(first)
                .setSecond(second)
                .build();
        System.out.printf("Requested first : %d, second : %d \n", first, second);

        com.proto.calculate.SumRequest sumRequest = com.proto.calculate.SumRequest.newBuilder()
                .setSum(summing)
                .build();

        com.proto.calculate.SumResponse sum = calculateClient.sum(sumRequest);

        System.out.println(sum.getResult());
    }

    private void calculateAvg(ManagedChannel channel) {
        CalculateServiceGrpc.CalculateServiceStub asyncClient = CalculateServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<AverageRequest> averageObserver = asyncClient.average(new StreamObserver<>() {
            @Override
            public void onNext(AverageResponse value) {
                System.out.println("Received value : " + value);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed");
                latch.countDown();
            }
        });

        setNumber(averageObserver, 1);
        setNumber(averageObserver, 2);
        setNumber(averageObserver, 3);
        setNumber(averageObserver, 4);

        averageObserver.onCompleted();

        try {
            latch.await(5L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void setNumber(StreamObserver<AverageRequest> averageObserver, int number) {
        averageObserver.onNext(AverageRequest.newBuilder()
                .setNumber(number)
                .build());
    }

    private void doErrorCall(ManagedChannel channel) {
        CalculateServiceGrpc.CalculateServiceBlockingStub calculateClient = CalculateServiceGrpc.newBlockingStub(channel);

        int number = -1;

        try {
            calculateClient.squareRoot(com.proto.calculate.SquareRootRequest.newBuilder()
                    .setNumber(number)
                    .build());

        } catch (StatusRuntimeException e){
            System.out.println("Got an exception for square root!");
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        CalculateClient calculateClient = new CalculateClient();

        calculateClient.run();


    }

}
