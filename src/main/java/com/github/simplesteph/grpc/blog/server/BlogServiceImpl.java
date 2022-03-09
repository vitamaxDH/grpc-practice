package com.github.simplesteph.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.proto.blog.Blog;
import com.proto.blog.BlogServiceGrpc;
import com.proto.blog.CreateBlogRequest;
import com.proto.blog.CreateBlogResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase database = mongoClient.getDatabase("mydb");
    private MongoCollection<Document> collection = database.getCollection("blog");

    @Override
    public void readBlog(com.proto.blog.ReadBlogRequest request, StreamObserver<com.proto.blog.ReadBlogResponse> responseObserver) {
        String blogId = request.getBlogId();

        System.out.println("Searching for a blog");

        Document result = null;

        try {
            result = collection.find(eq("_id", new ObjectId(blogId)))
                    .first();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .asRuntimeException()
            );
        }

        if (result == null) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .asRuntimeException()
            );
        } else {
            System.out.println("Blog found, sending response");
            Blog blog = Blog.newBuilder()
                    .setId(blogId)
                    .setAuthorId(result.getString("author_id"))
                    .setTitle(result.getString("title"))
                    .setContent(result.getString("content"))
                    .build();

            responseObserver.onNext(com.proto.blog.ReadBlogResponse.newBuilder()
                    .setBlog(blog)
                    .build());

            responseObserver.onCompleted();
        }
    }

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {

        System.out.println("Received Create Blog request");

        Blog blog = request.getBlog();

        Document doc = new Document("author_id", blog.getAuthorId())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());

        System.out.println("Inserting Blog...");
        // we insert (create) the document in mongoDB
        collection.insertOne(doc);

        // we retrieve the MongoDB generated ID
        String id = doc.getObjectId("_id").toString();
        System.out.println("Inserted Blog: " + id);

        CreateBlogResponse response = CreateBlogResponse.newBuilder()
                .setBlog(blog.toBuilder().setId(id))
                .build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }
}
