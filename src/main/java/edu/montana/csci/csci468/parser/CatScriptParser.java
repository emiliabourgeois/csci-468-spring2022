package edu.montana.csci.csci468.parser;

import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.*;
import edu.montana.csci.csci468.tokenizer.CatScriptTokenizer;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenList;
import edu.montana.csci.csci468.tokenizer.TokenType;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;
import edu.montana.csci.csci468.parser.ErrorType.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CatScriptParser {

    private TokenList tokens;
    private FunctionDefinitionStatement currentFunctionDefinition;

    public CatScriptProgram parse(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();

        // first parse an expression
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = null;
        try {
            expression = parseExpression();
        } catch(RuntimeException re) {
            // ignore :)
        }
        if (expression == null || tokens.hasMoreTokens()) {
            tokens.reset();
            while (tokens.hasMoreTokens()) {
                program.addStatement(parseProgramStatement());
            }
        } else {
            program.setExpression(expression);
        }

        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    public CatScriptProgram parseAsExpression(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = parseExpression();
        program.setExpression(expression);
        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    //============================================================
    //  Statements
    //============================================================

    private Statement parseProgramStatement() {
        Statement printStmt = parsePrintStatement();
        if (printStmt != null) {
            return printStmt;
        }
        return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private Statement parsePrintStatement() {
        if (tokens.match(PRINT)) {

            PrintStatement printStatement = new PrintStatement();
            printStatement.setStart(tokens.consumeToken());

            require(LEFT_PAREN, printStatement);
            printStatement.setExpression(parseExpression());
            printStatement.setEnd(require(RIGHT_PAREN, printStatement));

            return printStatement;
        } else {
            return null;
        }
    }

    //============================================================
    //  Expressions
    //============================================================

    private Expression parseExpression() {
        return parseEqualityExpression();
    }
    private Expression parseEqualityExpression() {
        Expression expression = parseComparisonExpression();
        while(tokens.match(BANG_EQUAL,EQUAL_EQUAL)){
            Token operator = tokens.consumeToken();
            final Expression RHS = parseComparisonExpression();
            EqualityExpression equalityExpression = new EqualityExpression(operator,expression,RHS);
            equalityExpression.setStart(expression.getStart());
            equalityExpression.setEnd(RHS.getEnd());
            expression = equalityExpression;
        }
        return expression;
    }

    private Expression parseComparisonExpression() {
        Expression expression = parseAdditiveExpression();
        while(tokens.match(GREATER,GREATER_EQUAL,LESS,LESS_EQUAL)){
            Token operator = tokens.consumeToken();
            final Expression RHS = parseAdditiveExpression();
            ComparisonExpression comparisonExpression = new ComparisonExpression(operator,expression,RHS);
            comparisonExpression.setStart(expression.getStart());
            comparisonExpression.setEnd(RHS.getEnd());
            expression = comparisonExpression;
        }
        return expression;
    }

    private Expression parseAdditiveExpression() {
        Expression expression = parseFactorExpression();
        while (tokens.match(PLUS, MINUS)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseFactorExpression();
            AdditiveExpression additiveExpression = new AdditiveExpression(operator, expression, rightHandSide);
            additiveExpression.setStart(expression.getStart());
            additiveExpression.setEnd(rightHandSide.getEnd());
            expression = additiveExpression;
        }
        return expression;
    }

    private Expression parseFactorExpression() {
        Expression expression = parseUnaryExpression();
        while (tokens.match(SLASH, STAR)) {
            Token operator = tokens.consumeToken();
            final Expression RHS = parseUnaryExpression();
            FactorExpression fe = new FactorExpression(operator,expression,RHS);
            fe.setStart((expression.getStart()));
            fe.setEnd(RHS.getEnd());
            expression = fe;
        }
        return expression;
    }

    private Expression parseUnaryExpression() {
        if (tokens.match(MINUS, NOT)) {
            Token token = tokens.consumeToken();
            Expression rhs = parseUnaryExpression();
            UnaryExpression unaryExpression = new UnaryExpression(token, rhs);
            unaryExpression.setStart(token);
            unaryExpression.setEnd(rhs.getEnd());
            return unaryExpression;
        } else {
            return parsePrimaryExpression();
        }
    }

    private Expression parsePrimaryExpression() {
        if (tokens.match(IDENTIFIER)) {
            Token identifierToken = tokens.consumeToken();
            IdentifierExpression exp = new IdentifierExpression(identifierToken.getStringValue());
            exp.setToken(identifierToken);
            if(tokens.match(LEFT_PAREN)){
                List<Expression> args = new ArrayList<Expression>();
                tokens.consumeToken();
                if(tokens.match(RIGHT_PAREN)){
                    tokens.consumeToken();
                    FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifierToken.getStringValue(),args);
                    return functionCallExpression;
                }
                args.add(parseExpression());
                while(tokens.match(COMMA)){
                    tokens.consumeToken();
                    args.add(parseExpression());
                }
                FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifierToken.getStringValue(),args);
                if(tokens.match(RIGHT_PAREN)){
                    tokens.consumeToken();
                }
                else {
                    functionCallExpression.addError(ErrorType.UNTERMINATED_ARG_LIST);
                }
                return functionCallExpression;

            }
            return exp;
        }
        else if (tokens.match(STRING)) {
            Token stringToken = tokens.consumeToken();
            StringLiteralExpression stringExpression = new StringLiteralExpression(stringToken.getStringValue());
            stringExpression.setToken(stringToken);
            return stringExpression;
        } else if (tokens.match(INTEGER)) {
            Token integerToken = tokens.consumeToken();
            IntegerLiteralExpression integerExpression = new IntegerLiteralExpression(integerToken.getStringValue());
            integerExpression.setToken(integerToken);
            return integerExpression;
        } else if (tokens.match(TRUE, FALSE)) {
            Token boolToken = tokens.consumeToken();
            BooleanLiteralExpression boolExpression = new BooleanLiteralExpression(Boolean.valueOf(boolToken.getStringValue()));
            boolExpression.setToken(boolToken);
            return boolExpression;
        } else if(tokens.match(NULL)) {
            Token nullToken = tokens.consumeToken();
            NullLiteralExpression nullExpression = new NullLiteralExpression();
            nullExpression.setToken(nullToken);
            return nullExpression;
        } else if(tokens.match(LEFT_PAREN)) {
            tokens.consumeToken();
            ParenthesizedExpression expression = new ParenthesizedExpression(parseExpression());
            if(tokens.match(RIGHT_PAREN)) {
                tokens.consumeToken();
                return expression;
            }
            else {
                SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression(tokens.consumeToken());
                return syntaxErrorExpression;
            }

        } else if(tokens.match(LEFT_BRACKET)){
            List<Expression> values = new ArrayList<Expression>();
            tokens.consumeToken();
            if(tokens.match(RIGHT_BRACKET)){
                tokens.consumeToken();
                ListLiteralExpression listLiteralExpression = new ListLiteralExpression(values);
                return listLiteralExpression;
            }
            values.add(parseExpression());
            while(tokens.match(COMMA)){
                tokens.consumeToken();
                values.add(parseExpression());
            }
            ListLiteralExpression listLiteralExpression = new ListLiteralExpression(values);
            if(tokens.match(RIGHT_BRACKET)){
                tokens.consumeToken();
            }
            else {
                listLiteralExpression.addError(ErrorType.UNTERMINATED_LIST);
            }
            return listLiteralExpression;
        } else {
            SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression(tokens.consumeToken());
            return syntaxErrorExpression;
        }
    }

    //============================================================
    //  Parse Helpers
    //============================================================
    private Token require(TokenType type, ParseElement elt) {
        return require(type, elt, ErrorType.UNEXPECTED_TOKEN);
    }

    private Token require(TokenType type, ParseElement elt, ErrorType msg) {
        if(tokens.match(type)){
            return tokens.consumeToken();
        } else {
            elt.addError(msg, tokens.getCurrentToken());
            return tokens.getCurrentToken();
        }
    }

}
