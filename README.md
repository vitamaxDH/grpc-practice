# gRPC 코드 정리

유데미 gRPC 강의를 보며 정리한 작성한 코드입니다. 

[gRPC [Java] Master Class: Build Modern API & Micro services](https://www.udemy.com/course/grpc-java/)

## gRPC 사용 방법

### 1. gradle 설정
1.1) 플러그인

com.google.protobuf github 에서 최신 버전을 확인하여 플러그인을 적용

[Protobuf Github](https://github.com/google/protobuf-gradle-plugin)

```groovy
plugins {
    id 'com.google.protobuf' version '0.8.18'
}
```

1.2) gRPC 관련 의존성 추가

```groovy
dependencies {
    runtimeOnly 'io.grpc:grpc-netty-shaded:1.44.1'
    implementation 'io.grpc:grpc-protobuf:1.44.1'
    implementation 'io.grpc:grpc-stub:1.44.1'
}
```

1.3) protobuf 관련 설정 추가
```groovy
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.2"
    }
    plugins {
        grpc {
            artifact = 'io.grpc:protoc-gen-grpc-java:1.44.1'
        }
    }
    generateProtoTasks {
        all()*.plugins {
            grpc {}
        }
    }
}
```

1.4) 컴파일 파일 경로 추가
```groovy
sourceSets.main.java.srcDir new File(buildDir, 'generated/source')
```


### 2. proto 파일 생성 및 컴파일

1. 도메인.proto 파일 생성 후 `syntax`, `package`, `option`, `message`, `service` 등을 정의
2. 컴파일
   - 인텔리제이 우측 코끼리모양 (gradle) 클릭
   - Tasks
     - other
       - generateProto

### 3. 서버에서 처리할 ServiceImpl 작성

1. proto 파일에서 정의한 xxxServiceImplBase 상속
2. 메서드 오버라이드

ex)
```java
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
}
```

### 4. 서버 생성
- ServerBuilder 를 통해 서버 생성

ex)
```java
public class GreetingServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello gRPC");

        Server server = ServerBuilder.forPort(50051)
                .addService(new GreetServerImpl())
                .build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received Shutdown Request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));

        server.awaitTermination();
    }

}
```

### 5. 요청 생성

ex)
```java
public class GreetingClient {

    private ManagedChannel channel;

    public void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        
        doUnaryCall(channel);

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

    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC client");

        GreetingClient main = new GreetingClient();
        main.run();
    }
}
```

### 6. 서버 기동 및 클라이언트 요청 실행

>GreetingServer
```
Hello gRPC
```

>GreetingClient
```
Hello I'm a gRPC client
Hello Daehan
Shutting down channel

Process finished with exit code 0
```


