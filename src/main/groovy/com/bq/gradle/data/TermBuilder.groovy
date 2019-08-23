package com.bq.gradle.data


class Term extends TermSingle {
    Translation translation
}

class TermSingle {
    String term
}

class Translation {
    String content
}

class TermBuilder {
    private String _term
    private String _content

    def term(term) {
        _term = term
        this
    }

    def content(content) {
        _content = content
        this
    }

    def build() {
        new Term(
                term: _term,
                translation: new Translation(
                        content: _content
                )
        )
    }
}