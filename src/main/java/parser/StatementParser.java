package parser;

import entity.ApiInfo;
import entity.FieldInfo;
import entity.StatementInfo;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatementParser {

    private static String LATEST_VERSIOM = "99.99.99";

    private static HashMap<String, String> variableMap;
    private static String lastAvailableVersion;
    private static String projectName;

    public static void main(String[] args) {
    }

    public static void initializeFields(String _projectName) {
        variableMap = new HashMap<String, String>();
        lastAvailableVersion = LATEST_VERSIOM;
        projectName = _projectName;
    }

    public static StatementInfo parseStatement(Statement statement) {

        StatementInfo statementInfo = new StatementInfo();
        statementInfo.setLastAvailableVersion(lastAvailableVersion);

        int statementType = statement.getNodeType();
        Expression expression = null;
        switch (statementType) {
            case Statement.VARIABLE_DECLARATION_STATEMENT:
                VariableDeclarationStatement vd = (VariableDeclarationStatement) statement;
                for (VariableDeclarationFragment vdf: (List<VariableDeclarationFragment>)vd.fragments()) {
                    String variableName = vdf.getName().toString();
                    String variableType = vd.getType().toString();
                    variableMap.put(variableName, variableType);
                    // check whether there is a MethodInvocation at the right of the equal mark
                    // if so, parse it recursively
                    Expression initializer = vdf.getInitializer();
                    if (initializer != null && initializer instanceof MethodInvocation) {
                        parseMethodInvocation((MethodInvocation) initializer);
                    }
                }
                return null;
            case Statement.EXPRESSION_STATEMENT:
                expression = ((ExpressionStatement) statement).getExpression();
                break;
            case Statement.IF_STATEMENT:
                IfStatement ifStatement = (IfStatement) statement;
                expression = ifStatement.getExpression();
                parseStatement(ifStatement.getThenStatement());
                parseStatement(ifStatement.getElseStatement());
                break;
            case Statement.RETURN_STATEMENT:
                expression = ((ReturnStatement) statement).getExpression();
                break;
            default:
                System.out.println("    !!! Need to parse " + statement.getClass().getName());

        }
        parseExpression(expression, statementInfo);

        return statementInfo;
    }

    private static void parseExpression(Expression expression, StatementInfo statementInfo) {
        if (expression instanceof MethodInvocation) {
            statementInfo =
                    parseMethodInvocation((MethodInvocation)expression);
        }

        else if (expression instanceof Assignment) {
            System.out.println("!!! Assignment: " + expression.toString());
        }
    }

    private static StatementInfo parseMethodInvocation(MethodInvocation mi) {
        StatementInfo statementInfo = new StatementInfo();

        statementInfo.setApiName(mi.getName().toString());
        String className = mi.getExpression().toString();
        // replace variable name with its type
        if (variableMap.containsKey(className)) {
            className = variableMap.get(className);
        }
        statementInfo.setClassName(className);

        List parameters = mi.arguments();
        List<String> parameterList = new ArrayList<String>();
        for (Object parameter: parameters) {
            if (parameter instanceof SimpleName) {
                parameterList.add(parameter.toString());
            }
            else if (parameter instanceof MethodInvocation) {
                StatementInfo innerStatementInfo =
                        parseMethodInvocation((MethodInvocation) parameter);
                if (innerStatementInfo != null) {
                    if (innerStatementInfo.getLastAvailableVersion().compareTo(statementInfo.getLastAvailableVersion()) < 0) {
                        statementInfo.setLastAvailableVersion(innerStatementInfo.getLastAvailableVersion());
                    }
                    parameterList.add(innerStatementInfo.getReturnType());
                }
                // TODO: deal with argument that not appears in apiInfoMap
            }
            else if (parameter instanceof QualifiedName) {
//                FieldInfo innerFieldInfo = SourceCodeParser.getFieldInfoByQualifiedName(projectName, argument.toString());
                FieldInfo innerFieldInfo = ClientCodeParser.fetchFieldInfoByQualifiedName(projectName, parameter.toString());
                if (innerFieldInfo != null) {
                    if (innerFieldInfo.getLastAvailableVersion().compareTo(statementInfo.getLastAvailableVersion()) < 0) {
                        statementInfo.setLastAvailableVersion(innerFieldInfo.getLastAvailableVersion());
                    }
                    parameterList.add(innerFieldInfo.getFieldType());
                }
                // TODO: deal with argument that not appears in fieldInfoMap
            }
        }
        statementInfo.setParameterList(parameterList);
//        ApiInfo apiInfo = SourceCodeParser.getApiInfoByStatementInfo(projectName, statementInfo);
        ApiInfo apiInfo = ClientCodeParser.fetchApiInfoByStatementInfo(projectName, statementInfo);
        if (apiInfo != null) {
            statementInfo.setReturnType(apiInfo.getReturnType());
            if (apiInfo.getLastAvailableVersion().compareTo(statementInfo.getLastAvailableVersion()) < 0) {
                statementInfo.setLastAvailableVersion(apiInfo.getLastAvailableVersion());
            }
        }

        return statementInfo;
    }

}
