package entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class StatementInfo {
    private String apiName;
    private String packageName;
    private String className;
    private String returnType;
    private String lastAvailableVersion;
    private List<String> parameterList;
//    private HashMap<String, String> variableMap;

    public StatementInfo() {
        this.apiName = "";
        this.packageName = "";
        this.className = "";
        this.returnType = "";
        this.lastAvailableVersion = "";
        this.parameterList = new ArrayList<String>();
        this.parameterList = new ArrayList<String>();
//        variableMap = new HashMap<String, String>();
    }
}
