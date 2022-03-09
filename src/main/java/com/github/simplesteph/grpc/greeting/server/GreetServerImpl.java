package com.github.simplesteph.grpc.greeting.server;

import com.proto.greet.GreetEveryoneRequest;
import com.proto.greet.GreetEveryoneResponse;
import com.proto.greet.GreetWithDeadlineRequest;
import com.proto.greet.LongGreetRequest;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class GreetServerImpl extends com.proto.greet.GreetServiceGrpc.GreetServiceImplBase {

    @Override
    public void greet(com.proto.greet.GreetRequest request, StreamObserver<com.proto.greet.GreetResponse> responseObserver) {
        // extract the fields we need
        com.proto.greet.Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();

        // create the response
        String result = "Hello " + firstName;
        com.proto.greet.GreetResponse response = com.proto.greet.GreetResponse.newBuilder()
                .setResult(result)
                .build();

        // send the response
        responseObserver.onNext(response);

        // complete the RPC call
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(com.proto.greet.GreetManyTimesRequest request, StreamObserver<com.proto.greet.GreetManyTimesResponse> responseObserver) {
        String firstName = request.getGreeting().getFirstName();

        try {
            for (int i = 0; i < 10; i++) {
                String result = "Hello " + firstName + ", response number: " + i;
                com.proto.greet.GreetManyTimesResponse response = com.proto.greet.GreetManyTimesResponse.newBuilder()
                        .setResult(result)
                        .build();

                responseObserver.onNext(response);

                TimeUnit.SECONDS.sleep(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<com.proto.greet.LongGreetRequest> longGreet(StreamObserver<com.proto.greet.LongGreetResponse> responseObserver) {
        System.out.println("server received longGreet");
        StreamObserver<com.proto.greet.LongGreetRequest> streamObserverRequest = new StreamObserver<>() {

            String result = "";

            @Override
            public void onNext(LongGreetRequest value) {
                // client sends a message
                result += ". Hello " + value.getGreeting().getFirstName() + "! \n";
            }

            @Override
            public void onError(Throwable t) {
                // client sends an error
            }

            @Override
            public void onCompleted() {
                // client is done

                // this is when we want to return a response (responseObserver)
                responseObserver.onNext(com.proto.greet.LongGreetResponse.newBuilder()
                        .setResult(result).build()
                );
                responseObserver.onCompleted();
            }
        };

        return streamObserverRequest;
    }

    @Override
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(GreetEveryoneRequest value) {
                String response = "Hello " + value.getGreeting().getFirstName();
                GreetEveryoneResponse greetEveryoneResponse = GreetEveryoneResponse.newBuilder()
                        .setResult(response)
                        .build();
                responseObserver.onNext(greetEveryoneResponse);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void greetWithDeadline(GreetWithDeadlineRequest request, StreamObserver<com.proto.greet.GreetWithDeadlineResponse> responseObserver) {
        Context current = Context.current();

        try {
            for (int i = 0; i < 3; i++) {
                if (!current.isCancelled()){
                    System.out.println("sleep for 100 ms");
                    TimeUnit.MILLISECONDS.sleep(100);
                } else {
                    return ;
                }
            }

            System.out.println("send response");
            responseObserver.onNext(
                    com.proto.greet.GreetWithDeadlineResponse.newBuilder()
                            .setResult("Hello " + request.getGreeting().getFirstName())
                            .build()
            );

            responseObserver.onCompleted();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
