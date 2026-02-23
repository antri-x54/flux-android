package org.nikanikoo.flux.ui.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.squareup.picasso.Picasso;

import org.nikanikoo.flux.R;
import org.nikanikoo.flux.security.AccountManager;
import org.nikanikoo.flux.ui.activities.LoginActivity;
import org.nikanikoo.flux.ui.activities.MainActivity;

import java.util.List;

import android.widget.ImageView;

/**
 * Dialog for managing multiple accounts
 */
public class AccountManagerDialog extends DialogFragment {
    
    public interface AccountManagerCallback {
        void onAccountSwitched();
        void onAccountRemoved();
    }
    
    private AccountManagerCallback callback;
    
    public void setCallback(AccountManagerCallback callback) {
        this.callback = callback;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Управление аккаунтами");
        
        // Inflate custom layout
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_account_manager, null);
        LinearLayout accountsList = view.findViewById(R.id.accounts_list);
        View addAccountButton = view.findViewById(R.id.add_account_button);
        
        AccountManager accountManager = AccountManager.getInstance(requireContext());
        List<AccountManager.Account> accounts = accountManager.getAccounts();
        String currentAccountId = accountManager.getCurrentAccountId();
        
        // Populate accounts list
        for (AccountManager.Account account : accounts) {
            View accountView = LayoutInflater.from(requireContext()).inflate(R.layout.item_account_manage, null);
            
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
            
            // Switch to account on click
            accountView.setOnClickListener(v -> {
                if (!isCurrent) {
                    accountManager.switchToAccount(account.id);
                    
                    // Update TokenManager with new account credentials
                    try {
                        org.nikanikoo.flux.security.TokenManager tokenManager =
                            new org.nikanikoo.flux.security.TokenManager(requireContext());
                        tokenManager.saveToken(account.token);
                        tokenManager.saveInstance(account.instance);
                    } catch (org.nikanikoo.flux.security.TokenManager.EncryptionException e) {
                        Toast.makeText(requireContext(), "Ошибка шифрования. Попробуйте переустановить приложение.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    Toast.makeText(requireContext(), "Переключено на " + account.fullName, Toast.LENGTH_SHORT).show();
                    
                    if (callback != null) callback.onAccountSwitched();
                    dismiss();
                    
                    // Restart MainActivity to apply new account
                    Intent intent = new Intent(requireContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    requireContext().startActivity(intent);
                }
            });
            
            // Remove account
            removeButton.setOnClickListener(v -> {
                showRemoveAccountConfirmation(account, () -> {
                    accountManager.removeAccount(account.id);
                    accountsList.removeView(accountView);
                    
                    if (callback != null) callback.onAccountRemoved();
                    
                    // If no accounts left, go to login
                    if (accountManager.getAccountCount() == 0) {
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        requireContext().startActivity(intent);
                    }
                });
            });
            
            accountsList.addView(accountView);
        }
        
        // Add new account
        addAccountButton.setOnClickListener(v -> {
            dismiss();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.putExtra("add_account", true);
            startActivity(intent);
        });
        
        builder.setView(view);
        builder.setNegativeButton("Закрыть", null);
        
        return builder.create();
    }
    
    private void showRemoveAccountConfirmation(AccountManager.Account account, Runnable onConfirm) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Удалить аккаунт?")
            .setMessage("Аккаунт " + account.fullName + " (" + account.instance + ") будет удален из приложения.\n\nЭто действие нельзя отменить.")
            .setPositiveButton("Удалить", (dialog, which) -> onConfirm.run())
            .setNegativeButton("Отмена", null)
            .show();
    }
}
