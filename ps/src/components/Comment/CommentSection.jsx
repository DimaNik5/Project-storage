import React, { useState, useEffect } from 'react';
import { fileApi } from '../../api/fileApi';
import './Comment.css';

const CommentSection = ({ repoId, userId }) => {
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadComments();
  }, [repoId]);

  const loadComments = async () => {
    try {
      setLoading(true);
      const commentsData = await fileApi.getRepositoryComments(repoId);
      setComments(commentsData);
    } catch (error) {
      console.error('Error loading comments:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async () => {
    if (!newComment.trim() || !userId) return;

    try {
      setSubmitting(true);
      await fileApi.addComment(repoId, userId, newComment);
      setNewComment('');
      loadComments();
    } catch (error) {
      alert('Error adding comment');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="loading">Loading comments...</div>;

  return (
    <div className="comment-section">
      <h3>Comments ({comments.length})</h3>
      
      {userId ? (
        <div className="add-comment">
          <textarea
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            placeholder="Write a comment..."
            rows={3}
          />
          <button 
            onClick={handleSubmit} 
            disabled={!newComment.trim() || submitting}
          >
            {submitting ? 'Posting...' : 'Comment'}
          </button>
        </div>
      ) : (
        <p className="login-prompt">Please login to comment</p>
      )}

      <div className="comments-list">
        {comments.length === 0 ? (
          <p className="no-comments">No comments yet</p>
        ) : (
          comments.map(comment => (
            <div key={comment.id} className="comment">
              <div className="comment-header">
                <strong>{comment.username}</strong>
                <span>{new Date(comment.createdAt).toLocaleString()}</span>
              </div>
              <p>{comment.content}</p>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default CommentSection;