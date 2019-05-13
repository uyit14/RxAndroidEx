package com.rxandroidex.rxandroidexapp.mvp.main;

import com.rxandroidex.rxandroidexapp.app.BaseApplication;
import com.rxandroidex.rxandroidexapp.network.ApiClient;
import com.rxandroidex.rxandroidexapp.network.ApiService;
import com.rxandroidex.rxandroidexapp.network.model.Note;
import com.rxandroidex.rxandroidexapp.network.model.User;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class Presenter implements IView.Presenter{
    private final IView.View mNotesView;
    private ApiService apiService = ApiClient.getClient(BaseApplication.getInstance()).create(ApiService.class);;
    private CompositeDisposable disposable = new CompositeDisposable();

    public Presenter(IView.View mNotesView) {
        this.mNotesView = mNotesView;
    }

    @Override
    public void requetsGetNotes() {
        disposable.add(
                apiService.fetchAllNotes()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(new Function<List<Note>, List<Note>>() {
                            @Override
                            public List<Note> apply(List<Note> notes) {
                                // TODO - note about sort
                                Collections.sort(notes, new Comparator<Note>() {
                                    @Override
                                    public int compare(Note n1, Note n2) {
                                        return n2.getId() - n1.getId();
                                    }
                                });
                                return notes;
                            }
                        })
                        .subscribeWith(new DisposableSingleObserver<List<Note>>() {
                            @Override
                            public void onSuccess(List<Note> notes) {
                                mNotesView.getNotesSuccess(notes);
                            }

                            @Override
                            public void onError(Throwable e) {
                                mNotesView.getNoteFail(e);
                            }
                        })
        );
    }

    @Override
    public void requestRegisterUser() {
        // unique id to identify the device
        String uniqueId = UUID.randomUUID().toString();

        disposable.add(
                apiService
                        .register(uniqueId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<User>() {
                            @Override
                            public void onSuccess(User user) {
                                mNotesView.registerUserSuccess(user);
                            }

                            @Override
                            public void onError(Throwable e) {
                                mNotesView.registerUserFail(e);
                            }
                        }));
    }

    @Override
    public void requestCreateNote(String note) {
        disposable.add(
                apiService.createNote(note)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<Note>() {

                            @Override
                            public void onSuccess(Note note) {
                                mNotesView.createNoteSuccess(note);
                            }

                            @Override
                            public void onError(Throwable e) {
                                mNotesView.createNoteFail(e);
                            }
                        }));
    }

    @Override
    public void requestUpdateNote(final int noteId, final String note, final int position) {
        disposable.add(
                apiService.updateNote(noteId, note)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                mNotesView.updateNoteSuccess(noteId, note, position);
                            }

                            @Override
                            public void onError(Throwable e) {
                                mNotesView.updateNoteFail(e);
                            }
                        }));
    }

    @Override
    public void requestDeleteNote(final int noteId, final int position) {
        disposable.add(
                apiService.deleteNote(noteId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                mNotesView.deleteNoteSuccess(noteId, position);
                            }

                            @Override
                            public void onError(Throwable e) {
                                mNotesView.deleteNoteFail(e);
                            }
                        })
        );
    }
}
