package org.nikanikoo.flux.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.squareup.picasso.Picasso;

import org.nikanikoo.flux.R;
import org.nikanikoo.flux.security.AccountManager;

import java.util.List;

public class AccountManagerActivity extends BaseSettingsActivity {

    private LinearLayout accountsList;
    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manager);

        setupToolbar("Управление аккаунтами");

        accountManager = AccountManager.getInstance(this);
        accountsList = findViewById(R.id.accounts_list);
        View addAccountButton = findViewById(R.id.add_account_button);

        loadAccounts();

        addAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("add_account", true);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAccounts();
    }

    private void loadAccounts() {
        accountsList.removeAllViews();

        List<AccountManager.Account> accounts = accountManager.getAccounts();
        String currentAccountId = accountManager.getCurrentAccountId();

        for (AccountManager.Account account : accounts) {
            View accountView = LayoutInflater.from(this).inflate(R.layout.item_account_manage_material, accountsList, false);

            ImageView avatar = accountView.findViewById(R.id.manage_account_avatar);
            TextView name = accountView.findViewById(R.id.manage_account_name);
            TextView instance = accountView.findViewById(R.id.manage_account_instance);
            View selectedIndicator = accountView.findViewById(R.id.manage_account_selected);
            View removeButton = accountView.findViewById(R.id.manage_account_remove);

            name.setText(account.fullName);
            instance.setText(account.instance);

            if (!account.photoUrl.isEmpty()) {
                Picasso.get().load(account.photoUrl).placeholder(R.drawable.camera_200).into(avatar);
            }

            boolean isCurrent = account.id.equals(currentAccountId);
            selectedIndicator.setVisibility(isCurrent ? View.VISIBLE : View.GONE);
            removeButton.setVisibility(isCurrent ? View.GONE : View.VISIBLE);

            accountView.setOnClickListener(v -> {
                if (!isCurrent) {
                    accountManager.switchToAccount(account.id);

                    try {
                        org.nikanikoo.flux.security.TokenManager tokenManager =
                                new org.nikanikoo.flux.security.TokenManager(this);
                        tokenManager.saveToken(account.token);
                        tokenManager.saveInstance(account.instance);
                    } catch (org.nikanikoo.flux.security.TokenManager.EncryptionException e) {
                        Toast.makeText(this, "Ошибка шифрования. Попробуйте переустановить приложение.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Toast.makeText(this, "Переключено на " + account.fullName, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });

            removeButton.setOnClickListener(v -> {
                showRemoveAccountConfirmation(account, () -> {
                    accountManager.removeAccount(account.id);
                    accountsList.removeView(accountView);

                    if (accountManager.getAccountCount() == 0) {
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            });

            accountsList.addView(accountView);
        }
    }

    private void showRemoveAccountConfirmation(AccountManager.Account account, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить аккаунт?")
                .setMessage("Аккаунт " + account.fullName + " (" + account.instance + ") будет удален из приложения.\n\nЭто действие нельзя отменить.")
                .setPositiveButton("Удалить", (dialog, which) -> onConfirm.run())
                .setNegativeButton("Отмена", null)
                .show();
    }
}
