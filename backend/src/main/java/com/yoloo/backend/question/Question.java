package com.yoloo.backend.question;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Link;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfNotDefault;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.feed.FeedItem;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.media.size.LargeSize;
import com.yoloo.backend.media.size.LowSize;
import com.yoloo.backend.media.size.MediumSize;
import com.yoloo.backend.media.size.MiniSize;
import com.yoloo.backend.media.size.ThumbSize;
import com.yoloo.backend.util.Deref;
import com.yoloo.backend.vote.Vote;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;
import org.joda.time.DateTime;

@Entity
@Cache
@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Question implements FeedItem {

  public static final String FIELD_CREATED = "created";
  public static final String FIELD_TAGS = "tags";
  public static final String FIELD_CATEGORIES = "categories";
  public static final String FIELD_COMMENTED = "commented";
  public static final String FIELD_RANK = "rank";
  public static final String FIELD_BOUNTY = "bounty";

  @Id
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private long id;

  @Parent
  @NonFinal
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Key<Account> parentUserKey;

  @Wither
  private Link avatarUrl;

  @Wither
  private String username;

  @Wither
  private String content;

  @Load(ShardGroup.class)
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private List<Ref<QuestionCounterShard>> shardRefs;

  @Singular
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Set<Key<Account>> reportedByKeys;

  @Index
  @Wither
  @NonFinal
  private Set<String> tags;

  @Index
  @Wither
  @NonFinal
  private Set<String> categories;

  @Wither
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Key<Comment> acceptedCommentKey;

  /**
   * The bounty value for the question. Bounty list is given below. 10, 20, 30, 40, 50
   */
  @Index
  @Wither
  private int bounty;

  /**
   * If a user questions a comment for given post then commented is true otherwise false.
   */
  @Index(value = IfNotDefault.class)
  @Wither
  @NonFinal
  private boolean commented;

  @Index(value = IfNotDefault.class)
  @Wither
  @NonFinal
  private double rank;

  @Wither
  private Media media;

  @Index
  @NonFinal
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private DateTime created;

  // Extra fields

  @Wither
  @Ignore
  private Vote.Direction dir;

  @Wither
  @Ignore
  private long votes;

  @Wither
  @Ignore
  private long comments;

  @Wither
  @Ignore
  private int reports;

  // Methods

  @ApiResourceProperty(name = "id")
  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  @ApiResourceProperty(name = "ownerId")
  public String getWebsafeOwnerId() {
    return this.parentUserKey.toWebSafeString();
  }

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public Key<Question> getKey() {
    return Key.create(parentUserKey, getClass(), id);
  }

  public String getAcceptedCommentId() {
    return acceptedCommentKey.toWebSafeString();
  }

  @Override
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public <T> Key<T> getVotableKey() {
    //noinspection unchecked
    return (Key<T>) getKey();
  }

  @Override
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public <E> List<E> getShards() {
    //noinspection unchecked
    return (List<E>) Deref.deref(this.shardRefs);
  }

  public Media getMedia() {
    if (media != null) {
      String mediaUrl = media.getUrl();

      ImmutableList<Media.Size> sizes =
          ImmutableList.<Media.Size>builder().add(new ThumbSize(mediaUrl))
              .add(new MiniSize(mediaUrl))
              .add(new LowSize(mediaUrl))
              .add(new MediumSize(mediaUrl))
              .add(new LargeSize(mediaUrl))
              .build();

      return this.media.withSizes(sizes);
    }
    return null;
  }

  @ApiResourceProperty(name = "created")
  public Date getDate() {
    return created.toDate();
  }

  @NoArgsConstructor
  public static class ShardGroup {
  }
}