package no.hiof.larseknu.playingwithviewmodel.screens.selection;


import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import no.hiof.larseknu.playingwithviewmodel.R;
import no.hiof.larseknu.playingwithviewmodel.databinding.FragmentMovieSelectionBinding;


public class MovieSelectionFragment extends Fragment {
    private FragmentMovieSelectionBinding binding;
    private MovieSelectionViewModel viewModel;

    public MovieSelectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_movie_selection, container, false);

        // Create and attach an instance of the view model to the parent activity
        viewModel = new ViewModelProvider(requireActivity()).get(MovieSelectionViewModel.class);

        binding.setSelectionViewModel(viewModel);
        binding.setLifecycleOwner(this);

        viewModel.getEventSelectionDone().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean changed) {
                if (changed)
                    movieSelectionFinished();
            }
        });

        // Inflate the layout for this fragment
        return binding.getRoot();
    }


    private void movieSelectionFinished() {
        Toast.makeText(this.getActivity(), "Movie selection done", Toast.LENGTH_SHORT).show();

        NavHostFragment.findNavController(this).navigate(MovieSelectionFragmentDirections.actionMovieSelectionToSummary());

    }
}
