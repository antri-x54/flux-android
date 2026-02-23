package org.nikanikoo.flux.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;
import org.nikanikoo.flux.data.managers.api.OpenVKApi;
import org.nikanikoo.flux.data.managers.ProfileManager;
import org.nikanikoo.flux.data.models.UserProfile;
import org.nikanikoo.flux.R;
import org.nikanikoo.flux.security.AccountManager;
import org.nikanikoo.flux.utils.ThemeManager;

import java.util.Locale;

public class TwoFactorActivity extends AppCompatActivity {

    private TextInputEditText editTwoFactorCode;
    private MaterialButton btnVerifyCode;
    private TextView textResendCode;
    private TextView textUseBackupCode;
    
    private String username;
    private String password;
    private String instance;
    
    private final OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            navigateToLogin();
        }
    };
    
    private void navigateToLogin() {
        Intent intent = new Intent(TwoFactorActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Применяем тему перед super.onCreate()
        ThemeManager themeManager = ThemeManager.getInstance(this);
        themeManager.applyThemeToActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_factor);
        
        ThemeManager.applySystemBarsAppearance(this);
        
        // Применяем Material You динамические цвета если выбрана эта тема
        if (themeManager.getThemeStyle() == ThemeManager.STYLE_MATERIAL_YOU && 
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivityIfAvailable(this);
        }

        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        instance = getIntent().getStringExtra("instance");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editTwoFactorCode = findViewById(R.id.edit_two_factor_code);
        btnVerifyCode = findViewById(R.id.btn_verify_code);

        setupListeners();
        
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
    }

    private void setupListeners() {
        editTwoFactorCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isValidCode = s.length() == 6 && s.toString().matches("\\d{6}");
                btnVerifyCode.setEnabled(isValidCode);

                if (editTwoFactorCode.getError() != null) {
                    editTwoFactorCode.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnVerifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyTwoFactorCode();
            }
        });

        btnVerifyCode.setEnabled(false);
    }

    private void verifyTwoFactorCode() {
        String code = editTwoFactorCode.getText().toString().trim();

        if (code.length() != 6) {
            editTwoFactorCode.setError("Введите 6-значный код");
            return;
        }
        
        if (!code.matches("\\d{6}")) {
            editTwoFactorCode.setError("Код должен содержать только цифры");
            return;
        }

        btnVerifyCode.setEnabled(false);
        btnVerifyCode.setText("Проверка...");

        performTwoFactorLogin(username, password, code);
    }

    private void performTwoFactorLogin(String username, String password, String code) {
        // Сохраняем инстанс перед авторизацией, если он был передан
        if (instance != null && !instance.isEmpty()) {
            OpenVKApi.getInstance(this).saveInstance(instance);
        }
        
        OpenVKApi.getInstance(this).loginWith2FA(username, password, code, new OpenVKApi.LoginCallback() {
            @Override
            public void onSuccess(String token) {
                runOnUiThread(() -> {
                    Toast.makeText(TwoFactorActivity.this, "Код подтвержден", Toast.LENGTH_SHORT).show();
                    OpenVKApi.getInstance(TwoFactorActivity.this).saveToken(token);
                    
                    ProfileManager.getInstance(TwoFactorActivity.this).clearCache();
                    
                    // Load profile and save account
                    ProfileManager.getInstance(TwoFactorActivity.this).loadProfile(false, false, new ProfileManager.ProfileCallback() {
                        @Override
                        public void onSuccess(UserProfile profile) {
                            String currentInstance = OpenVKApi.getInstance(TwoFactorActivity.this).getBaseUrl();
                            AccountManager.getInstance(TwoFactorActivity.this).addAccount(token, currentInstance, profile);
                            
                            Intent intent = new Intent(TwoFactorActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        
                        @Override
                        public void onError(String error) {
                            // Save account even if profile loading fails
                            String currentInstance = OpenVKApi.getInstance(TwoFactorActivity.this).getBaseUrl();
                            UserProfile dummyProfile = new UserProfile();
                            dummyProfile.setId(0);
                            dummyProfile.setFirstName("Загрузка...");
                            dummyProfile.setLastName("");
                            dummyProfile.setScreenName(username);
                            AccountManager.getInstance(TwoFactorActivity.this).addAccount(token, currentInstance, dummyProfile);
                            
                            Intent intent = new Intent(TwoFactorActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    System.out.println("Ошибка 2FA: " + error);
                    
                    String message = "Ошибка проверки кода";
                    
                    try {
                        String jsonError = error;
                        if (error.startsWith("ERROR_JSON:")) {
                            jsonError = error.substring("ERROR_JSON:".length());
                        }

                        JSONObject errorJson = new JSONObject(jsonError);

                        if (errorJson.has("error_code")) {
                            int errorCode = errorJson.getInt("error_code");
                            String errorMsg = errorJson.optString("error_msg", "");
                            
                            if (errorCode == 28) {
                                message = "Неверный код двухфакторной аутентификации";
                            } else if (errorCode == 5) {
                                message = "Ошибка авторизации пользователя";
                            } else {
                                message = errorMsg.isEmpty() ? "Ошибка аутентификации (код " + errorCode + ")" : errorMsg;
                            }
                        }
                        else if (errorJson.has("error")) {
                            String errorCode = errorJson.getString("error");
                            String errorDescription = errorJson.optString("error_description", "");
                            
                            if ("invalid_grant".equals(errorCode)) {
                                if (errorDescription.toLowerCase(Locale.ROOT).contains("2fa") || 
                                    errorDescription.toLowerCase(Locale.ROOT).contains("code")) {
                                    message = "Неверный код двухфакторной аутентификации";
                                } else {
                                    message = "Неверные данные для входа";
                                }
                            } else if ("invalid_request".equals(errorCode)) {
                                message = "Неверный формат запроса";
                            } else {
                                message = errorDescription.isEmpty() ? "Ошибка аутентификации" : errorDescription;
                            }
                        }
                        else if (errorJson.has("error_msg")) {
                            String errorMsg = errorJson.getString("error_msg");
                            if (errorMsg.toLowerCase(Locale.ROOT).contains("invalid") && 
                                errorMsg.toLowerCase(Locale.ROOT).contains("2fa")) {
                                message = "Неверный код двухфакторной аутентификации";
                            } else {
                                message = errorMsg;
                            }
                        } else if (errorJson.has("error_description")) {
                            message = errorJson.getString("error_description");
                        }
                    } catch (Exception e) {
                        System.out.println("Failed to parse 2FA error JSON: " + e.getMessage());
                        
                        // Проверяем простые текстовые ошибки
                        if (error.toLowerCase(Locale.ROOT).contains("invalid") && 
                            error.toLowerCase(Locale.ROOT).contains("code")) {
                            message = "Неверный код двухфакторной аутентификации";
                        } else if (error.toLowerCase(Locale.ROOT).contains("expired")) {
                            message = "Код истек, запросите новый";
                        }
                    }
                    
                    Toast.makeText(TwoFactorActivity.this, message, Toast.LENGTH_LONG).show();
                    btnVerifyCode.setEnabled(true);
                    btnVerifyCode.setText("Подтвердить код");
                    editTwoFactorCode.setError("Неверный код");

                    editTwoFactorCode.setText("");
                });
            }
        });
    }
}