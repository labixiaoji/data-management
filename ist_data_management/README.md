## DataManagement

#### 项目Jar包地址

> https://jbox.sjtu.edu.cn/l/h1iWAW



以我目前的理解，创建数据模型时除了模态信息，更重要的是要选择好模型的存储形态（结构、半结构、非结构）：
- 如果是结构需要让用户指定需要哪些column（前端就是之前讨论的那样）
- 如果是半结构需要让用户指定哪些field，前端或许可以表现成下面的样子（先不支持数组），然后交给后端解析
```json
{
  "f1": "number",
  "f2": "string",
  "f3": {
    "f4": "number"
  }
}
```
- 如果是非结构就不需要指定

当明确了存储形态后，质量规则的形态也一起确定了，看代码的话，可以看到DataModel和QualityRule都有结构、半结构、非结构的三个子类

数据模型的存储形态和数据源没有必然联系，比如结构化的数据模型，其实也可以使用非结构数据源

postgresql和mongodb已经连接了服务器，不必再进行本地配置

写代码时最好闲着没事就格式化一下代码

### addDataModel

这里详细说明一下addDataModel这个接口怎么请求

请求体在java里长这样，看注释：
```java
class AddModelFromVo {
    // 指定这是什么类型的model，1：结构，2：半结构，3：非结构
    private Integer type;
    // model的名字，不允许重复
    private String modelName;
    // 指定这个model是什么模态的，1：时序，2：图片，3：文本
    private Integer modal;
    // 标签
    private String tag;
    // 业务域
    private String domain;
    // 描述
    private String description;
    // 这个model有哪些属性
    private List<Field> fields;
    // 哪些过滤规则
    private List<RuleFromVo> rules;
    // 哪些数据源
    private List<SourceFromVo> sources;
}

class Field {
    // 属性名
    private String name;
    // 类型，1：数字，2：字符串
    private Integer type;
    // 描述
    private String description;
    // 是否敏感
    private Boolean sensitive;
    // 是否加密
    private Boolean encrypt;
    // 密级
    private Integer secretLevel;
}

// 下面具体说
class RuleFromVo {
    private Integer ruleType;
    private Object rule;
}

// 下面具体说
class SourceFromVo {
    private Integer sourceType;
    private Object dataSource;
}
```

#### RuleFromVo
model的type是直接决定了rule的类型的（结构、半结构、非结构），每种类型的rule下又有不同类型的具体rule，比如结构化rule下有比较大小关系的规则和判断非空的规则等等

现有的一些rule（持续更新）：
- 结构化rule
  - ruleType = 1
    ```java
    class ComparisonRule {
        public static final Integer TYPE_GE = 1;
        public static final Integer TYPE_GT = 2;
        public static final Integer TYPE_EQ = 3;
        public static final Integer TYPE_LE = 4;
        public static final Integer TYPE_LT = 5;
        private String description;
        private String leftFieldName;
        private String rightFieldName;
        private Integer type;
    }
    ```
- 半结构化rule
  - ruleType = 1
    ```java
    class NotNullRule {
        private String description;
        private String fieldName;
    }
    ```
- 非结构化rule
    - ruleType = 1
      ```java
      class SizeRule {
          private String description;
          private Integer min;
          private Integer max;  
      }
      ```

#### SourceFromVo

sourceType目前有四种，应该不会有变化：
- sourceType = 1，postgresql
  ```java
  public class DataSource {
        // 和sourceType一致
        private Integer type;
        // 例子：jdbc:postgresql://124.222.140.214:5666/data_management?user=postgres&password=123qweasd
        private String url;
        // 标签
        private String tag;
        // 描述
        private String description;
        // 表名
        private String tableName;
        // sourceFieldName : targetFieldName，注意这里的targetFieldName一定得是model里定义的
        private Map<String, String> fieldMap;
  }
  ```
- sourceType = 2，mongodb
    ```java
    public class DataSource {
          // 和sourceType一致
          private Integer type;
          // 例子：mongodb://124.222.140.214:27088
          private String url;
          // 标签
          private String tag;
          // 描述
          private String description;
          // 数据库名
          private String databaseName;
          // 集合名
          private String collectionName;
          // 和postgresql基本一致，但由于mongodb里会有嵌套的对象，所以属性之间需要使用分隔符
          // 前后端交互时都使用"!^@"作为分隔符，比如"col5!^@col53!^@col531"，但在前端展示的时候替换为"."
          private Map<String, String> fieldMap;
    }
    ```
- sourceType = 3，influxdb，待补充
- sourceType = 4，directory，待补充

目前用过的两个可行的body：
```json
{
    // structured
    "type": 1,
    "modelName": "cn_test_structured_model",
    // temporal
    "modal": 1,
    "tag": "testTag",
    "domain": "testDomain",
    "description": "testDescription",
    "fields": [
        {
            "name": "model_col1",
            // number
            "type": 1,
            "description": "testDescription",
            "sensitive": false,
            "encrypt": false,
            "secretLevel": 0
        },
        {
            "name": "model_col2",
            // number
            "type": 1,
            "description": "testDescription",
            "sensitive": false,
            "encrypt": false,
            "secretLevel": 0
        },
        {
            "name": "model_col3",
            // string
            "type": 2,
            "description": "testDescription",
            "sensitive": false,
            "encrypt": false,
            "secretLevel": 0
        },
        {
            "name": "model_col4",
            // number
            "type": 1,
            "description": "testDescription",
            "sensitive": false,
            "encrypt": false,
            "secretLevel": 0
        }
    ],
    "rules": [
        {
            "ruleType": 1,
            "rule": {
                "description": "testDescription",
                "leftFieldName": "model_col1",
                "rightFieldName": "model_col2",
                "type": 3
            }
        }
    ],
    "sources": [
        {
            // postgresql
            "sourceType": 1,
            "dataSource": {
                "type": 1,
                "url": "jdbc:postgresql://124.222.140.214:5666/data_management?user=postgres&password=123qweasd",
                "tableName": "test_source",
                "tag": "testTag",
                "description": "testDescription",
                "fieldMap": {
                    "source_col1": "model_col1",
                    "source_col2": "model_col2",
                    "source_col3": "model_col3",
                    "source_col4": "model_col4"
                }
            }
        },
        {
            // mongodb
            "sourceType": 2,
            "dataSource": {
                "type": 2,
                "url": "mongodb://124.222.140.214:27088",
                "databaseName": "data_management",
                "collectionName": "test_mongodb_source",
                "tag": "testTag",
                "description": "testDescription",
                "fieldMap": {
                    "col1": "model_col1",
                    "col5!^@col53!^@col531": "model_col2",
                    "col2": "model_col3",
                    "col4": "model_col4"
                }
            }
        }
    ]
}
```
```json
{
    // semi_structured
    "type": 2,
    "modelName": "cn_test_semi_structured_model",
    // temporal
    "modal": 1,
    "tag": "testTag",
    "domain": "testDomain",
    "description": "testDescription",
    "fields": [
        {
            "name": "model_col1",
            // number
            "type": 1,
            "description": "testDescription",
            "sensitive": false,
            "encrypt": false,
            "secretLevel": 0
        },
        {
            "name": "model_col2!^@col21!^@col211",
            // number
            "type": 1,
            "description": "testDescription",
            "sensitive": false,
            "encrypt": false,
            "secretLevel": 0
        },
        {
            "name": "model_col3",
            // string
            "type": 2,
            "description": "testDescription",
            "sensitive": false,
            "encrypt": false,
            "secretLevel": 0
        },
        {
            "name": "model_col4",
            // number
            "type": 1,
            "description": "testDescription",
            "sensitive": false,
            "encrypt": false,
            "secretLevel": 0
        }
    ],
    "rules": [
        {
            "ruleType": 1,
            "rule": {
                "description": "testDescription",
                "fieldName": "model_col1"
            }
        }
    ],
    "sources": [
        {
            // postgresql
            "sourceType": 1,
            "dataSource": {
                "type": 1,
                "url": "jdbc:postgresql://124.222.140.214:5666/data_management?user=postgres&password=123qweasd",
                "tableName": "test_source",
                "tag": "testTag",
                "description": "testDescription",
                "fieldMap": {
                    "source_col1": "model_col1",
                    "source_col2": "model_col2!^@col21!^@col211",
                    "source_col3": "model_col3",
                    "source_col4": "model_col4"
                }
            }
        },
        {
            // mongodb
            "sourceType": 2,
            "dataSource": {
                "type": 2,
                "url": "mongodb://124.222.140.214:27088",
                "databaseName": "data_management",
                "collectionName": "test_mongodb_source",
                "tag": "testTag",
                "description": "testDescription",
                "fieldMap": {
                    "col1": "model_col1",
                    "col5!^@col53!^@col531": "model_col2!^@col21!^@col211",
                    "col2": "model_col3",
                    "col4": "model_col4"
                }
            }
        }
    ]
}
```
