/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.nashorn.internal.parser;

import static jdk.nashorn.internal.parser.TokenType.EOF;
import static jdk.nashorn.internal.parser.TokenType.EOL;
import static jdk.nashorn.internal.parser.TokenType.IDENT;

import jdk.nashorn.internal.ir.IdentNode;
import jdk.nashorn.internal.ir.LiteralNode;
import jdk.nashorn.internal.parser.Lexer.LexerToken;
import jdk.nashorn.internal.parser.Lexer.RegexToken;
import jdk.nashorn.internal.runtime.ECMAErrors;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.JSErrorType;
import jdk.nashorn.internal.runtime.ParserException;
import jdk.nashorn.internal.runtime.Source;

/**
 * Base class for parsers.
 */
public abstract class AbstractParser {
    /** Source to parse. */
    protected final Source source;

    /** Error manager to report errors. */
    protected final ErrorManager errors;

    /** Stream of lex tokens to parse. */
    protected TokenStream stream;

    /** Index of current token. */
    protected int k;

    /** Descriptor of current token. */
    protected long token;

    /** Type of current token. */
    protected TokenType type;

    /** Type of last token. */
    protected TokenType last;

    /** Start position of current token. */
    protected int start;

    /** Finish position of previous token. */
    protected int finish;

    /** Current line number. */
    protected int line;

    /** Position of last EOL + 1. */
    protected int linePosition;

    /** Lexer used to scan source content. */
    protected Lexer lexer;

    /** Is this parser running under strict mode? */
    protected boolean isStrictMode;

    /**
     * Construct a parser.
     *
     * @param source  Source to parse.
     * @param errors  Error reporting manager.
     * @param strict  True if we are in strict mode
     */
    protected AbstractParser(final Source source, final ErrorManager errors, final boolean strict) {
        this.source       = source;
        this.errors       = errors;
        this.k            = -1;
        this.token        = Token.toDesc(EOL, 0, 1);
        this.type         = EOL;
        this.last         = EOL;
        this.start        = 0;
        this.finish       = 0;
        this.line         = 0;
        this.linePosition = 0;
        this.lexer        = null;
        this.isStrictMode = strict;
    }

    /**
     * Get the Source
     *
     * @return the Source
     */
    public Source getSource() {
        return source;
    }

    /**
     * Get the ith token.
     *
     * @param i Index of token.
     *
     * @return  the token
     */
    protected final long getToken(final int i) {
        // Make sure there are enough tokens available.
        while (i > stream.last()) {
            // If we need to buffer more for lookahead.
            if (stream.isFull()) {
                stream.grow();
            }

            // Get more tokens.
            lexer.lexify();
        }

        return stream.get(i);
    }

    /**
     * Return the tokenType of the ith token.
     *
     * @param i Index of token
     *
     * @return the token type
     */
    protected final TokenType T(final int i) {
        // Get token descriptor and extract tokenType.
        return Token.descType(getToken(i));
    }

    /**
     * Seek next token that is not an EOL.
     *
     * @return tokenType of next token.
     */
    protected final TokenType next() {
        do {
            nextOrEOL();
        } while (type == EOL);

        return type;
    }

    /**
     * Seek next token.
     *
     * @return tokenType of next token.
     */
    protected final TokenType nextOrEOL() {
        // Capture last token tokenType.
        last = type;
        if (type != EOF) {

            // Set up next token.
            k++;
            final long lastToken = token;
            token = getToken(k);
            type = Token.descType(token);

            // do this before the start is changed below
            if (last != EOL) {
                finish = start + Token.descLength(lastToken);
            }

            if (type == EOL) {
                line = Token.descLength(token);
                linePosition = Token.descPosition(token);
            } else {
                start = Token.descPosition(token);
            }

        }

        return type;
    }

    /**
     * Get the message string for a message ID and arguments
     *
     * @param msgId The Message ID
     * @param args  The arguments
     *
     * @return The message string
     */
    protected static String message(final String msgId, final String... args) {
        return ECMAErrors.getMessage("parser.error." + msgId, args);
    }

    /**
     * Report an error.
     *
     * @param message    Error message.
     * @param errorToken Offending token.
     */
    protected final void error(final String message, final long errorToken) {
        error(JSErrorType.SYNTAX_ERROR, message, errorToken);
    }

    /**
     * Report an error.
     *
     * @param errorType  The error type
     * @param message    Error message.
     * @param errorToken Offending token.
     */
    protected final void error(final JSErrorType errorType, final String message, final long errorToken) {
        final int position  = Token.descPosition(errorToken);
        final int lineNum   = source.getLine(position);
        final int columnNum = source.getColumn(position);
        final String formatted = ErrorManager.format(message, source, lineNum, columnNum, errorToken);
        final ParserException exp = new ParserException(formatted, source, lineNum, columnNum, errorToken);
        exp.setErrorType(errorType);
        throw exp;
    }

    /**
     * Report an error.
     *
     * @param message Error message.
     */
    protected final void error(final String message) {
        error(JSErrorType.SYNTAX_ERROR, message);
    }

    /**
     * Report an error.
     *
     * @param errorType  The error type
     * @param message    Error message.
     */
    protected final void error(final JSErrorType errorType, final String message) {
        // TODO - column needs to account for tabs.
        final int position = Token.descPosition(token);
        final int column = position - linePosition;
        final String formatted = ErrorManager.format(message, source, line, column, token);
        final ParserException exp = new ParserException(formatted, source, line, column, token);
        exp.setErrorType(errorType);
        throw exp;
    }

    /**
     * Generate 'expected' message.
     *
     * @param expected Expected tokenType.
     *
     * @return the message string
     */
    protected final String expectMessage(final TokenType expected) {
        final String tokenString = Token.toString(source, token, false);
        String msg;

        if (expected == null) {
            msg = AbstractParser.message("expected.stmt", tokenString);
        } else {
            final String expectedName = expected.getNameOrType();
            msg = AbstractParser.message("expected", expectedName, tokenString);
        }

        return msg;
    }

    /**
     * Check next token and advance.
     *
     * @param expected Expected tokenType.
     *
     * @throws ParserException on unexpected token type
     */
    protected final void expect(final TokenType expected) throws ParserException {
        if (type != expected) {
            error(expectMessage(expected));
        }

        next();
    }

    /**
     * Check next token, get its value and advance.
     *
     * @param  expected Expected tokenType.
     * @return The JavaScript value of the token
     * @throws ParserException on unexpected token type
     */
    protected final Object expectValue(final TokenType expected) throws ParserException {
        if (type != expected) {
            error(expectMessage(expected));
        }

        final Object value = getValue();

        next();

        return value;
    }

    /**
     * Get the value of the current token.
     *
     * @return JavaScript value of the token.
     */
    protected final Object getValue() {
        return getValue(token);
    }

    /**
     * Get the value of a specific token
     *
     * @param valueToken the token
     *
     * @return JavaScript value of the token
     */
    protected final Object getValue(final long valueToken) {
        try {
            return lexer.getValueOf(valueToken, isStrictMode);
        } catch (final ParserException e) {
            errors.error(e);
        }

        return null;
    }

    /**
     * Certain future reserved words can be used as identifiers in
     * non-strict mode. Check if the current token is one such.
     *
     * @return true if non strict mode identifier
     */
    protected final boolean isNonStrictModeIdent() {
        return !isStrictMode && type.getKind() == TokenKind.FUTURESTRICT;
    }

    /**
     * Get ident.
     *
     * @return Ident node.
     */
    protected final IdentNode getIdent() {
        // Capture IDENT token.
        long identToken = token;

        if (isNonStrictModeIdent()) {
            // Fake out identifier.
            identToken = Token.recast(token, IDENT);
            // Get IDENT.
            final String ident = (String)getValue(identToken);

            next();

            // Create IDENT node.
            return new IdentNode(source, identToken, finish, ident);
        }

        // Get IDENT.
        final String ident = (String)expectValue(IDENT);
        if (ident == null) {
            return null;
        }
        // Create IDENT node.
        return new IdentNode(source, identToken, finish, ident);
    }

    /**
     * Check if current token is in identifier name
     *
     * @return true if current token is an identifier name
     */
    protected final boolean isIdentifierName() {
        final TokenKind kind = type.getKind();
        if (kind == TokenKind.KEYWORD || kind == TokenKind.FUTURE || kind == TokenKind.FUTURESTRICT) {
            return true;
        }
        // Fake out identifier.
        final long identToken = Token.recast(token, IDENT);
        // Get IDENT.
        final String ident = (String)getValue(identToken);
        return !ident.isEmpty() && Character.isJavaIdentifierStart(ident.charAt(0));
    }

    /**
     * Create an IdentNode from the current token
     *
     * @return an IdentNode representing the current token
     */
    protected final IdentNode getIdentifierName() {
        if (type == IDENT) {
            return getIdent();
        } else if (isIdentifierName()) {
            // Fake out identifier.
            final long identToken = Token.recast(token, IDENT);
            // Get IDENT.
            final String ident = (String)getValue(identToken);
            next();
            // Create IDENT node.
            return new IdentNode(source, identToken, finish, ident);
        } else {
            expect(IDENT);
            return null;
        }
    }

    /**
     * Create a LiteralNode from the current token
     *
     * @return LiteralNode representing the current token
     * @throws ParserException if any literals fails to parse
     */
    protected final LiteralNode<?> getLiteral() throws ParserException {
        // Capture LITERAL token.
        final long literalToken = token;

        // Create literal node.
        final Object value = getValue();

        LiteralNode<?> node = null;

        if (value == null) {
            node = LiteralNode.newInstance(source, literalToken, finish);
        } else if (value instanceof Number) {
            node = LiteralNode.newInstance(source, literalToken, finish, (Number)value);
        } else if (value instanceof String) {
            node = LiteralNode.newInstance(source, literalToken, finish, (String)value);
        } else if (value instanceof LexerToken) {
            if (value instanceof RegexToken) {
                final RegexToken regex = (RegexToken)value;
                try {
                    RegExp.validate(regex.getExpression(), regex.getOptions());
                } catch (final ParserException e) {
                    error(e.getMessage());
                }
            }
            node = LiteralNode.newInstance(source, literalToken, finish, (LexerToken)value);
        } else {
            assert false : "unknown type for LiteralNode: " + value.getClass();
        }

        next();
        return node;
    }
}
