package ru.bcs.at.library.core.core.javabank.http.responses;

public class Inject extends Response {
    public static final String INJECT = "inject";

    public Inject() {
        this.put(INJECT, "");
    }

    public Inject withFunction(String function) {
        this.put(INJECT, function);
        return this;
    }
}