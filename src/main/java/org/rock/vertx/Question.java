package org.rock.vertx;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.pgclient.PgPool;

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

    public static Multi<Question> findAll(PgPool client) {
    return client.query("SELECT id, question,answer FROM questions ORDER BY question ASC").execute()
            .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
            .onItem().transform(Question::from);
    }

}
