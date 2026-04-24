package com.example.diabetesassistant.data.repository

import com.example.diabetesassistant.data.local.dao.CommentDao
import com.example.diabetesassistant.data.local.dao.CommunityPostDao
import com.example.diabetesassistant.data.local.dao.FavoriteDao
import com.example.diabetesassistant.data.local.entity.Comment
import com.example.diabetesassistant.data.local.entity.CommunityPost
import com.example.diabetesassistant.data.local.entity.Favorite
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 社区仓库接口。
 */
interface CommunityRepository {
    /**
     * 发布帖子。
     */
    suspend fun createPost(post: CommunityPost): Long

    /**
     * 更新帖子。
     */
    suspend fun updatePost(post: CommunityPost)

    /**
     * 删除帖子。
     */
    suspend fun deletePost(post: CommunityPost)

    /**
     * 查询帖子详情。
     */
    suspend fun getPostById(id: Long): CommunityPost?

    /**
     * 观察帖子流。
     */
    fun observePosts(): Flow<List<CommunityPost>>

    /**
     * 新增评论。
     */
    suspend fun addComment(comment: Comment): Long

    /**
     * 删除评论。
     */
    suspend fun deleteComment(comment: Comment)

    /**
     * 观察帖子评论。
     */
    fun observeComments(postId: Long): Flow<List<Comment>>

    /**
     * 添加收藏。
     */
    suspend fun addFavorite(userId: Long, postId: Long)

    /**
     * 移除收藏。
     */
    suspend fun removeFavorite(userId: Long, postId: Long)

    /**
     * 观察是否已收藏。
     */
    fun observeIsFavorite(userId: Long, postId: Long): Flow<Boolean>
}

/**
 * 社区仓库实现。
 */
@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val communityPostDao: CommunityPostDao,
    private val commentDao: CommentDao,
    private val favoriteDao: FavoriteDao,
) : CommunityRepository {
    /**
     * @see CommunityRepository.createPost
     */
    override suspend fun createPost(post: CommunityPost): Long = communityPostDao.insert(post)

    /**
     * @see CommunityRepository.updatePost
     */
    override suspend fun updatePost(post: CommunityPost) {
        communityPostDao.update(post)
    }

    /**
     * @see CommunityRepository.deletePost
     */
    override suspend fun deletePost(post: CommunityPost) {
        communityPostDao.delete(post)
    }

    /**
     * @see CommunityRepository.getPostById
     */
    override suspend fun getPostById(id: Long): CommunityPost? = communityPostDao.getById(id)

    /**
     * @see CommunityRepository.observePosts
     */
    override fun observePosts(): Flow<List<CommunityPost>> = communityPostDao.observeAll()

    /**
     * @see CommunityRepository.addComment
     */
    override suspend fun addComment(comment: Comment): Long = commentDao.insert(comment)

    /**
     * @see CommunityRepository.deleteComment
     */
    override suspend fun deleteComment(comment: Comment) {
        commentDao.delete(comment)
    }

    /**
     * @see CommunityRepository.observeComments
     */
    override fun observeComments(postId: Long): Flow<List<Comment>> = commentDao.observeByPostId(postId)

    /**
     * @see CommunityRepository.addFavorite
     */
    override suspend fun addFavorite(userId: Long, postId: Long) {
        favoriteDao.insert(Favorite(userId = userId, postId = postId))
    }

    /**
     * @see CommunityRepository.removeFavorite
     */
    override suspend fun removeFavorite(userId: Long, postId: Long) {
        favoriteDao.delete(userId = userId, postId = postId)
    }

    /**
     * @see CommunityRepository.observeIsFavorite
     */
    override fun observeIsFavorite(userId: Long, postId: Long): Flow<Boolean> {
        return favoriteDao.observeIsFavorite(userId = userId, postId = postId)
    }
}
