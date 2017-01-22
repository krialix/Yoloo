package com.yoloo.backend.question;

import com.google.common.base.Optional;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.gamification.Tracker;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.util.StringUtil;
import com.yoloo.backend.vote.Vote;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;

import static com.yoloo.backend.OfyService.factory;
import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "create")
public class QuestionService {

  @NonNull
  private QuestionShardService shardService;

  public QuestionModel create(Account account, String content, String tags, String categories,
      Integer bounty, Media media, Tracker tracker) {

    final Key<Question> questionKey = factory().allocateId(account.getKey(), Question.class);

    // Create list of new shard entities for given comment.
    List<QuestionCounterShard> shards = shardService.createShards(questionKey);
    // Create list of new shard refs.
    List<Ref<QuestionCounterShard>> shardRefs =
        ShardUtil.createRefs(shards).toList(shards.size()).blockingGet();

    Question question = Question.builder()
        .id(questionKey.getId())
        .parentUserKey(account.getKey())
        .avatarUrl(account.getAvatarUrl())
        .username(account.getUsername())
        .content(content)
        .shardRefs(shardRefs)
        .tags(StringUtil.splitToSet(tags, ","))
        .categories(StringUtil.splitToSet(categories, ","))
        .dir(Vote.Direction.DEFAULT)
        .bounty(tracker.getBounties() >= bounty ? bounty : 0)
        .acceptedCommentKey(null)
        .media(media)
        .comments(0)
        .votes(0)
        .reports(0)
        .commented(false)
        .created(DateTime.now())
        .build();

    return QuestionModel.builder().question(question).shards(shards).build();
  }

  public Question update(Question question, Optional<Media> media, Optional<String> content,
      Optional<Integer> bounty, Optional<String> tags) {
    if (bounty.isPresent()) {
      question = question.withBounty(bounty.get());
    }

    if (content.isPresent()) {
      question = question.withContent(content.get());
    }

    if (tags.isPresent()) {
      question = question.withTags(StringUtil.splitToSet(tags.get(), ","));
    }

    if (media.isPresent()) {
      question = question.withMedia(media.get());
    }

    return question;
  }

  public List<Key<Comment>> getCommentKeys(Key<Question> questionKey) {
    return ofy().load().type(Comment.class)
        .filter(Comment.FIELD_QUESTION_KEY + " =", questionKey)
        .keys().list();
  }

  public List<Key<Vote>> getVoteKeys(Key<Question> questionKey) {
    return ofy().load().type(Vote.class)
        .filter(Vote.FIELD_VOTABLE_KEY + " =", questionKey)
        .keys().list();
  }
}
