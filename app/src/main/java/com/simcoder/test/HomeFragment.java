package com.simcoder.test;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.special.ResideMenu.ResideMenu;


public class HomeFragment extends Fragment {

    private View parentView;
    private Button sos,triggerPhrase,receiveSos,tutorial;
    private ResideMenu resideMenu;
    private MainActivity2 parentActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.home, container, false);
        sos= (Button) parentView.findViewById(R.id.sos);
        triggerPhrase= (Button) parentView.findViewById(R.id.triggerPhrase);
        receiveSos= (Button) parentView.findViewById(R.id.receiveSos);
        tutorial= (Button) parentView.findViewById(R.id.btn_open_menu);
        parentActivity = (MainActivity2) getActivity();
        resideMenu = parentActivity.getResideMenu();
        FrameLayout ignored_view = (FrameLayout) parentView.findViewById(R.id.ignored_view);
        resideMenu.addIgnoredView(ignored_view);
        parentView.findViewById(R.id.btn_open_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
        receiveSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReceiveSosFragment fragment = new ReceiveSosFragment();
                resideMenu.clearIgnoredViewList();
                parentActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_fragment, fragment, "fragment")
                        .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .addToBackStack(null)
                        .commit();
                return;
            }});
        sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationFragment fragment = new LocationFragment();
                Bundle arguments = new Bundle();
                arguments.putInt( "choice" , 1);
                fragment.setArguments(arguments);
                resideMenu.clearIgnoredViewList();
                parentActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_fragment, fragment, "fragment")
                        .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .addToBackStack(null)
                        .commit();
                return;
            }});
        triggerPhrase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TriggerPhraseFragment fragment = new TriggerPhraseFragment();
                resideMenu.clearIgnoredViewList();
                parentActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_fragment, fragment, "fragment")
                        .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .addToBackStack(null)
                        .commit();
                return;
            }});
        tutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
                SharedPreferences ref = getActivity().getSharedPreferences("IntroSliderApp", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = ref.edit();
                editor.putBoolean("FirstTimeStartFlag", true);
                editor.commit();
                Intent intent = new Intent(getActivity(), WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        return parentView;
    }
}
