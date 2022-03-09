package com.github.simplesteph.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends com.proto.blog.BlogServiceGrpc.BlogServiceImplBase {

    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase database = mongoClient.getDatabase("mydb");
    private MongoCollection<Document> collection = database.getCollection("blog");

    @Override
    public void createBlog(com.proto.blog.CreateBlogRequest request, StreamObserver<com.proto.blog.CreateBlogResponse> responseObserver) {

        System.out.println("Received Create Blog request");

        com.proto.blog.Blog blog = request.getBlog();

        Document doc = new Document("author_id", blog.getAuthorId())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());

        System.out.println("Inserting Blog...");
        // we insert (create) the document in mongoDB
        collection.insertOne(doc);

        // we retrieve the MongoDB generated ID
        String id = doc.getObjectId("_id").toString();
        System.out.println("Inserted Blog: " + id);

        com.proto.blog.CreateBlogResponse response = com.proto.blog.CreateBlogResponse.newBuilder()
                .setBlog(blog.toBuilder().setId(id))
                .build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

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
            com.proto.blog.Blog blog = documentToBlog(result);

            responseObserver.onNext(com.proto.blog.ReadBlogResponse.newBuilder()
                    .setBlog(blog)
                    .build());

            responseObserver.onCompleted();
        }
    }

    private com.proto.blog.Blog documentToBlog(Document document) {
        return com.proto.blog.Blog.newBuilder()
                .setId(document.getObjectId("_id").toString())
                .setAuthorId(document.getString("author_id"))
                .setTitle(document.getString("title"))
                .setContent(document.getString("content"))
                .build();
    }

    @Override
    public void updateBlog(com.proto.blog.UpdateBlogRequest request, StreamObserver<com.proto.blog.UpdateBlogResponse> responseObserver) {
        System.out.println("Received Update Blog request");

        com.proto.blog.Blog blog = request.getBlog();
        String blogId = blog.getId();

        System.out.println("Searching for a blog so we can update it");

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
            Document replacement = new Document("author_id", blog.getAuthorId())
                    .append("title", blog.getTitle())
                    .append("content", blog.getContent())
                    .append("_id", new ObjectId(blogId));

            System.out.println("Replacing blog in database...");

            UpdateResult updateResult = collection.replaceOne(eq("_id", result.getObjectId("_id")), replacement);

            System.out.println("Replaced! Sending as a response");

            responseObserver.onNext(
                    com.proto.blog.UpdateBlogResponse.newBuilder()
                            .setBlog(documentToBlog(replacement))
                            .build()
            );

            responseObserver.onCompleted();
        }
    }


    @Override
    public void deleteBlog(com.proto.blog.DeleteBlogRequest request, StreamObserver<com.proto.blog.DeleteBlogResponse> responseObserver) {
        System.out.println("Received Delete Blog Request");
        String blogId = request.getBlogId();

        DeleteResult result = null;
        try {
            result = collection.deleteOne(eq("_id", new ObjectId(blogId)));
        } catch (Exception e) {
            System.out.println("Blog not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }
        if (result.getDeletedCount() == 0) {
            System.out.println("Blog not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .asRuntimeException()
            );
        } else {
            System.out.println("Blog was deleted");
            responseObserver.onNext(com.proto.blog.DeleteBlogResponse.newBuilder()
                    .setBlogId(blogId)
                    .build());

            responseObserver.onCompleted();
        }
    }
}
