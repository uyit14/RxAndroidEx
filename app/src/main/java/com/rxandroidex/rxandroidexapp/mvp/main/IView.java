package com.rxandroidex.rxandroidexapp.mvp.main;

import com.rxandroidex.rxandroidexapp.network.model.Note;
import com.rxandroidex.rxandroidexapp.network.model.User;

import java.util.List;

public interface IView {
    interface View{
        //register user
        void registerUserSuccess(User user);
        void registerUserFail(Throwable e);
        //get notes
        void getNotesSuccess(List<Note> notes);
        void getNoteFail(Throwable e);
        //create note
        void createNoteSuccess(Note note);
        void createNoteFail(Throwable e);
        //update note
        void updateNoteSuccess(int noteId, final String note, final int position);
        void updateNoteFail(Throwable e);
        //delete note
        void deleteNoteSuccess(final int noteId, final int position);
        void deleteNoteFail(Throwable e);
    }
    interface Presenter{
        void requestGetNotes();
        void requestRegisterUser();
        void requestCreateNote(String note);
        void requestUpdateNote(int noteId, final String note, final int position);
        void requestDeleteNote(final int noteId, final int position);
    }
}
