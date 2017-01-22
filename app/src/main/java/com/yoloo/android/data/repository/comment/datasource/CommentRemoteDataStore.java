package com.yoloo.android.data.repository.comment.datasource;

import com.yoloo.android.data.ApiManager;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.CommentRealm;
import io.reactivex.Observable;
import java.util.List;

public class CommentRemoteDataStore {

  private static CommentRemoteDataStore INSTANCE;

  private CommentRemoteDataStore() {
  }

  public static CommentRemoteDataStore getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new CommentRemoteDataStore();
    }
    return INSTANCE;
  }

  public Observable<CommentRealm> get(String commentId) {
    return ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.empty());
  }

  public Observable<CommentRealm> add(CommentRealm comment) {
    return ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.just(comment));
  }

  public void delete(String commentId) {
    ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.empty());
  }

  public Observable<Response<List<CommentRealm>>> list(String postId, String cursor, String eTag,
      int limit) {
    return ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.empty());
  }
}
