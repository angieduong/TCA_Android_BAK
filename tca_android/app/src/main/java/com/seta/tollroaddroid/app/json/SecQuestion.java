package com.seta.tollroaddroid.app.json;

/**
 * Created by thomashuang on 16-03-28.
 */
public class SecQuestion implements java.io.Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String question_id;
    private String question_text;

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getQuestion_text() {
        return question_text;
    }

    public void setQuestion_text(String question_text) {
        this.question_text = question_text;
    }
}
