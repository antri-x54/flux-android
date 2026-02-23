package org.nikanikoo.flux.utils;

import androidx.recyclerview.widget.DiffUtil;

import org.nikanikoo.flux.data.models.Post;

import java.util.List;

public class PostDiffCallback extends DiffUtil.Callback {
    private final List<Post> oldList;
    private final List<Post> newList;

    public PostDiffCallback(List<Post> oldList, List<Post> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Post oldPost = oldList.get(oldItemPosition);
        Post newPost = newList.get(newItemPosition);

        return oldPost.getPostId() == newPost.getPostId() 
                && oldPost.getOwnerId() == newPost.getOwnerId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Post oldPost = oldList.get(oldItemPosition);
        Post newPost = newList.get(newItemPosition);

        boolean sameContent = (oldPost.getContent() == null && newPost.getContent() == null)
                || (oldPost.getContent() != null && oldPost.getContent().equals(newPost.getContent()));
        
        boolean sameLikeCount = oldPost.getLikeCount() == newPost.getLikeCount();
        boolean sameLiked = oldPost.isLiked() == newPost.isLiked();
        boolean sameCommentCount = oldPost.getCommentCount() == newPost.getCommentCount();

        return sameContent && sameLikeCount && sameLiked && sameCommentCount;
    }
}
