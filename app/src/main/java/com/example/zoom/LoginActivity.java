package com.example.zoom;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.example.zoom.call.services.LoginService;
import com.example.zoom.call.utils.Constants;
import com.example.zoom.call.utils.Consts;
import com.example.zoom.call.utils.QBEntityCallbackImpl;
import com.example.zoom.call.utils.SharedPrefsHelper;
import com.example.zoom.call.utils.ToastUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 100;
    private EditText editTextEmail, editTextPassword, editTextMobileNumber, editTextOTP;
    private TextView textViewCancel, textViewTitle, textViewForgotPassword;
    private AppCompatButton buttonSignIn, buttonSignInWithGoogle, buttonSignInWithFB;
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private FirebaseAuth mAuth;
    private final String TAG = LoginActivity.class.getSimpleName();
    private GoogleSignInClient mGoogleSignInClient;
    private boolean isSignIn;
    private QBUser userForSave;
    private String name="";
    private String userId= "";
    private Button btnSendOTP, btnValidate;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthCredential credential;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        isSignIn = getIntent().getBooleanExtra("isSignIn", false);
        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
        // [END phone_auth_callbacks]

        initViews();
        clickListener();
        configureGoogleSignIn();

        if(!isSignIn) {
            textViewTitle.setText("Sign Up");
            buttonSignIn.setText("Sign Up");
        }
    }

    private void initViews() {

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        textViewCancel = findViewById(R.id.textViewCancel);
        textViewTitle = findViewById(R.id.textViewTitle);
        buttonSignIn = findViewById(R.id.btnSignIn);
        buttonSignInWithGoogle = findViewById(R.id.btnLoginWithGoogle);
        buttonSignInWithFB = findViewById(R.id.btnLoginWithFaceBook);
        loginButton =  findViewById(R.id.login_button);
        textViewForgotPassword =  findViewById(R.id.textViewForgotPassword);
        editTextMobileNumber =  findViewById(R.id.editTextMobileNumber);
        btnSendOTP =  findViewById(R.id.btnSendOTP);
        btnValidate =  findViewById(R.id.btnValidate);
        editTextOTP =  findViewById(R.id.editTextOTP);
    }

    private void clickListener() {
        textViewCancel.setOnClickListener(this);
        buttonSignIn.setOnClickListener(this);
        buttonSignInWithGoogle.setOnClickListener(this);
        buttonSignInWithFB.setOnClickListener(this);
        textViewForgotPassword.setOnClickListener(this);
        btnSendOTP.setOnClickListener(this);
        btnValidate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textViewCancel :
                finish();
                break;
            case R.id.btnSignIn :

                if (editTextEmail.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Please enter email ", Toast.LENGTH_SHORT).show();
                }
                else if (editTextPassword.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
                }
                else if (!isValidEmail(editTextEmail.getText().toString())) {
                    Toast.makeText(this, "Please enter correct email", Toast.LENGTH_SHORT).show();
                }
                else if (editTextPassword.getText().toString().length()<6) {
                    Toast.makeText(this, "Please use strong password", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(isSignIn) {
                        emailSignIn();
                    }
                    else
                        emailSignUp();
                }

                break;
            case R.id.btnLoginWithGoogle :
                googleSignIn();
                break;
            case R.id.btnLoginWithFaceBook :
                facebookLogin();
                break;
            case R.id.btnSendOTP :
                if (editTextMobileNumber.getText().toString().length()<10) {
                    Toast.makeText(this, "Please enter valid mobile number ", Toast.LENGTH_SHORT).show();
                }
                else {
                    phoneNumberLogin();
                    btnValidate.setEnabled(true);
                }
                break;
            case R.id.btnValidate:
                if (editTextOTP.getText().toString().length()<6)
                    Toast.makeText(this, "Enter valid OTP!", Toast.LENGTH_SHORT).show();
                else if(mVerificationId!=null) {
                    credential = PhoneAuthProvider.getCredential(mVerificationId, editTextOTP.getText().toString());
                    signInWithPhoneAuthCredential(credential);
                }
                break;
            case R.id.textViewForgotPassword :
                startActivity(new Intent(this, ForgotPasswordActivity.class));
                finish();
                break;
        }
    }

    private void phoneNumberLogin() {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91" + editTextMobileNumber.getText().toString())       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();

                            if (user.getEmail() != null)
                                userId = user.getEmail();
                            else if (user.getProviderData().get(0).getEmail() != null)
                                userId = user.getProviderData().get(0).getEmail();
                            else if (user.getProviderData().get(1).getEmail() != null)
                                userId = user.getProviderData().get(1).getEmail();
                            else
                                userId = editTextMobileNumber.getText().toString() + "@gmail.com";

                            sharedPrefsHelper.save(Constants.USER_EMAIL, userId);

                            String name_array[] = userId.split("@");
                            name = name_array[0];


                            userForSave = createUserWithEnteredData();
                            startSignUpNewUser(userForSave);
                            // Update UI
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Invalid Verification Coder entered", Toast.LENGTH_SHORT).show();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }
    private void facebookLogin() {

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        loginButton.setReadPermissions("email", "public_profile");

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user.getEmail() != null)
                                userId = user.getEmail();
                            else if (user.getProviderData().get(0).getEmail() != null)
                                userId = user.getProviderData().get(0).getEmail();
                            else if (user.getProviderData().get(1).getEmail() != null)
                                userId = user.getProviderData().get(1).getEmail();

                            sharedPrefsHelper.save(Constants.USER_EMAIL, userId);

                            String name_array[] = userId.split("@");
                            name = name_array[0];

                            userForSave = createUserWithEnteredData();
                            startSignUpNewUser(userForSave);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
        else if (resultCode == Consts.EXTRA_LOGIN_RESULT_CODE) {
            hideProgressDialog();
            boolean isLoginSuccess = data.getBooleanExtra(Consts.EXTRA_LOGIN_RESULT, false);
            String errorMessage = data.getStringExtra(Consts.EXTRA_LOGIN_ERROR_MESSAGE);

            if (isLoginSuccess) {
                saveUserData(userForSave);
                signInCreatedUser(userForSave);
            } else {
                ToastUtils.longToast(getString(R.string.login_chat_login_error) + errorMessage);
             }
        }
        else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d("userNameL", user.getEmail() + user.getProviderData().get(0).getEmail() + " --- " + user.getProviderData().get(1).getEmail());

                            if (user.getEmail() != null)
                                userId = user.getEmail();
                            else if (user.getProviderData().get(0).getEmail() != null)
                                userId = user.getProviderData().get(0).getEmail();
                            else if (user.getProviderData().get(1).getEmail() != null)
                                userId = user.getProviderData().get(1).getEmail();

                            sharedPrefsHelper.save(Constants.USER_EMAIL, userId);

                            String name_array[] = userId.split("@");
                            name = name_array[0];

                            userForSave = createUserWithEnteredData();
                            startSignUpNewUser(userForSave);
                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void emailSignUp() {
        mAuth.createUserWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            userId = user.getEmail();
                            sharedPrefsHelper.save(Constants.USER_EMAIL, userId);


                            String name_array[] = userId.split("@");
                            name = name_array[0];

                            userForSave = createUserWithEnteredData();
                            startSignUpNewUser(userForSave);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void emailSignIn() {
        mAuth.signInWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user.getEmail() != null)
                                userId = user.getEmail();
                            else if (user.getProviderData().get(0).getEmail() != null)
                                userId = user.getProviderData().get(0).getEmail();
                            else if (user.getProviderData().get(1).getEmail() != null)
                                userId = user.getProviderData().get(1).getEmail();

                            sharedPrefsHelper.save(Constants.USER_EMAIL, userId);

                            String name_array[] = userId.split("@");
                            name = name_array[0];

                            userForSave = createUserWithEnteredData();
                            startSignUpNewUser(userForSave);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient =GoogleSignIn.getClient(this, gso);
    }
    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public static boolean isValidEmail(String target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private void startLoginService(QBUser qbUser) {
        Intent tempIntent = new Intent(this, LoginService.class);
        PendingIntent pendingIntent = createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        LoginService.start(this, qbUser, pendingIntent);
    }
    private void signInCreatedUser(final QBUser qbUser) {
        Log.d(TAG, "SignIn Started");
        requestExecutor.signInUser(qbUser, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle params) {
                Log.d(TAG, "SignIn Successful");
                sharedPrefsHelper.saveQbUser(userForSave);
                updateUserOnServer(qbUser);
            }

            @Override
            public void onError(QBResponseException responseException) {
                Log.d(TAG, "Error SignIn" + responseException.getMessage());
                hideProgressDialog();
                ToastUtils.longToast(R.string.sign_in_error);
            }
        });
    }

    private void updateUserOnServer(QBUser user) {
        user.setPassword(null);
        QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                hideProgressDialog();
                //HomeActivity.start(LoginActivity.this);
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onError(QBResponseException e) {
                hideProgressDialog();
                ToastUtils.longToast(R.string.update_user_error);
            }
        });
    }

    private void loginToChat(final QBUser qbUser) {
        qbUser.setPassword(App.USER_DEFAULT_PASSWORD);
        userForSave = qbUser;
        startLoginService(qbUser);
    }

    private void saveUserData(QBUser qbUser) {
        SharedPrefsHelper sharedPrefsHelper = SharedPrefsHelper.getInstance();
        sharedPrefsHelper.saveQbUser(qbUser);
    }

    private QBUser createUserWithEnteredData() {
        return createQBUserWithCurrentData(userId, name);
    }

    private QBUser createQBUserWithCurrentData(String userLogin, String userFullName) {
        QBUser qbUser = null;
        if (!TextUtils.isEmpty(userLogin) && !TextUtils.isEmpty(userFullName)) {
            qbUser = new QBUser();
            qbUser.setLogin(userLogin);
            qbUser.setFullName(userFullName);
            qbUser.setPassword(App.USER_DEFAULT_PASSWORD);
        }
        return qbUser;
    }
    private void startSignUpNewUser(final QBUser newUser) {
        Log.d(TAG, "SignUp New User");
        showProgressDialog(R.string.dlg_creating_new_user);
        requestExecutor.signUpNewUser(newUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        Log.d(TAG, "SignUp Successful");
                        saveUserData(newUser);
                        loginToChat(result);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.d(TAG, "Error SignUp" + e.getMessage());
                        if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                            signInCreatedUser(newUser);
                        } else {
                            hideProgressDialog();
                            ToastUtils.longToast(R.string.sign_up_error);
                        }
                    }
                }
        );
    }

}

//FirebaseAuth.getInstance().signOut();