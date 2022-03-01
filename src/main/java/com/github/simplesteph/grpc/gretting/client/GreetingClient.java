package com.github.simplesteph.grpc.gretting.client;

import com.proto.greet.GreetServiceGrpc;
import com.proto.greet.Greeting;
import com.proto.greet.LongGreetRequest;
import com.proto.greet.LongGreetResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    private ManagedChannel channel;

    public void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
//        doUnaryCall(channel);
//        doServerStreawmingCall(channel);
//        doClientStreamingCall(channel);

        doBiDiStreamingCall(channel);

        // do something
        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        // created a protocol buffer greeting message
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Daehan")
                .setLastName("Choi")
                .build();

        // do the same for a GreetRequest
        com.proto.greet.GreetRequest greetRequest = com.proto.greet.GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        // call the RPC and get back a GreetResponse (protocol buffers)
        com.proto.greet.GreetResponse greetResponse = greetClient.greet(greetRequest);

        System.out.println(greetResponse.getResult());
    }

    private void doServerStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        com.proto.greet.GreetManyTimesRequest greetManyTimes = com.proto.greet.GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Daehan").build())
                .build();

        // we stream the responses (in a blocking manner)
        greetClient.greetManyTimes(greetManyTimes)
                .forEachRemaining(greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                });


    }

    private void doClientStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestObserver = asyncClient.longGreet(new StreamObserver<>() {
            @Override
            public void onNext(LongGreetResponse value) {
                // we get a response from the server
                System.out.println("Received a response from the server");
                System.out.println(value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                // we get an error from the server
            }

            @Override
            public void onCompleted() {
                // the server is done sending us data
                System.out.println("Server has completed sending us something");
                latch.countDown();
                // onCompleted will be called right after onNext()
            }
        });

        requestObserverOnNext(requestObserver, "max");
        requestObserverOnNext(requestObserver, "Rebecca");
        requestObserverOnNext(requestObserver, "Lee");

        // we tell the server that the client is done sending data
        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void requestObserverOnNext(StreamObserver<com.proto.greet.LongGreetRequest> requestObserver, String firstName) {
        System.out.println("Sending message " + firstName);
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName(firstName))
                .build());
    }

    private void doBiDiStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub greetClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<com.proto.greet.GreetEveryoneRequest> requestObserver = greetClient.greetEveryone(new StreamObserver<>() {
            @Override
            public void onNext(com.proto.greet.GreetEveryoneResponse value) {
                System.out.println("Response from server: " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending data");
                latch.countDown();
            }
        });

        Arrays.asList("Max", "대한", "haha").forEach(name -> {
            requestObserver.onNext(com.proto.greet.GreetEveryoneRequest.newBuilder()
                    .setGreeting(Greeting.newBuilder()
                            .setFirstName(name)
                            .build())
                    .build());
        });

        requestObserver.onCompleted();

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC client");

        GreetingClient main = new GreetingClient();
        main.run();
    }

}
