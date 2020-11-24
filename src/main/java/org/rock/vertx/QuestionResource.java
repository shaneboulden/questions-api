package org.rock.vertx;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/questions")
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
                .flatMap(r -> client.query("CREATE TABLE questions (id SERIAL PRIMARY KEY, question TEXT NOT NULL, answer TEXT)").execute())
                .flatMap(r -> client.query("INSERT INTO questions (question,answer) VALUES ('What is the meaning of life','42')").execute())
                .flatMap(r -> client.query("INSERT INTO questions (question,answer) VALUES ('What is my password','hunter2')").execute())
                .flatMap(r -> client.query("INSERT INTO questions (question,answer) VALUES ('Why is beer better than water','I mean...')").execute())
                .await().indefinitely();
    }

    @GET
    public Multi<Question> get() {
        return Question.findAll(client);
    } 

}
