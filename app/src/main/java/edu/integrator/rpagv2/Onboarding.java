package edu.integrator.rpagv2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.integrator.rpagv2.R;

public class Onboarding extends AppCompatActivity {

    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        viewPager = findViewById(R.id.onboardingViewPager);

        FragmentAdapter fragmentAdapter =
                new FragmentAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fragmentAdapter);
    }

    class FragmentAdapter extends FragmentPagerAdapter {


        public FragmentAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        public FragmentAdapter(FragmentManager fragmentManager) {
            super(fragmentManager, FragmentAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new WelcomeFragment();
                case 1:
                    return new DescriptionFragment();
                default:
                    return new WelcomeFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public static class WelcomeFragment extends Fragment {
        View view;
        Button btnNext;
        ViewPager viewPager;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            view = inflater.inflate(R.layout.onboarding_welcome_fragment, container, false);
            init();
            return view;
        }

        void init() {
            viewPager = getActivity().findViewById(R.id.onboardingViewPager);
            btnNext = view.findViewById(R.id.btnOnboardingWelcomeNext);
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickNext();
                }
            });
        }

        public void onClickNext () {
            viewPager.setCurrentItem(1);
        }
    }

    public static class DescriptionFragment extends Fragment {
        View view;
        Button btnBack, btnNext;
        ViewPager viewPager;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            view = inflater.inflate(R.layout.onboarding_description_fragment, container, false);
            init();
            return view;
        }

        void init() {
            viewPager = getActivity().findViewById(R.id.onboardingViewPager);
            btnNext = view.findViewById(R.id.btnOnboardingDescriptionNext);
            btnBack = view.findViewById(R.id.btnOnboardingDescriptionBack);
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickNext();
                }
            });
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickBack();
                }
            });
        }

        public void onClickNext () {
            getActivity().finish();
        }
        public void onClickBack () {
            viewPager.setCurrentItem(0);
        }
    }


}
