package entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ApiInfo {
    private String name;
    private String projectName;
    private String packageName;
    private String className;
    private String returnType;
    private String lastAvailableVersion;
    private List<String> parameterList;
    private List<String> exceptionList;

    public ApiInfo() {
        this.name = "";
        this.projectName = "";
        this.packageName = "";
        this.className = "";
        this.returnType = "";
        this.lastAvailableVersion = "";
        this.parameterList = new ArrayList<String>();
        this.exceptionList = new ArrayList<String>();
    }

    public ApiInfo(String name, String projectName, String packageName, String className,
                   String returnType, String lastAvailableVersion, List<String> parameterList,
                   List<String> exceptionList) {
        this.name = name;
        this.projectName = projectName;
        this.packageName = packageName;
        this.className = className;
        this.returnType = returnType;
        this.lastAvailableVersion = lastAvailableVersion;
        this.parameterList = parameterList;
        this.exceptionList = exceptionList;
    }

    public ApiInfo(ApiInfo apiInfo) {
        this.name = apiInfo.getName();
        this.projectName = apiInfo.getProjectName();
        this.packageName = apiInfo.getPackageName();
        this.className = apiInfo.getClassName();
        this.returnType = apiInfo.getReturnType();
        this.lastAvailableVersion = apiInfo.getLastAvailableVersion();
        this.parameterList = apiInfo.getParameterList();
        this.exceptionList = apiInfo.getExceptionList();
    }

}
