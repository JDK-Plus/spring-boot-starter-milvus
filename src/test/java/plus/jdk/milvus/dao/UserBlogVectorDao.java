package plus.jdk.milvus.dao;

import plus.jdk.milvus.annotation.VectorRepository;
import plus.jdk.milvus.collection.UserBlogVector;
import plus.jdk.milvus.record.VectorModelRepositoryImpl;

@VectorRepository
public class UserBlogVectorDao extends VectorModelRepositoryImpl<UserBlogVector> {
}