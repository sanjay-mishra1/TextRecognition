package com.example.textrecognition.app;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.textrecognition.R;
import com.example.textrecognition.java.ChooserActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class FormattedTextViewerFragment extends BottomSheetDialogFragment {
    ArrayList<String> textList;
    private StringBuilder formattedText;

    public FormattedTextViewerFragment(ArrayList<String> textList) {
        this.textList = textList;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_text_viewer,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView textView=view.findViewById(R.id.textSelected);
        formattedText=formatText();
        textView.setText(formattedText);
        view.findViewById(R.id.closeBt).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.copyBt).setOnClickListener(v -> {
           try {
               if (getActivity()!=null)
               ((AllTextViewerActivity) getActivity()).copyToClipboard(formattedText.toString());
               else
                   Toast.makeText(getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
           }catch (Exception e){
               Toast.makeText(getContext(),"Error occurred",Toast.LENGTH_SHORT).show();
           }
        });
        view.findViewById(R.id.sendBt).setOnClickListener(v -> {
            try {
                if (getActivity()!=null)
                ((AllTextViewerActivity) getActivity()).sendToDb(formattedText.toString());
                else Toast.makeText(getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
            }catch (Exception e) {
                Toast.makeText(getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private StringBuilder formatText() {
        return ChooserActivity.formattedText;
//        StringBuilder text= new StringBuilder();
//        for (String sentence:textList)
//            text.append(sentence).append("\n");
//        return text;
    }


    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog=(BottomSheetDialog)super.onCreateDialog(savedInstanceState);
        bottomSheetDialog.setOnShowListener(dia -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dia;
            FrameLayout bottomSheet =  dialog .findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet!=null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(false);
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return bottomSheetDialog;
    }

}

