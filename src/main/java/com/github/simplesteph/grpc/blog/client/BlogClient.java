package com.github.simplesteph.grpc.blog.client;

import com.proto.blog.Blog;
import com.proto.blog.BlogServiceGrpc;
import com.proto.blog.CreateBlogRequest;
import com.proto.blog.CreateBlogResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

public class BlogClient {

    public static void main(String[] args) throws Exception {
        BlogClient calculateClient = new BlogClient();

        calculateClient.run();
    }

    void run() throws Exception {
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

        com.proto.blog.ReadBlogResponse readBlogResponse = blogClient.readBlog(com.proto.blog.ReadBlogRequest.newBuilder()
                .setBlogId(blogId)
                .build());

        System.out.println(readBlogResponse.getBlog());

        System.out.println("Reading blog with non existing id....");

        // trigger not found
//        ReadBlogResponse readBlogResponseNotFound = blogClient.readBlog(com.proto.blog.ReadBlogRequest.newBuilder()
//                .setBlogId("6228aa4ba09ab969873eeab1")
//                .build());

        Blog newBlog = Blog.newBuilder()
                .setId(blogId)
                .setAuthorId("Changed Author")
                .setTitle("New blog (updated!")
                .setContent("Hello world this is my first blog! I've added some more content")
                .build();

        System.out.println("Updating blog");
        com.proto.blog.UpdateBlogResponse updateBlogResponse = blogClient.updateBlog(com.proto.blog.UpdateBlogRequest.newBuilder()
                .setBlog(newBlog)
                .build());

        System.out.println("Updated blog");
        System.out.println(updateBlogResponse.getBlog());

        System.out.println("Deleting blog");
        com.proto.blog.DeleteBlogResponse deleteBlogResponse = blogClient.deleteBlog(com.proto.blog.DeleteBlogRequest.newBuilder()
                .setBlogId(blogId)
                .build());

        System.out.println("Deleted blog");
        System.out.println(deleteBlogResponse.getBlogId());


        System.out.println("Reading blog");
        // this one should return NOT_FOUND
        com.proto.blog.ReadBlogResponse readBlogResponseAfterDeletion = blogClient.readBlog(com.proto.blog.ReadBlogRequest.newBuilder()
                .setBlogId(blogId)
                .build());

    }

}

