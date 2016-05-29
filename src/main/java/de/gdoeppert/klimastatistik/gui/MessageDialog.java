package de.gdoeppert.klimastatistik.gui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.gdoeppert.klimastatistik.R;


// ...

public class MessageDialog extends DialogFragment {

    private TextView messageView;
    private String message = null;
    private String title = null;

    public MessageDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.messagedialog, container);
        messageView = (TextView) view.findViewById(R.id.message);
        if (message != null) {
            messageView.setText(message);
        }
        getDialog().setTitle(title);

        return view;
    }

    public void setMessage(String tt, String msg) {
        title = tt;
        message = msg;
        if (messageView != null) {
            messageView.setText(msg);
        }
    }
}
