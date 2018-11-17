/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.seta.tollroaddroid.app.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.seta.tollroaddroid.app.R;
import com.seta.tollroaddroid.app.api.Resource;

/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 */
public class FingerprintAuthenticationDialogFragment extends DialogFragment {

    private Button mCancelButton;

    private String title;
    private String description;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
        
        Bundle bundle = getArguments();
        if(bundle != null)
        {
            title = bundle.getString(Resource.KEY_TITLE);
            description = bundle.getString(Resource.KEY_DESCRIPTION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.fingerprint_for_tca));
        View v = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);
        mCancelButton = (Button) v.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if(listener != null)
                {
                    listener.exit();
                }
            }
        });
        
        Bundle bundle = getArguments();
        if(bundle != null)
        {
            title = bundle.getString(Resource.KEY_TITLE);
            description = bundle.getString(Resource.KEY_DESCRIPTION);
        }

        if(description != null && !description.isEmpty())
        {
            TextView tvDescription = (TextView)v.findViewById(R.id.fingerprint_description);
            tvDescription.setText(description);
        }

        if(title != null && !title.isEmpty())
        {
            getDialog().setTitle(title);
        }


        getDialog().setCancelable(false);
        return v;
    }

    public interface MyExitListener{
        public void exit();
    }

    private MyExitListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (MyExitListener) context;
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
            castException.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

}
