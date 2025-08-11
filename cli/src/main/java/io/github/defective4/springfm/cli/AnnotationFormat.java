package io.github.defective4.springfm.cli;

import java.util.function.Consumer;

import io.github.defective4.springfm.server.data.AudioAnnotation;

public enum AnnotationFormat {
    JSON(s -> System.out.println(new AnnotationContainer(s).toJSON())),
    TEXT(s -> System.out.println((s.getTitle() == null ? null : s.getTitle().trim()) + " | " + s.getDescription()
            + " | Music: " + !s.isNonMusic()));

    private final Consumer<AudioAnnotation> printer;

    private AnnotationFormat(Consumer<AudioAnnotation> printer) {
        this.printer = printer;
    }

    public Consumer<AudioAnnotation> getPrinter() {
        return printer;
    }

}
