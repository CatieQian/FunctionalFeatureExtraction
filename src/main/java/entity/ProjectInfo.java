package entity;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class ProjectInfo {
    private String name;
    private List<String> versionList;
//    private List<String> pathList; // path need to parse (contain entities)
    private HashMap<String, ApiInfo> apiInfoMap;
    private HashMap<String, FieldInfo> fieldInfoMap;
    public ProjectInfo(String name) {
        this.name = name;
        this.apiInfoMap = new HashMap<String, ApiInfo>();
        this.fieldInfoMap = new HashMap<String, FieldInfo>();
    }
}
