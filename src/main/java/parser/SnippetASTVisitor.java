package parser;

import entity.ApiInfo;
import entity.FieldInfo;
import entity.StatementInfo;
import lombok.Getter;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class SnippetASTVisitor extends ASTVisitor{

    private String LATEST_VERSIOM = "99.99.99";

    private HashMap<String, String> variableMap;
    private HashMap<String, String> methodMap;
    private String lastAvailableVersion;
    private String projectName;
    private CompilationUnit cu;

    public SnippetASTVisitor(CompilationUnit cu, String projectName) {
        this.cu = cu;
        initializeFields(projectName);
        fetchLocalMethods();
    }

    // TODO: consider local methods
    private void fetchLocalMethods() {

    }

    private void initializeFields(String projectName) {
        this.variableMap = new HashMap<String, String>();
        this.methodMap = new HashMap<String, String>();
        this.lastAvailableVersion = LATEST_VERSIOM;
        this.projectName = projectName;
    }

    public boolean visit(VariableDeclarationStatement treeNode) {
        for (VariableDeclarationFragment vdf: (List<VariableDeclarationFragment>)treeNode.fragments()) {
            String variableName = vdf.getName().toString();
            String variableType = treeNode.getType().toString();
            variableMap.put(variableName, variableType);
        }
        return true;
    }

    public void endVisit(MethodInvocation treeNode) {
        StatementInfo statementInfo = new StatementInfo();

        statementInfo.setLastAvailableVersion(lastAvailableVersion);
        statementInfo.setApiName(treeNode.getName().toString());

        String className = treeNode.getExpression()==null? "": treeNode.getExpression().toString();
        // replace variable name with its type
        if (variableMap.containsKey(className)) {
            className = variableMap.get(className);
        }
        else if (methodMap.containsKey(className)) {
            className = methodMap.get(className);
        }
        statementInfo.setClassName(className);

        List parameters = treeNode.arguments();
        List<String> parameterList = new ArrayList<String>();
        for (Object parameter: parameters) {
            if (parameter instanceof SimpleName) {
                parameterList.add(parameter.toString());
            }
            else if (parameter instanceof MethodInvocation) {
                String methodName = parameter.toString();
                // fetch return type
                if (methodMap.containsKey(methodName)) {
                    parameterList.add(methodMap.get(methodName));
                }
                // TODO: check whether the method is in localMethods
//                else if (localMethods.contains(methodName) {
//
//                }
            }
            else if (parameter instanceof QualifiedName) {
                FieldInfo innerFieldInfo = ClientCodeParser.fetchFieldInfoByQualifiedName(projectName, parameter.toString());
                if (innerFieldInfo != null) {
                    if (innerFieldInfo.getLastAvailableVersion().compareTo(this.lastAvailableVersion) < 0) {
                        this.lastAvailableVersion = innerFieldInfo.getLastAvailableVersion();
                    }
                    parameterList.add(innerFieldInfo.getFieldType());
                }
                // TODO: deal with argument that not appears in fieldInfoMap
            }
        }
        statementInfo.setParameterList(parameterList);

        ApiInfo apiInfo = ClientCodeParser.fetchApiInfoByStatementInfo(projectName, statementInfo);
        if (apiInfo != null) {
            methodMap.put(treeNode.toString(), apiInfo.getReturnType());
            if (apiInfo.getLastAvailableVersion().compareTo(this.lastAvailableVersion) < 0) {
                this.lastAvailableVersion= apiInfo.getLastAvailableVersion();
            }
        }
    }
}
