package com.github.simplesteph.grpc.blog.client;

import com.proto.blog.Blog;
import com.proto.blog.BlogServiceGrpc;
import com.proto.blog.CreateBlogRequest;
import com.proto.blog.CreateBlogResponse;
import com.proto.blog.ReadBlogRequest;
import com.proto.blog.ReadBlogResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {

    public static void main(String[] args) {
        BlogClient calculateClient = new BlogClient();

        calculateClient.run();
    }

    void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        Blog blog = Blog.newBuilder()
                .setAuthorId("Max")
                .setTitle("New blog!")
                .setContent("Hello world this is my first blog!")
                .build();


        CreateBlogResponse createBlogResponse = blogClient.createBlog(CreateBlogRequest.newBuilder()
                .setBlog(blog)
                .build());

        System.out.println("Received create blog response");
        System.out.println(createBlogResponse.toString());

        String blogId = createBlogResponse.getBlog().getId();

        System.out.println("Reading blog....");

        ReadBlogResponse readBlogResponse = blogClient.readBlog(com.proto.blog.ReadBlogRequest.newBuilder()
                .setBlogId(blogId)
                .build());

        System.out.println(readBlogResponse.getBlog());

        System.out.println("Reading blog with non existing id....");

        // trigger not found
//        ReadBlogResponse readBlogResponseNotFound = blogClient.readBlog(com.proto.blog.ReadBlogRequest.newBuilder()
//                .setBlogId("6228aa4ba09ab969873eeab1")
//                .build());



    }

}
