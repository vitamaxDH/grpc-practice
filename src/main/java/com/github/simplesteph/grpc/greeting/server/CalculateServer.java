package com.github.simplesteph.grpc.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

public class CalculateServer {

    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(9999)
                .addService(new CalculateServerImpl())
                .addService(ProtoReflectionService.newInstance()) // reflection
                .build();

        server.start();
        System.out.println("Server started");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received Shutdown Request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));

        server.awaitTermination();
    }
}
