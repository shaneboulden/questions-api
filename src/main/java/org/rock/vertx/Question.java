package org.rock.vertx;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;

public class Question {

    public Long id;

    public String question;
    public String answer;

    public Question() {
    }

    public Question(String question) {
        this.question = question;
    }

    public Question(Long id, String Question) {
      this.id = id;
      this.question = question;
    }

    public Question(Long id, String question, String answer) {
        this.id = id;
        this.question = question;
        this.answer = answer;
    }

    private static Question from(Row row) {
      return new Question(row.getLong("id"), row.getString("question"), row.getString("answer"));
    }

    public static Uni<Question> findById(PgPool client, Long id) {
    return client.preparedQuery("SELECT id, question, answer FROM questions WHERE id = $1").execute(Tuple.of(id))
            .onItem().transform(RowSet::iterator)
            .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public static Multi<Question> findAll(PgPool client) {
    return client.query("SELECT id, question,answer FROM questions ORDER BY created_at DESC").execute()
            .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
            .onItem().transform(Question::from);
    }

    public Uni<Long> save(PgPool client) {
    return client.preparedQuery("INSERT INTO questions (question) VALUES ($1) RETURNING id").execute(Tuple.of(question))
            .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }

    public static Uni<Boolean> delete(PgPool client, Long id) {
    return client.preparedQuery("DELETE FROM questions WHERE id = $1").execute(Tuple.of(id))
            .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }
}
