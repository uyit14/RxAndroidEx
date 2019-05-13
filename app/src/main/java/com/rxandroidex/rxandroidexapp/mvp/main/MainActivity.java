package com.rxandroidex.rxandroidexapp.mvp.main;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rxandroidex.rxandroidexapp.R;
import com.rxandroidex.rxandroidexapp.network.model.Note;
import com.rxandroidex.rxandroidexapp.network.model.User;
import com.rxandroidex.rxandroidexapp.app.utils.MyDividerItemDecoration;
import com.rxandroidex.rxandroidexapp.app.utils.PrefUtils;
import com.rxandroidex.rxandroidexapp.app.utils.RecyclerTouchListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.HttpException;

public class MainActivity extends AppCompatActivity implements IView.View {

    private static final String TAG = MainActivity.class.getSimpleName();

    private NotesAdapter mAdapter;
    private List<Note> notesList = new ArrayList<>();

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.txt_empty_notes_view)
    TextView noNotesView;

    Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //
        presenter = new Presenter(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.activity_title_home));
        setSupportActionBar(toolbar);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoteDialog(false, null, -1);
            }
        });

        // white background notification bar
        whiteNotificationBar(fab);


        mAdapter = new NotesAdapter(this, notesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        /**
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         * */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));

        /**
         * Check for stored Api Key in shared preferences
         * If not present, make api call to register the user
         * This will be executed when app is installed for the first time
         * or data is cleared from settings
         * */
        if (TextUtils.isEmpty(PrefUtils.getApiKey(this))) {
            presenter.requestRegisterUser();
        } else {
            presenter.requetsGetNotes();
        }
    }


    /**
     * Shows alert dialog with EditText options to enter / edit
     * a note.
     * when shouldUpdate=true, it automatically displays old note and changes the
     * button text to UPDATE
     */
    private void showNoteDialog(final boolean shouldUpdate, final Note note, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.note_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputNote = view.findViewById(R.id.note);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_note_title) : getString(R.string.lbl_edit_note_title));

        if (shouldUpdate && note != null) {
            inputNote.setText(note.getNote());
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show toast message when no text is entered
                if (TextUtils.isEmpty(inputNote.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Enter note!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                // check if user updating note
                if (shouldUpdate && note != null) {
                    // update note by it's id
                    presenter.requestUpdateNote(note.getId(), inputNote.getText().toString(), position);
                } else {
                    // create new note
                    presenter.requestCreateNote(inputNote.getText().toString().trim());
                }
            }
        });
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showNoteDialog(true, notesList.get(position), position);
                } else {
                    presenter.requestDeleteNote(notesList.get(position).getId(), position);
                }
            }
        });
        builder.show();
    }

    private void toggleEmptyNotes() {
        if (notesList.size() > 0) {
            noNotesView.setVisibility(View.GONE);
        } else {
            noNotesView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Showing a Snackbar with error message
     * The error body will be in json format
     * {"error": "Error message!"}
     */
    private void showError(Throwable e) {
        String message = "";
        try {
            if (e instanceof IOException) {
                message = "No internet connection!";
            } else if (e instanceof HttpException) {
                HttpException error = (HttpException) e;
                String errorBody = error.response().errorBody().string();
                JSONObject jObj = new JSONObject(errorBody);

                message = jObj.getString("error");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        if (TextUtils.isEmpty(message)) {
            message = "Unknown error occurred! Check LogCat.";
        }

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void registerUserSuccess(User user) {
        // Storing user API Key in preferences
        PrefUtils.storeApiKey(getApplicationContext(), user.getApiKey());
        Toast.makeText(getApplicationContext(),
                "Device is registered successfully! ApiKey: " + PrefUtils.getApiKey(getApplicationContext()),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void registerUserFail(Throwable e) {
        Log.e(TAG, "onError: " + e.getMessage());
        showError(e);
    }

    @Override
    public void getNotesSuccess(List<Note> notes) {
        notesList.clear();
        notesList.addAll(notes);
        mAdapter.notifyDataSetChanged();

        toggleEmptyNotes();
        Log.e(TAG, "onSuccess: " + "Fetch note success!");
    }

    @Override
    public void getNoteFail(Throwable e) {
        Log.e(TAG, "onError: " + e.getMessage());
        showError(e);
    }

    @Override
    public void createNoteSuccess(Note note) {
        if (!TextUtils.isEmpty(note.getError())) {
            Toast.makeText(getApplicationContext(), note.getError(), Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "new note created: " + note.getId() + ", " + note.getNote() + ", " + note.getTimestamp());

        // Add new item and notify adapter
        notesList.add(0, note);
        mAdapter.notifyItemInserted(0);

        toggleEmptyNotes();
    }

    @Override
    public void createNoteFail(Throwable e) {
        Log.e(TAG, "onError: " + e.getMessage());
        showError(e);
    }

    @Override
    public void updateNoteSuccess(int noteId, String note, int position) {
        Log.d(TAG, "Note updated!");

        Note n = notesList.get(position);
        n.setNote(note);

        // Update item and notify adapter
        notesList.set(position, n);
        mAdapter.notifyItemChanged(position);
    }

    @Override
    public void updateNoteFail(Throwable e) {
        Log.e(TAG, "onError: " + e.getMessage());
        showError(e);
    }

    @Override
    public void deleteNoteSuccess(int noteId, int position) {
        Log.d(TAG, "Note deleted! " + noteId);

        // Remove and notify adapter about item deletion
        notesList.remove(position);
        mAdapter.notifyItemRemoved(position);

        Toast.makeText(MainActivity.this, "Note deleted!", Toast.LENGTH_SHORT).show();

        toggleEmptyNotes();
    }

    @Override
    public void deleteNoteFail(Throwable e) {
        Log.e(TAG, "onError: " + e.getMessage());
        showError(e);
    }
}
