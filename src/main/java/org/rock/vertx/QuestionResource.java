package org.rock.vertx;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/api/questions/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QuestionResource {

    @Inject
    io.vertx.mutiny.pgclient.PgPool client;

    @Inject
    @ConfigProperty(name = "anvil.schema.create", defaultValue = "true")
    boolean schemaCreate;

    @PostConstruct
    void config() {
      if (schemaCreate) {
          initdb();
      }
    }

    @PostConstruct
    void initdb() {
        client.query("DROP TABLE IF EXISTS questions").execute()
                .flatMap(r -> client.query("CREATE OR REPLACE FUNCTION trigger_set_timestamp() RETURNS TRIGGER AS $$ BEGIN NEW.updated_at = NOW(); RETURN NEW; END; $$ LANGUAGE plpgsql").execute())
                .flatMap(r -> client.query("CREATE TABLE questions (id SERIAL PRIMARY KEY, question TEXT NOT NULL, answer TEXT, created_at TIMESTAMPTZ NOT NULL DEFAULT NOW())").execute())
                .flatMap(r -> client.query("CREATE TRIGGER set_timestamp BEFORE UPDATE ON questions FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp()").execute())
                .flatMap(r -> client.query("INSERT INTO questions (question,answer) VALUES ('What is the meaning of life','42')").execute())
                .flatMap(r -> client.query("INSERT INTO questions (question,answer) VALUES ('What is my password','hunter2')").execute())
                .flatMap(r -> client.query("INSERT INTO questions (question,answer) VALUES ('Why is beer better than water','I mean...')").execute())
                .await().indefinitely();
    }

    @GET
    public Multi<Question> get() {
        return Question.findAll(client);
    } 

    @GET
    @Path("{id}")
    public Uni<Response> getSingle(@PathParam("id") Long id) {
    return Question.findById(client, id)
            .onItem().transform(question -> question != null ? Response.ok(question) : Response.status(Status.NOT_FOUND))
            .onItem().transform(ResponseBuilder::build);
    }

    @POST
    public Uni<Response> create(Question question) {
    return question.save(client)
            .onItem().transform(id -> URI.create("/questions/" + id))
            .onItem().transform(uri -> Response.created(uri).build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
    return Question.delete(client, id)
            .onItem().transform(deleted -> deleted ? Status.NO_CONTENT : Status.NOT_FOUND)
            .onItem().transform(status -> Response.status(status).build());
    } 
}
