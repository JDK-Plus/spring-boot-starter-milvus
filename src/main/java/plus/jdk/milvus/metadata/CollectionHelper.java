package plus.jdk.milvus.metadata;

import lombok.extern.slf4j.Slf4j;
import plus.jdk.milvus.annotation.VectorCollectionColumn;
import plus.jdk.milvus.annotation.VectorCollectionName;
import plus.jdk.milvus.config.GlobalConfig;
import plus.jdk.milvus.global.SimpleTypeRegistry;
import plus.jdk.milvus.global.VectorTypeHandler;
import plus.jdk.milvus.global.handler.AnnotationHandler;
import plus.jdk.milvus.global.handler.PostInitCollectionInfoHandler;
import plus.jdk.milvus.selector.MilvusSelector;
import plus.jdk.milvus.toolkit.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

/**
 * <p>
 * 实体类反射表辅助类
 * </p>
 */
@Slf4j
public class CollectionHelper {

    /**
     * 储存反射类表信息
     */
    private static final Map<Class<?>, CollectionDefinition> COLLECTION_INFO_CACHE = new ConcurrentHashMap<>();

    /**
     * 储存表名对应的反射类表信息
     */
    private static final Map<String, CollectionDefinition> COLLECTION_NAME_INFO_CACHE = new ConcurrentHashMap<>();


    /**
     * 默认表主键名称
     */
    private static final String DEFAULT_ID_NAME = "id";

    /**
     * <p>
     * 获取实体映射表信息
     * </p>
     *
     * @param clazz 反射实体类
     * @return 数据库表反射信息
     */
    public static CollectionDefinition getCollectionInfo(Class<?> clazz) {
        if (clazz == null || clazz.isPrimitive() || SimpleTypeRegistry.isSimpleType(clazz) || clazz.isInterface()) {
            return null;
        }
        Class<?> targetClass = ClassUtils.getUserClass(clazz);
        CollectionDefinition definition = COLLECTION_INFO_CACHE.get(targetClass);
        if (null != definition) {
            return definition;
        }
        definition = initCollectionInfo(targetClass);

        COLLECTION_INFO_CACHE.put(targetClass, definition);
        return definition;
    }

    /**
     * <p>
     * 根据表名获取实体映射表信息
     * </p>
     *
     * @param collectionName 表名
     * @return 数据库表反射信息
     */
    public static CollectionDefinition getCollectionInfo(String collectionName) {
        if (StringUtils.isBlank(collectionName)) {
            return null;
        }
        return COLLECTION_NAME_INFO_CACHE.get(collectionName);
    }


    /**
     * <p>
     * 实体类反射获取表信息【初始化】
     * </p>
     *
     * @param clazz 反射实体类
     * @return 数据库表反射信息
     */
    public static synchronized CollectionDefinition initCollectionInfo(Class<?> clazz) {
        CollectionDefinition targetCollectionInfo = COLLECTION_INFO_CACHE.get(clazz);
        if (targetCollectionInfo != null && (clazz.equals(targetCollectionInfo.getClazz()))) {
            return targetCollectionInfo;
        }
        // 不是同一个 Configuration,进行重新初始化
        GlobalConfig globalConfig = GlobalConfigUtils.getGlobalConfig(clazz);
        PostInitCollectionInfoHandler postInitCollectionInfoHandler = globalConfig.getPostInitCollectionInfoHandler();
        /* 没有获取到缓存信息,则初始化 */
        CollectionDefinition definition = postInitCollectionInfoHandler.creteCollectionInfo(clazz);

        /* 初始化表名相关 */
        initCollectionName(clazz, globalConfig, definition);
        /* 初始化字段相关 */
        initCollectionFields(clazz, globalConfig, definition);

        /* 自动构建 resultMap */
        postInitCollectionInfoHandler.postCollectionInfo(definition);
        COLLECTION_INFO_CACHE.put(clazz, definition);
        COLLECTION_NAME_INFO_CACHE.put(definition.getName(), definition);

        /* 缓存 lambda */
        LambdaUtils.installCache(definition);
        return definition;
    }


    /**
     * <p>
     * 初始化 表数据库类型,表名,resultMap
     * </p>
     *
     * @param clazz          实体类
     * @param globalConfig   全局配置
     * @param collectionInfo 数据库表反射信息
     */
    private static void initCollectionName(Class<?> clazz, GlobalConfig globalConfig, CollectionDefinition collectionInfo) {
        /* 数据库全局配置 */
        GlobalConfig.MilvusConfig dbConfig = globalConfig.getMilvusConfig();
        AnnotationHandler annotationHandler = globalConfig.getAnnotationHandler();
        VectorCollectionName vectorCollectionName = annotationHandler.getAnnotation(clazz, VectorCollectionName.class);
        if (vectorCollectionName == null) {
            throw ExceptionUtils.mpe("Missing @VectorCollectionName Annotation In Class: \"%s\".", clazz.getName());
        }

        String collectionName = clazz.getSimpleName();
        String collectionPrefix = dbConfig.getCollectionPrefix();

        if (StringUtils.isNotBlank(vectorCollectionName.name())) {
            collectionName = vectorCollectionName.name();
        } else {
            collectionName = initCollectionNameWithDbConfig(collectionName, dbConfig);
        }

        // 表追加前缀
        String targetCollectionName = collectionName;
        if (StringUtils.isNotBlank(collectionPrefix)) {
            targetCollectionName = collectionPrefix + targetCollectionName;
        }

        // 表格式化
        String collectionFormat = dbConfig.getCollectionFormat();
        if (StringUtils.isNotBlank(collectionFormat)) {
            targetCollectionName = String.format(collectionFormat, targetCollectionName);
        }


        collectionInfo.setEntityType(clazz);
        collectionInfo.setDescription(vectorCollectionName.description());
        collectionInfo.setDatabase(vectorCollectionName.database());
        collectionInfo.setName(targetCollectionName);
    }


    /**
     * <p>
     * 初始化 表主键,表字段
     * </p>
     *
     * @param clazz                实体类
     * @param globalConfig         全局配置
     * @param collectionDefinition 数据库表反射信息
     */
    private static void initCollectionFields(Class<?> clazz, GlobalConfig globalConfig, CollectionDefinition collectionDefinition) {
        AnnotationHandler annotationHandler = globalConfig.getAnnotationHandler();
        PostInitCollectionInfoHandler postInitCollectionInfoHandler = globalConfig.getPostInitCollectionInfoHandler();
        List<Field> list = getAllFields(clazz, annotationHandler);
        // 标记是否读取到主键
        boolean isReadPK = false;
        // 是否存在 @TableId 注解
//        boolean existTableId = isExistTableId(list, annotationHandler);
        // 是否存在 @TableLogic 注解
//        boolean existTableLogic = isExistTableLogic(list, annotationHandler);
        boolean existTableLogic = false;

        List<ColumnDefinition> columnsList = new ArrayList<>(list.size());
        for (Field field : list) {
//            if (excludeProperty.contains(field.getName())) {
//                continue;
//            }

//            boolean isPK = false;
            /* 主键ID 初始化 */
//            if (existTableId) {
//                TableId tableId = annotationHandler.getAnnotation(field, TableId.class);
//                if (tableId != null) {
//                    if (isReadPK) {
//                        throw ExceptionUtils.mpe("@TableId can't more than one in Class: \"%s\".", clazz.getName());
//                    }
//
//                    initTableIdWithAnnotation(globalConfig, collectionDefinition, field, tableId);
//                    isPK = isReadPK = true;
//                }
//            } else if (!isReadPK) {
//                isPK = isReadPK = initTableIdWithoutAnnotation(globalConfig, collectionDefinition, field);
//            }

//            if (isPK) {
//                continue;
//            }
            final VectorCollectionColumn collectionColumn = annotationHandler.getAnnotation(field, VectorCollectionColumn.class);

            /* 有 @TableField 注解的字段初始化 */
            if (collectionColumn != null) {
                VectorTypeHandler<?> typeHandler = MilvusSelector.applicationContext.getBean(collectionColumn.embeddingTypeHandler());
                ColumnDefinition columnDefinition = new ColumnDefinition(globalConfig, collectionDefinition, field, collectionColumn, typeHandler, existTableLogic);
                columnsList.add(columnDefinition);
                postInitCollectionInfoHandler.postFieldInfo(columnDefinition);
            }
        }

        /* 字段列表 */
        collectionDefinition.setColumns(columnsList);

        /* 未发现主键注解，提示警告信息 */
//        if (!isReadPK) {
//            log.warn(String.format("Can not find table primary key in Class: \"%s\".", clazz.getName()));
//        }
    }

    /**
     * 根据 DbConfig 初始化 表名
     *
     * @param className 类名
     * @param dbConfig  DbConfig
     * @return 表名
     */
    private static String initCollectionNameWithDbConfig(String className, GlobalConfig.MilvusConfig dbConfig) {
        String collectionName = className;
        // 开启表名下划线申明
        if (dbConfig.isTableUnderline()) {
            collectionName = StringUtils.camelToUnderline(collectionName);
        }
        // 大写命名判断
        if (dbConfig.isCapitalMode()) {
            collectionName = collectionName.toUpperCase();
        } else {
            // 首字母小写
            collectionName = StringUtils.firstToLowerCase(collectionName);
        }
        return collectionName;
    }

    /**
     * <p>
     * 获取该类的所有属性列表
     * </p>
     *
     * @param clazz             反射类
     * @param annotationHandler 注解处理类
     * @return 属性集合
     */
    public static List<Field> getAllFields(Class<?> clazz, AnnotationHandler annotationHandler) {
        List<Field> fieldList = ReflectionKit.getFieldList(ClassUtils.getUserClass(clazz));
        return fieldList.stream()
                /* 过滤没有注解的非表字段属性 */
                .filter(field -> annotationHandler.getAnnotation(field, VectorCollectionColumn.class) != null)
                .collect(toList());
    }
}
