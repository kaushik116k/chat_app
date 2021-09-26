package com.example.social.dashboard;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.social.Adapters.AdapterUsers;
import com.example.social.Authentication.LoginActivity;
import com.example.social.R;
import com.example.social.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUsers> usersList;
    FirebaseAuth firebaseAuth;
    public UsersFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users,container,false);
        recyclerView = view.findViewById(R.id.users_RecyclerView);

        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        usersList = new ArrayList<>();

        getAllUsers();
        return view;
    }

    private void getAllUsers() {

        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelUsers modelUsers  = ds.getValue(ModelUsers.class);

                    if (!modelUsers.getUid().equals(fUser.getUid()))
                    {
                        usersList.add(modelUsers);
                    }

                    adapterUsers = new AdapterUsers(getActivity(),usersList);
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchUsers(String query) {
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelUsers modelUsers  = ds.getValue(ModelUsers.class);

                    if (!modelUsers.getUid().equals(fUser.getUid()))
                    {
                        if (modelUsers.getName().toLowerCase().equals(query.toLowerCase()) ||
                        modelUsers.getEmail().toLowerCase().equals(query.toLowerCase()))
                        {
                            usersList.add(modelUsers);
                        }
                    }

                    adapterUsers = new AdapterUsers(getActivity(),usersList);
                    adapterUsers.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private  void checkUserStatus()
    {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null)
        {
            // profile.setText(user.getEmail());
        }
        else
        {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim()))
                {
                    searchUsers(query);
                }
                else {
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim()))
                {
                    searchUsers(newText);
                }
                else {
                    getAllUsers();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout)
        {
            firebaseAuth.signOut();
            Toast.makeText(getActivity(), "Successfully logged out", Toast.LENGTH_SHORT).show();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}