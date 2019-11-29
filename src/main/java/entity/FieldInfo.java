package entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldInfo {
    private String name;
    private String fieldType;
    private String projectName;
    private String packageName;
    private String className;
    private String lastAvailableVersion;

    public FieldInfo() {
        this.name = "";
        this.fieldType = "";
        this.projectName = "";
        this.packageName = "";
        this.className = "";
        this.lastAvailableVersion = "";
    }

    public FieldInfo(String name, String fieldType, String projectName, String packageName,
                     String className, String lastAvailableVersion) {
        this.name = name;
        this.fieldType = fieldType;
        this.projectName = projectName;
        this.packageName = packageName;
        this.className = className;
        this.lastAvailableVersion = lastAvailableVersion;
    }

    public FieldInfo(FieldInfo fieldInfo) {
        this.name = fieldInfo.getName();
        this.fieldType = fieldInfo.getFieldType();
        this.projectName = fieldInfo.getProjectName();
        this.packageName = fieldInfo.getPackageName();
        this.className = fieldInfo.getClassName();
        this.lastAvailableVersion = fieldInfo.getLastAvailableVersion();
    }
}
