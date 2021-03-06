package com.example.social.dashboard;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.social.Authentication.LoginActivity;
import com.example.social.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    StorageReference storageReference;
    String storage_path = "Users_profile_cover_Img";

    ImageView avatarIv,coverIv;
    TextView nameTv,emailTv,phoneTv;
    FloatingActionButton fab;

    ProgressDialog pd;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    String cameraPermissions[];
    String storagePermissions[];

    Uri image_uri;

    String profileOrCoverPhoto;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile,container,false);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        avatarIv = view.findViewById(R.id.avatarIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        coverIv = view.findViewById(R.id.coverIv);

        fab = view.findViewById(R.id.fab);
        pd = new ProgressDialog(getActivity());

        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren())
                {
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image1 = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();
                    String image2 = user.getPhotoUrl().toString();

                    if (name.equals(""))
                    {
                        name.equals("Mighty");
                        nameTv.setText("Mighty");
                    }
                    else
                    {
                        nameTv.setText(name);
                    }
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    try {
                        Picasso.get().load(image1).into(avatarIv);
                    }
                    catch (Exception e)
                    {
                        image2.equals(image1);
                        Picasso.get().load(image2).into(avatarIv);
                    }


                    try {
                        Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e)
                    {
                        Picasso.get().load(R.drawable.circular).into(coverIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        return view;
    }

    private boolean checkStoragePermissions()
    {
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result;
    }
    private void requestStoragePermission()
    {
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions()
    {
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }
    private void requestCameraPermission()
    {
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        String options[] = {"Edit Profile Picture", "Edit Cover Photo", "Edit Name", "Edit Phone"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Profile");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0)
                {
                    //Edit profile photo
                    pd.setMessage("Updating profile picture");
                    profileOrCoverPhoto = "image";
                    showImagePicDialog();
                }
                else if (which == 1)
                {
                    //Edit cover photo
                    pd.setMessage("Updating cover photo");
                    profileOrCoverPhoto = "cover";
                    showImagePicDialog();
                }
                else if (which == 2)
                {
                    //Edit name
                    pd.setMessage("Updating name");
                    showNamePhoneUpdateDialog("name");
                }
                else if (which == 3)
                {
                    //Edit phone
                    pd.setMessage("Updating phone");
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });
        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + key);

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = editText.getText().toString().trim();

                if(!TextUtils.isEmpty(value))
                {
                    pd.show();
                    HashMap<String,Object> result = new HashMap<>();
                    result.put(key,value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else
                {
                    Toast.makeText(getActivity(), "Please Enter " + key, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    private void showImagePicDialog() {
        String options[] = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick photo from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0)
                {
                    //Camera clicked
                    if(!checkCameraPermissions()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                else if (which == 1)
                {
                    //Gallery clicked
                    if (!checkStoragePermissions())
                    {
                        requestStoragePermission();
                    }
                    else
                        pickFromGallery();
                }
            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case CAMERA_REQUEST_CODE :
            {
                if (grantResults.length > 0)
                {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && writeStorageAccepted)
                    {
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(getActivity(), "Please enable camera and storage permissions", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE :
            {
                if (grantResults.length > 0)
                {
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (writeStorageAccepted)
                    {
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(getActivity(), "Please enable storage permissions", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK)
        {
            if (requestCode == IMAGE_PICK_CAMERA_CODE)
            {
                uploadCoverProfilePhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_GALLERY_CODE)
            {
                image_uri = data.getData();
                uploadCoverProfilePhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadCoverProfilePhoto(Uri uri) {
        pd.show();
        String filePathAndName = storage_path + "" + profileOrCoverPhoto + " " + user.getUid();
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();
                        if (uriTask.isSuccessful())
                        {
                            HashMap<String, Object> results = new HashMap<>();
                            results.put(profileOrCoverPhoto,downloadUri.toString());
                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Image Updated", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error while updating image", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            }
                            else
                            {
                                pd.dismiss();
                                Toast.makeText(getActivity(), "Some error Occurred", Toast.LENGTH_SHORT).show();
                            }
                        }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pickFromGallery() {
        Intent galleyIntent = new Intent(Intent.ACTION_PICK);
        galleyIntent.setType("image/*");
        startActivityForResult(galleyIntent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");

        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
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