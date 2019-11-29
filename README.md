#FunctionalFeatureExtraction

##Usage
1. Set MySQL database properties in `utils/Database.java`
2. Put source code files in `data/source_code` and modify `initializeFields()` in `parser/SourceCodeParser.java`
3. Run `parser/SourceCodeParser.java` to parse source code. Result will be stored in MySQL
4. Run `parser/ClientCodeParser.java` to parse client code snippet and get matched API calls. Result will be printed to the console