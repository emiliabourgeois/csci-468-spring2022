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
        Statement statement = parseFunctionDefinition();
        if (statement != null) {
            return statement;
        }
        statement = parseStatement();
        if (statement != null){
            return statement;
        }
        return new SyntaxErrorStatement(tokens.consumeToken());
    }
    private Statement parseFunctionDefinition(){
        if(tokens.match(FUNCTION)){
            FunctionDefinitionStatement functionDefinitionStatement = new FunctionDefinitionStatement();
            functionDefinitionStatement.setStart(tokens.consumeToken());
            Token funcname = require(IDENTIFIER,functionDefinitionStatement);
            functionDefinitionStatement.setName(funcname.getStringValue());

            require(LEFT_PAREN,functionDefinitionStatement);
            if (!tokens.match(RIGHT_PAREN)) {
                do {
                    Token paramName = require(IDENTIFIER, functionDefinitionStatement);
                    TypeLiteral typeLiteral = null;
                    if (tokens.matchAndConsume(COLON)) {
                        typeLiteral = parseTypeLiteral();
                    }
                    functionDefinitionStatement.addParameter(paramName.getStringValue(), typeLiteral);
                } while (tokens.matchAndConsume(COMMA));
            }
            require(RIGHT_PAREN, functionDefinitionStatement);
            TypeLiteral typeLiteral = null;
            if (tokens.matchAndConsume(COLON)) {
                typeLiteral = parseTypeLiteral();
            }
            functionDefinitionStatement.setType(typeLiteral);

            currentFunctionDefinition = functionDefinitionStatement;

            require(LEFT_BRACE,functionDefinitionStatement);
            LinkedList<Statement> statements = new LinkedList<>();
            while(!tokens.match(RIGHT_BRACE) && tokens.hasMoreTokens()){
                statements.add(parseStatement());
            }
            require(RIGHT_BRACE,functionDefinitionStatement);
            functionDefinitionStatement.setBody(statements);
            return functionDefinitionStatement;
        }
        else return null;
    }

    private Statement parseStatement() {
        try {
            Statement stmt = parsePrintStatement();
            if (stmt != null) {
                return stmt;
            }
            stmt = parseForStatement();
            if (stmt != null) {
                return stmt;
            }
            stmt = parseIfStatement();
            if (stmt != null) {
                return stmt;
            }
            stmt = parseVarStatement();
            if (stmt != null) {
                return stmt;
            }
            stmt = parseAssignmentOrFunctionCallStatement();
            if (stmt != null) {
                return stmt;
            }
            if (currentFunctionDefinition != null) {
                stmt = parseReturnStatement();
                if (stmt != null) {
                    return stmt;
                }
            }
            return new SyntaxErrorStatement(tokens.consumeToken());
        } catch (UnknownExpressionParseException e) {
            SyntaxErrorStatement syntaxErrorStatement = new SyntaxErrorStatement(tokens.consumeToken());
            while(tokens.hasMoreTokens()){
                if(tokens.match(VAR,FOR,IF,ELSE,PRINT)){
                    break;
                }
                else {
                    tokens.consumeToken();
                }
            }
            return syntaxErrorStatement;
        }
    }
    private Statement parseReturnStatement(){
        if(tokens.matchAndConsume(RETURN)){
            ReturnStatement returnStatement = new ReturnStatement();
            returnStatement.setFunctionDefinition(currentFunctionDefinition);
            if(!tokens.match(RIGHT_BRACE)){
                Expression expression = parseExpression();
                returnStatement.setExpression(expression);
            }
            return returnStatement;
        }
        return null;
    }
    private Statement parseForStatement() {
        if(tokens.match(FOR)){
            ForStatement forStatement = new ForStatement();
            forStatement.setStart(tokens.consumeToken());
            require(LEFT_PAREN, forStatement);
            forStatement.setVariableName(tokens.consumeToken().getStringValue());
            require(IN,forStatement);
            forStatement.setExpression(parseExpression());
            require(RIGHT_PAREN,forStatement);
            require(LEFT_BRACE,forStatement);
            LinkedList<Statement> stmts = new LinkedList<>();
            while(!tokens.match(RIGHT_BRACE) && !tokens.match(EOF)) {
                Statement statement = parseStatement();
                stmts.add(statement);
            }
            forStatement.setBody(stmts);
            forStatement.setEnd(require(RIGHT_BRACE,forStatement));
            return forStatement;
        }

        else{
            return null;
        }
    }
    private Statement parseIfStatement() {
        if (tokens.match(IF)){
            IfStatement ifStatement = new IfStatement();
            ifStatement.setStart(tokens.consumeToken());
            require(LEFT_PAREN, ifStatement);
            ifStatement.setExpression(parseExpression());
            require(RIGHT_PAREN,ifStatement);
            require(LEFT_BRACE,ifStatement);
            LinkedList<Statement> stmts = new LinkedList<>();
            while(!tokens.match(RIGHT_BRACE) && !tokens.match(EOF)) {
                Statement statement = parseStatement();
                stmts.add(statement);
            }
            ifStatement.setTrueStatements(stmts);
            ifStatement.setEnd(require(RIGHT_BRACE,ifStatement));
            if (tokens.match(ELSE)) {
                tokens.consumeToken();
                if (tokens.match(IF)){
                    parseIfStatement();
                }
                else {
                    require(LEFT_BRACE,ifStatement);
                    LinkedList<Statement> elsestmts = new LinkedList<>();
                    while(!tokens.match(RIGHT_BRACE) && !tokens.match(EOF)) {
                        Statement statement = parseStatement();
                        elsestmts.add(statement);
                    }
                    ifStatement.setElseStatements(elsestmts);
                    ifStatement.setEnd(require(RIGHT_BRACE,ifStatement));
                }
            }
            return ifStatement;
        }
        else {
            return null;
        }
    }

    private Statement parseVarStatement() {
        if(tokens.match(VAR)){
            VariableStatement variableStatement = new VariableStatement();
            variableStatement.setStart(tokens.consumeToken());
            variableStatement.setVariableName(tokens.consumeToken().getStringValue());
            if (tokens.match(COLON)){
                require(COLON,variableStatement);
                variableStatement.setExplicitType(parseTypeLiteral().getType());
            }
            require(EQUAL,variableStatement);

            variableStatement.setExpression(parseExpression());
            variableStatement.setEnd(variableStatement.getExpression().getEnd());
            return variableStatement;
        }
        else {
            return null;
        }
    }

    private Statement parseAssignmentOrFunctionCallStatement() {
        if(tokens.match(IDENTIFIER)){
            AssignmentStatement assignmentStatement = new AssignmentStatement();
            assignmentStatement.setStart(tokens.consumeToken());
            assignmentStatement.setVariableName(assignmentStatement.getStart().getStringValue());
            if(!tokens.match(EQUAL)){
                return parseFunctionCallStatement();
            }
            require(EQUAL, assignmentStatement);
            assignmentStatement.setExpression(parseExpression());
            assignmentStatement.setEnd(assignmentStatement.getExpression().getEnd());
            return assignmentStatement;
        }
        else {
            return null;
        }
    }

    private Statement parseFunctionCallStatement() {
            Token func = tokens.lastToken();
            if(tokens.match(LEFT_PAREN)) {
                tokens.consumeToken();
                ArrayList<Expression> exps = new ArrayList<>();
                if(!tokens.match(RIGHT_PAREN)) {
                    exps.add(parseExpression());
                }
                while(tokens.match(COMMA)) {
                    tokens.consumeToken();
                    exps.add(parseExpression());
                }
                FunctionCallStatement functionCallStatement = new FunctionCallStatement(new FunctionCallExpression(func.getStringValue(),exps));
                functionCallStatement.setStart(func);
                require(RIGHT_PAREN, functionCallStatement);
                functionCallStatement.setEnd(tokens.lastToken());
                return functionCallStatement;
            }
        else {
            return null;
        }
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
    private TypeLiteral parseTypeLiteral() {
        if (tokens.match("int")) {
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.INT);
            typeLiteral.setToken(tokens.consumeToken());
            return typeLiteral;
        }
        else if (tokens.match("string")) {
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.STRING);
            typeLiteral.setToken(tokens.consumeToken());
            return typeLiteral;
        }
        else if (tokens.match("bool")) {
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.BOOLEAN);
            typeLiteral.setToken(tokens.consumeToken());
            return typeLiteral;
        }
        else if (tokens.match("object")) {
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.OBJECT);
            typeLiteral.setToken(tokens.consumeToken());
            return typeLiteral;
        }
        else if (tokens.match("list")) {
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.getListType(CatscriptType.OBJECT));
            typeLiteral.setToken(tokens.consumeToken());
            if(tokens.matchAndConsume(LESS)){
                TypeLiteral component = parseTypeLiteral();
                typeLiteral.setType(CatscriptType.getListType(component.getType()));
                require(GREATER, typeLiteral);
            }
            return typeLiteral;
        }
        TypeLiteral typeLiteral = new TypeLiteral();
        typeLiteral.setType(CatscriptType.OBJECT);
        typeLiteral.setToken(tokens.consumeToken());
        typeLiteral.addError(ErrorType.BAD_TYPE_NAME);
        return typeLiteral;
    }
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
            if (tokens.match(LEFT_PAREN)) {
                List<Expression> args = new ArrayList<Expression>();
                tokens.consumeToken();
                if (tokens.match(RIGHT_PAREN)) {
                    tokens.consumeToken();
                    FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifierToken.getStringValue(), args);
                    return functionCallExpression;
                }
                args.add(parseExpression());
                while (tokens.match(COMMA)) {
                    tokens.consumeToken();
                    args.add(parseExpression());
                }
                FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifierToken.getStringValue(), args);
                if (tokens.match(RIGHT_PAREN)) {
                    tokens.consumeToken();
                } else {
                    functionCallExpression.addError(ErrorType.UNTERMINATED_ARG_LIST);
                }
                return functionCallExpression;

            }
            return exp;
        }
        if (tokens.match(STRING)) {
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
        } else if (tokens.match(NULL)) {
            Token nullToken = tokens.consumeToken();
            NullLiteralExpression nullExpression = new NullLiteralExpression();
            nullExpression.setToken(nullToken);
            return nullExpression;
        } else if (tokens.match(LEFT_PAREN)) {
            tokens.consumeToken();
            ParenthesizedExpression expression = new ParenthesizedExpression(parseExpression());
            if (tokens.match(RIGHT_PAREN)) {
                tokens.consumeToken();
                return expression;
            } else {
                SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression(tokens.consumeToken());
                return syntaxErrorExpression;
            }

        } else if (tokens.match(LEFT_BRACKET)) {
            List<Expression> values = new ArrayList<Expression>();
            tokens.consumeToken();
            if (tokens.match(RIGHT_BRACKET)) {
                tokens.consumeToken();
                ListLiteralExpression listLiteralExpression = new ListLiteralExpression(values);
                return listLiteralExpression;
            }
            values.add(parseExpression());
            while (tokens.match(COMMA)) {
                tokens.consumeToken();
                values.add(parseExpression());
            }
            ListLiteralExpression listLiteralExpression = new ListLiteralExpression(values);
            if (tokens.match(RIGHT_BRACKET)) {
                tokens.consumeToken();
            } else {
                listLiteralExpression.addError(ErrorType.UNTERMINATED_LIST);
            }
            return listLiteralExpression;
        } else {
            throw new UnknownExpressionParseException();
            //SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression(tokens.consumeToken());
            //return syntaxErrorExpression;
        }
    }
    class UnknownExpressionParseException extends RuntimeException{

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
