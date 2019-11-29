package parser;

import entity.ApiInfo;
import entity.FieldInfo;
import entity.StatementInfo;
import org.eclipse.jdt.core.dom.*;
import utils.Common;
import utils.Database;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ClientCodeParser {

    private static int flag;
    private static Connection connection = null;
    private static String PROJECT_NAME = "poi";

    public static void main(String[] args) {

        initializeDB();

        testWithExample();
//        testWithFuncionalPosts();

        finalizeDB();
    }

    private static void testWithExample() {
        String testCode = "    // Aqua background\n" +
                "CellStyle style = wb.createCellStyle();\n" +
                "style.setFillBackgroundColor(IndexedColors.AQUA.getIndex());\n" +
                "style.setFillPattern(CellStyle.BIG_SPOTS);\n" +
                "row.setRowStyle(style);";

        CompilationUnit cu = getCompilationUnitFromString(testCode);
        System.out.println("【Matched source code information】");
        parseClientCodeByVisitor(cu, PROJECT_NAME);

    }

    private static void testWithFuncionalPosts() {
        String filePath = "E:\\data\\client_code\\func_posts_readable.txt";
        String fileContent = Common.readTextFile(filePath);
        List<String> snippetList = Arrays.asList(fileContent.split("#@"));
        for (String clientCode: snippetList) {
            CompilationUnit cu = getCompilationUnitFromString(clientCode);
            parseClientCodeByVisitor(cu, PROJECT_NAME);
        }

    }

    private static void initializeDB() {
        connection = Database.getConnection();
    }

    private static void finalizeDB() {
        Database.closeConnection(connection);
    }

    private static ASTParser getASTParser(String clientCode) {
        ASTParser parser = ASTParser.newParser(AST.JLS12);
        parser.setSource(clientCode.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        return parser;
    }

    private static CompilationUnit getCompilationUnitFromString(String clientCode) {

        System.out.println("【Start parsing following code snippet】");
        System.out.println(clientCode);

        ASTParser parser = getASTParser(clientCode);
        ASTNode cu = (CompilationUnit) parser.createAST(null);
        if (((CompilationUnit) cu).types().isEmpty()) {
            flag = 1;
            String s1 = "public class sample{\n" + clientCode + "\n}";
            parser = getASTParser(s1);
            cu = parser.createAST(null);
            cu.accept(new ASTVisitor() {
                public boolean visit(MethodDeclaration node) {
                    flag = 2;
                    return false;
                }
            });
            if (flag == 1) {
                s1 = "public class sample{\n public void foo(){\n" + clientCode + "\n}\n}";
                parser = getASTParser(s1);
                cu = parser.createAST(null);
            }
            if (flag == 2) {
                s1 = "public class sample{\n" + clientCode + "\n}";
                parser = getASTParser(s1);
                cu = parser.createAST(null);
            }
        }
        else {
            parser = getASTParser(clientCode);
            cu = parser.createAST(null);
        }
        return (CompilationUnit) cu;
    }

    private static void parseClientCode(CompilationUnit cu, String projectName) {
        if (!cu.types().isEmpty()) {
            // get class
            TypeDeclaration typeDec = (TypeDeclaration) cu.types().get(0);
            MethodDeclaration methodDec[] = typeDec.getMethods();
            for (MethodDeclaration method: methodDec) {
                List<Statement> statementList = method.getBody().statements();
                StatementParser.initializeFields(projectName);
                for (Statement statement: statementList) {
                    StatementInfo statementInfo =
                            StatementParser.parseStatement(statement);
                    if (statementInfo != null) {
                    }
                }
            }
        }
    }

    private static void parseClientCodeByVisitor(CompilationUnit cu, String projectName) {

        if (!cu.types().isEmpty()) {
            SnippetASTVisitor snippetASTVisitor = new SnippetASTVisitor(cu, projectName);
            cu.accept(snippetASTVisitor);
            System.out.println("【Last available version】");
            System.out.println("    " + snippetASTVisitor.getLastAvailableVersion());
        }
    }

    public static ApiInfo fetchApiInfoByStatementInfo(String projectName, StatementInfo statementInfo) {
        String template = "SELECT * FROM api_info\n" +
                "WHERE name LIKE \"%s\"\n" +
                "  AND project_name LIKE \"%s\"\n" +
                "  AND package_name LIKE \"%s\"\n" +
                "  AND class_name LIKE \"%s\"\n" +
                "  AND return_type LIKE \"%s\"\n" +
                "  AND parameter_list LIKE \"%s\";";
        String sql = String.format(template,
                statementInfo.getApiName()==""? "%": statementInfo.getApiName(),
                projectName,
                statementInfo.getPackageName()==""? "%": statementInfo.getPackageName(),
                statementInfo.getClassName()==""? "%": statementInfo.getClassName(),
                statementInfo.getReturnType()==""? "%": statementInfo.getReturnType(),
                statementInfo.getParameterList().toString());

        HashMap<String, String> resultMap = Database.executeSQL(connection, sql);
        ApiInfo apiInfo = null;
        if (resultMap != null && resultMap.size() > 0) {
            apiInfo = new ApiInfo(resultMap.get("name"), resultMap.get("project_name"),
                    resultMap.get("package_name"), resultMap.get("class_name"),
                    resultMap.get("return_type"), resultMap.get("last_available_version"),
                    Common.stringToList(resultMap.get("parameter_list")),
                    Common.stringToList(resultMap.get("exception_list")));
            System.out.println("    " + apiInfo.getClassName() + "." + apiInfo.getName() + apiInfo.getParameterList().toString() + "," + apiInfo.getLastAvailableVersion());
        }

        return apiInfo;
    }

    public static FieldInfo fetchFieldInfoByQualifiedName(String projectName, String qualifiedName) {
        String template = "SELECT * FROM field_info\n" +
                "WHERE name LIKE \"%s\"\n" +
                "  AND project_name LIKE \"%s\"\n" +
                "  AND package_name LIKE \"%s\"\n" +
                "  AND class_name LIKE \"%s\";";
        String name = qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1, qualifiedName.length());
        qualifiedName = qualifiedName.replace(name, "");
        String className = "";
        String packageName = "";
        if (qualifiedName.length() > 0) {
            qualifiedName = qualifiedName.substring(0, qualifiedName.length() - 1);
            className = qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1, qualifiedName.length());
            qualifiedName = qualifiedName.replace(className, "");
            if (qualifiedName.length() > 0) {
                qualifiedName = qualifiedName.substring(0, qualifiedName.length() - 1);
                packageName = qualifiedName;
            }
        }
        String sql = String.format(template,
                name==""? "%": name,
                projectName,
                packageName==""? "%": packageName,
                className==""? "%": className);
        HashMap<String, String> resultMap = Database.executeSQL(connection, sql);
        FieldInfo fieldInfo = null;
        if (resultMap != null && resultMap.size() > 0) {
            fieldInfo = new FieldInfo(resultMap.get("name"), resultMap.get("field_type"),
                    resultMap.get("project_name"), resultMap.get("package_name"),
                    resultMap.get("class_name"), resultMap.get("last_available_version"));
            System.out.println("    " + className + "." + name + "," + fieldInfo.getLastAvailableVersion());
        }

        return fieldInfo;
    }
}
