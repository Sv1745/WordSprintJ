package com.wordsprint.model;

public class Word {

    private Long wordId;
    private String word;

    public Word() {
    }

    public Word(Long wordId, String word) {
        this.wordId = wordId;
        this.word = word;
    }

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}