package parser;

import entity.ApiInfo;
import entity.FieldInfo;
import entity.ProjectInfo;
import entity.StatementInfo;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import utils.Common;
import utils.Database;

import java.io.*;
import java.sql.Connection;
import java.util.*;

public class SourceCodeParser {

    private static HashMap<String, ProjectInfo> projectInfoMap;
    private static List<ApiInfo> apiInfoList;
    private static List<FieldInfo> fieldInfoList;
    private static Connection connection = null;

    public static void main(String[] args) {
        initialize();
        getInfoFromSourceCode();
        finalizeDB();
    }

    private static void initialize() {
        initializeFields();
        initializeDB();
    }

    private static void initializeFields() {
        projectInfoMap = new HashMap<String, ProjectInfo>();
        apiInfoList = new ArrayList<ApiInfo>();
        fieldInfoList = new ArrayList<FieldInfo>();

        ProjectInfo poi = new ProjectInfo("poi");
        poi.setVersionList(Arrays.asList("4.1.1", "4.1.0", "4.0.1", "4.0.0", "3.16", "3.15", "3.14",
                "3.13", "3.12", "3.11", "3.10", "3.9", "3.8", "3.7", "3.6", "3.5"));
//        poi.setVersionList(Arrays.asList("4.1.1", "3.16")); // for test
//        poi.setPathList(Arrays.asList("\\src\\java\\org\\apache\\poi\\hssf\\usermodel", "\\src\\java\\org\\apache\\poi\\ss\\usermodel"));
        projectInfoMap.put("poi", poi);

//        ProjectInfo lucene = new ProjectInfo("lucene");
//        lucene.setVersionList(Arrays.asList("8.3.0", "8.2.0", "8.1.1", "8.1.0", "8.0.0", "7.7.2",
//                "7.7.1", "7.7.0", "7.6.0", "7.5.0", "7.4.0", "7.3.1", "7.3.0", "7.2.1", "7.2.0",
//                "7.1.0", "7.0.1", "7.0.0", "6.6.6", "6.6.5"));
////        lucene.setPathList(Arrays.asList("\\lucene\\core\\src\\java\\org\\apache\\lucene\\index"));
//        projectInfoMap.put("lucene", lucene);
//
//        ProjectInfo jfreechart = new ProjectInfo("jfreechart");
//        jfreechart.setVersionList(Arrays.asList("1.5.0", "1.0.19", "1.0.18", "1.0.17", "1.0.15", "1.0.14"));
////        jfreechart.setPathList(Arrays.asList("\\src\\main\\java\\org\\jfree\\chart",
////                "\\src\\main\\java\\org\\jfree\\chart\\annotations",
////                "\\src\\main\\java\\org\\jfree\\chart\\axis",
////                "\\src\\main\\java\\org\\jfree\\chart\\block",
////                "\\src\\main\\java\\org\\jfree\\chart\\date",
////                "\\src\\main\\java\\org\\jfree\\chart\\editor",
////                "\\src\\main\\java\\org\\jfree\\chart\\encoders",
////                "\\src\\main\\java\\org\\jfree\\chart\\entity",
////                "\\src\\main\\java\\org\\jfree\\chart\\event",
////                "\\src\\main\\java\\org\\jfree\\chart\\imagemap",
////                "\\src\\main\\java\\org\\jfree\\chart\\labels",
////                "\\src\\main\\java\\org\\jfree\\chart\\needle",
////                "\\src\\main\\java\\org\\jfree\\chart\\panel",
////                "\\src\\main\\java\\org\\jfree\\chart\\plot",
////                "\\src\\main\\java\\org\\jfree\\chart\\renderer",
////                "\\src\\main\\java\\org\\jfree\\chart\\resources",
////                "\\src\\main\\java\\org\\jfree\\chart\\servlet",
////                "\\src\\main\\java\\org\\jfree\\chart\\text",
////                "\\src\\main\\java\\org\\jfree\\chart\\title",
////                "\\src\\main\\java\\org\\jfree\\chart\\ui",
////                "\\src\\main\\java\\org\\jfree\\chart\\urls",
////                "\\src\\main\\java\\org\\jfree\\chart\\utils"));
//        projectInfoMap.put("jfreechart", jfreechart);
//
//        ProjectInfo neo4j = new ProjectInfo("neo4j");
//        neo4j.setVersionList(Arrays.asList("3.5.12", "3.5.11", "3.5.9", "3.5.8", "3.5.7", "3.5.6",
//                "3.5.5", "3.5.4", "3.5.3", "3.5.2", "3.5.1", "3.5.0", "3.4.16", "3.4.14", "3.4.13",
//                "3.4.12", "3.4.11", "3.4.10", "3.4.9", "3.4.8"));
////        neo4j.setPathList(Arrays.asList(""));
//        projectInfoMap.put("neo4j", neo4j)
    }

    private static void initializeDB() {
        connection = Database.getConnection();
        Database.executeSQL(connection, "DROP TABLE IF EXISTS api_info;");
        String createApiInfoTable = "CREATE TABLE api_info (\n" +
                "  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,\n" +
                "  name VARCHAR(512) NOT NULL,\n" +
                "  project_name VARCHAR(512) NOT NULL,\n" +
                "  package_name VARCHAR(512) NOT NULL,\n" +
                "  class_name VARCHAR(512) NOT NULL,\n" +
                "  return_type VARCHAR(512) NOT NULL,\n" +
                "  last_available_version VARCHAR(512) NOT NULL,\n" +
                "  parameter_list VARCHAR(512) NOT NULL,\n" +
                "  exception_list VARCHAR(512) NOT NULL\n" +
                ");";
        Database.executeSQL(connection, createApiInfoTable);

        Database.executeSQL(connection, "DROP TABLE IF EXISTS field_info;");
        String createFieldInfoTable =  "CREATE TABLE field_info (\n" +
                "  id INT UNSIGNED AUTO_INCREMENT  PRIMARY KEY,\n" +
                "  name VARCHAR(512) NOT NULL,\n" +
                "  field_type VARCHAR(512) NOT NULL,\n" +
                "  project_name VARCHAR(512) NOT NULL,\n" +
                "  package_name VARCHAR(512) NOT NULL,\n" +
                "  class_name VARCHAR(512) NOT NULL,\n" +
                "  last_available_version VARCHAR(512) NOT NULL\n" +
                ");";
        Database.executeSQL(connection, createFieldInfoTable);
    }

    private static void finalizeDB() {
        Database.closeConnection(connection);
    }

    public static void getInfoFromSourceCode() {
        String parentDir = "E:\\data\\source_code";
        HashMap<String, ProjectInfo> result = new HashMap<String, ProjectInfo>();
        for (String projectName: projectInfoMap.keySet()) {
            ProjectInfo projectInfo = projectInfoMap.get(projectName);
            Long startTime = System.currentTimeMillis();   //获取开始时间

            System.out.println("Parsing project " + projectName);
            String dir = parentDir + "\\" + projectName + "\\";
            for (String version: projectInfo.getVersionList()) {
                System.out.println("    Parsing version " + version);
                List<String> fileList = getJavaFileList(dir + version);
                // the following line is for test
//                List<String> fileList = Arrays.asList(dir + version + "\\src\\java\\org\\apache\\poi\\ss\\usermodel\\CellStyle.java");
                for (String filePath : fileList) {
                    String sourceCode = Common.readTextFile(filePath);
                    updateProjectInfo(sourceCode, projectName, version, filePath);
//                    projectInfo.setApiInfoMap(projectInfoMap.get(projectName).getApiInfoMap());
                }

                storeApiInfoListIntoDB(apiInfoList);
                storeFieldInfoListIntoDB(fieldInfoList);

                // only parse specified files
//                for (String pathToParse: projectInfo.getPathList()) {
//                    String filePath = dir + version + pathToParse;
//                    System.out.println("Parsing filePath " + filePath);
//                    File file = new File(filePath);
//                    for (File f: file.listFiles()) {
//                        if (f.isFile()) {
//                            Long fileLength = f.length();
//                            if (fileLength > fileLength.intValue()) {
//                                System.out.println("Caution!!! FileLength exceed MAX INT value!");
//                            }
//                            byte[] fileContent = new byte[fileLength.intValue()];
//                            try {
//                                FileInputStream inputStream = new FileInputStream(f);
//                                inputStream.read(fileContent);
//                                inputStream.close();
//
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            String sourceCode = new String(fileContent);
////                        System.out.println(sourceCode);
//                            projectInfo.setApiInfoMap(updateApiInfo(sourceCode, projectInfo.getApiInfoMap(), version));
//                        }
//                    }
//                }

                System.out.println("        Current size of fieldInfoMap: " + projectInfo.getFieldInfoMap().size());
                System.out.println("        Current size of apiInfoMap: " + projectInfo.getApiInfoMap().size());
            }
            result.put(projectName, projectInfo);
            String resultFilePath = "E:\\data\\";
            String resultFileName = "ApiVersionStatistics-" + projectInfo.getName();
            writeFieldInfosToFile(projectInfo.getFieldInfoMap(), resultFilePath + resultFileName + "-field.txt");
            writeApiInfosToFile(projectInfo.getApiInfoMap(), resultFilePath + resultFileName + "-api.txt");

            Long endTime = System.currentTimeMillis(); //获取结束时间
            System.out.println("Running lasts " + (endTime - startTime) / 1000 + " seconds");
        }
        projectInfoMap = result;
    }

    private static List<String> getJavaFileList(String filePath) {
        List<String> fileList = new ArrayList<String>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        if (files != null) {
            for (File f: files) {
                if (f.isFile() && f.toString().endsWith(".java")) {
                    fileList.add(f.toString());
                }
                else {
                    fileList.addAll(getJavaFileList(f.getPath()));
                }
            }
        }
        return fileList;
    }

//    private static String getSourceCodeFromFile(String filePath) {
//        File file = new File(filePath);
//        Long fileLength = file.length();
//        if (fileLength > fileLength.intValue()) {
//            System.out.println("Caution!!! FileLength exceed MAX INT value!");
//        }
//        byte[] fileContent = new byte[fileLength.intValue()];
//        try {
//            FileInputStream inputStream = new FileInputStream(file);
//            inputStream.read(fileContent);
//            inputStream.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String sourceCode = new String(fileContent);
//        return sourceCode;
//    }

    private static void updateProjectInfo(String sourceCode, String projectName, String version, String filePath) {

        ProjectInfo projectInfo = projectInfoMap.get(projectName);
        HashMap<String, FieldInfo> fieldInfoMap = projectInfo.getFieldInfoMap();
        HashMap<String, ApiInfo> apiInfoMap = projectInfo.getApiInfoMap();

        ASTParser parser = ASTParser.newParser(AST.JLS12);
        parser.setSource(sourceCode.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        Map options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5); //or newer version
        parser.setCompilerOptions(options);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        if (!cu.types().isEmpty()) {
            if (cu.types().get(0) instanceof TypeDeclaration) {
                // get class
                TypeDeclaration typeDec = (TypeDeclaration) cu.types().get(0);
                PackageDeclaration packageDeclaration = ((CompilationUnit) typeDec.getParent()).getPackage();
                String packageName = "";
                if (packageDeclaration != null) {
                    packageName = packageDeclaration.getName().toString();
                }
                String className = typeDec.getName().toString();

                // get FieldInfo
                for (FieldDeclaration fieldDeclaration: typeDec.getFields()) {
                    if (Modifier.isPrivate(fieldDeclaration.getModifiers()) == true) {
                        continue;
                    }
                    FieldInfo fieldInfo = new FieldInfo();
                    fieldInfo.setName(((VariableDeclaration)fieldDeclaration.fragments().get(0)).getName().toString());
                    fieldInfo.setProjectName(projectName);
                    fieldInfo.setFieldType(fieldDeclaration.getType().toString());
                    fieldInfo.setClassName(className);
                    fieldInfo.setPackageName(packageName);
                    fieldInfo.setLastAvailableVersion(version);
                    String fieldSignature = fieldInfo.getClassName() + "." + fieldInfo.getName();
                    if (fieldInfoMap.containsKey(fieldSignature) == false) {
                        fieldInfoMap.put(fieldSignature, new FieldInfo(fieldInfo));
                        fieldInfoList.add(new FieldInfo(fieldInfo));
                    }
                }

                for (MethodDeclaration methodDec: typeDec.getMethods()) {
                    ApiInfo apiInfo = getApiInfoByMethodDeclaration(methodDec, projectName, packageName, className, version);
                    if (apiInfo != null) {
                        //generate signature
//                        String methodSignature = apiInfo.getReturnType();
//                        if (methodSignature.length() > 0) {
//                            methodSignature += " ";
//                        }
//                        methodSignature = apiInfo.getPackageName() + "." + apiInfo.getClassName() + "." +
//                                apiInfo.getName() + apiInfo.getParameterList();
                        // e.g. CellStyle.setFillPattern[short]
                        String methodSignature = apiInfo.getClassName() + "." + apiInfo.getName() +
                                apiInfo.getParameterList();
                        if (apiInfoMap.containsKey(methodSignature) == false) {
                            apiInfoMap.put(methodSignature, apiInfo);
                            apiInfoList.add(apiInfo);
                        }
                    }
                }
            }
            else if (cu.types().get(0) instanceof EnumDeclaration) {
                EnumDeclaration enumDec = (EnumDeclaration) cu.types().get(0);
                PackageDeclaration packageDeclaration = ((CompilationUnit) enumDec.getParent()).getPackage();
                String packageName = "";
                if (packageDeclaration != null) {
                    packageName = packageDeclaration.getName().toString();
                }
                String className = enumDec.getName().toString();
                List<String> constantList = new ArrayList<String>();
                for (EnumConstantDeclaration enumConst: (List<EnumConstantDeclaration>)enumDec.enumConstants()) {
                    constantList.add(enumConst.getName().toString());
                }
                List bodyDeclarations = enumDec.bodyDeclarations();
                for (int i=0; i<bodyDeclarations.size(); i++) {
                    if (bodyDeclarations.get(i) instanceof MethodDeclaration) {
                        MethodDeclaration methodDec = (MethodDeclaration) bodyDeclarations.get(i);
                        ApiInfo apiInfo = getApiInfoByMethodDeclaration(methodDec, projectName, packageName, className, version);
                        if (apiInfo != null) {
                            String originalClassName = apiInfo.getClassName();
                            for (String constant: constantList) {
                                apiInfo.setClassName(originalClassName + "." + constant);
                                // e.g. IndexedColors.AQUA.getIndex[]
                                String methodSignature = apiInfo.getClassName() + "." +
                                        apiInfo.getName() + apiInfo.getParameterList();
                                if (apiInfoMap.containsKey(methodSignature) == false) {
                                    apiInfoMap.put(methodSignature, new ApiInfo(apiInfo));
                                    apiInfoList.add(new ApiInfo(apiInfo));
                                }
                            }
                        }
                    }
                }
            }
        }
        projectInfo.setFieldInfoMap(fieldInfoMap);
        projectInfo.setApiInfoMap(apiInfoMap);

        projectInfoMap.put(projectName, projectInfo);
    }

    private static ApiInfo getApiInfoByMethodDeclaration(MethodDeclaration methodDec, String projectName,
                                                         String packageName, String className, String version) {
        if (Modifier.isPrivate(methodDec.getModifiers()) == true) {
            return null;
        }
        ApiInfo apiInfo = new ApiInfo();
        apiInfo.setName(methodDec.getName().toString());
        apiInfo.setProjectName(projectName);
        apiInfo.setPackageName(packageName);
        apiInfo.setClassName(className);
        apiInfo.setReturnType(methodDec.getReturnType2()==null? "": methodDec.getReturnType2().toString());
        apiInfo.setLastAvailableVersion(version);
        apiInfo.setExceptionList(methodDec.thrownExceptionTypes());
        List<String> methodParameterList = new ArrayList<String>();
        Iterator<SingleVariableDeclaration> parameters = methodDec.parameters().iterator();
        while (parameters.hasNext()) {
            SingleVariableDeclaration parameter = parameters.next();
            methodParameterList.add(parameter.getType().toString());
        }
        apiInfo.setParameterList(methodParameterList);
        return apiInfo;
    }

    private static void storeApiInfoListIntoDB(List<ApiInfo> apiInfoList) {
        if (apiInfoList.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO api_info VALUES ";
        String template = "(0,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"),";
        for (ApiInfo apiInfo: apiInfoList) {
            sql += String.format(template, apiInfo.getName(), apiInfo.getProjectName(), apiInfo.getPackageName(),
                    apiInfo.getClassName(), apiInfo.getReturnType(), apiInfo.getLastAvailableVersion(),
                    apiInfo.getParameterList(), apiInfo.getExceptionList());
        }
        sql = sql.substring(0, sql.length() - 1);
        sql += ";";
        Database.executeSQL(connection, sql);
        System.out.println("        Inserted " + apiInfoList.size() + " api infos into api_info");

        // clear apiInfoList after insertation
        apiInfoList.clear();
    }

    private static void storeFieldInfoListIntoDB(List<FieldInfo> fieldInfoList) {
        if (fieldInfoList.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO field_info VALUES ";
        String template = "(0,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"),";
        for (FieldInfo fieldInfo: fieldInfoList) {
            sql += String.format(template, fieldInfo.getName(), fieldInfo.getFieldType(),
                    fieldInfo.getProjectName(), fieldInfo.getPackageName(),
                    fieldInfo.getClassName(), fieldInfo.getLastAvailableVersion());
        }
        sql = sql.substring(0, sql.length() - 1);
        sql += ";";
        Database.executeSQL(connection, sql);
        System.out.println("        Inserted " + fieldInfoList.size() + " field infos into field_info");

        // clear fieldInfoList after insertation
        fieldInfoList.clear();
    }

    private static void writeFieldInfosToFile(HashMap<String, FieldInfo> fieldInfoMap, String fileName) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            for (String signature: fieldInfoMap.keySet()) {
                out.write(signature + "," + fieldInfoMap.get(signature).getLastAvailableVersion() + "\n");
            }
            out.close();
            System.out.println("Succeed in writing result to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeApiInfosToFile(HashMap<String, ApiInfo> apiInfoMap, String fileName) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            for (String signature: apiInfoMap.keySet()) {
                out.write(signature + "," + apiInfoMap.get(signature).getLastAvailableVersion() + "\n");
            }
            out.close();
            System.out.println("Succeed in writing result to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ApiInfo getApiInfoByStatementInfo(String projectName, StatementInfo statementInfo) {
        String signature = statementInfo.getClassName();
        if (signature.length() > 0) {
            signature += ".";
        }
        signature += statementInfo.getApiName() + statementInfo.getParameterList();

        HashMap<String, ApiInfo> apiInfoMap = projectInfoMap.get(projectName).getApiInfoMap();
        if (apiInfoMap.containsKey(signature) == false) {
            return null;
        }
        ApiInfo apiInfo = apiInfoMap.get(signature);
        System.out.println(signature + "," + apiInfo.getLastAvailableVersion());
        return apiInfo;
    }

    public static FieldInfo getFieldInfoByQualifiedName(String projectName, String qualifiedName) {
        HashMap<String, FieldInfo> fieldInfoMap = projectInfoMap.get(projectName).getFieldInfoMap();
        if (fieldInfoMap.containsKey(qualifiedName) == false) {
            return null;
        }
        FieldInfo fieldInfo = fieldInfoMap.get(qualifiedName);
        System.out.println(qualifiedName + "," + fieldInfo.getLastAvailableVersion());
        return fieldInfo;
    }

}
