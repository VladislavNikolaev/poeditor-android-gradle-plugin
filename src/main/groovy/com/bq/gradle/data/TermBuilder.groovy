package com.bq.gradle.data


class Term extends TermSingle {
    String context
    Translation translation
}

class TermSingle {
    String term
}

class Translation {
    String content
    Integer fuzzy = 0
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
                context: _content,
                translation: new Translation(
                        content: _content,
                        fuzzy: 0
                )
        )
    }
}