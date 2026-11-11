package com.stormbox.launcher;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * StormBox MVP launcher.
 *
 * Runtime contract expected from a companion runtime app:
 * package: com.stormbox.runtime
 * activity action: com.stormbox.runtime.RUN
 * extras:
 *   "executable_uri" (String) - content URI selected by the user
 *   "game_title"      (String)
 *   "profile"         (String)
 *
 * This is an integration contract, not an existing Winlator API.
 * A runtime app must implement it before the Launch button can work.
 */
public class MainActivity extends Activity {
    private static final int PICK_EXE = 1001;
    private static final String PREFS = "stormbox";
    private static final String KEY_EXE = "exe_uri";
    private static final String RUNTIME_PACKAGE = "com.stormbox.runtime";
    private static final String ACTION_RUN = "com.stormbox.runtime.RUN";

    private SharedPreferences prefs;
    private TextView exeLabel;
    private TextView runtimeLabel;
    private String selectedUri;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        selectedUri = prefs.getString(KEY_EXE, null);
        buildUi();
        refreshStatus();
    }

    private void buildUi() {
        int pad = dp(18);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(pad, pad, pad, pad);
        content.setBackgroundColor(0xFF10151E);

        TextView title = new TextView(this);
        title.setText("STORMBOX");
        title.setTextSize(28);
        title.setTextColor(0xFFFFFFFF);
        title.setGravity(Gravity.CENTER_VERTICAL);
        content.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Windows game launcher • Android ARM64");
        subtitle.setTextColor(0xFFB7C3D4);
        subtitle.setTextSize(14);
        content.addView(subtitle);

        runtimeLabel = makeLabel("Runtime: checking...");
        content.addView(runtimeLabel);

        exeLabel = makeLabel("Game: not selected");
        content.addView(exeLabel);

        Button choose = makeButton("SELECT GAME EXE");
        choose.setOnClickListener(v -> chooseExe());
        content.addView(choose);

        Button launch = makeButton("LAUNCH GAME");
        launch.setOnClickListener(v -> launchGame());
        content.addView(launch);

        Button profile = makeButton("PROFILE: HELIO G100 • 960 × 540");
        profile.setOnClickListener(v ->
            Toast.makeText(this,
                "MVP profile: Box64 / Wine / DXVK settings must be configured in the runtime.",
                Toast.LENGTH_LONG).show());
        content.addView(profile);

        TextView note = makeLabel(
            "Important: StormBox is the launcher only. It does not contain Windows, Wine, Box64, DXVK, game files, or proprietary drivers. Install/configure a compatible companion runtime separately."
        );
        note.setTextSize(12);
        content.addView(note);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);
        setContentView(scroll);
    }

    private TextView makeLabel(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(0xFFE0E7F0);
        view.setTextSize(15);
        view.setPadding(0, dp(14), 0, dp(14));
        return view;
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        lp.topMargin = dp(8);
        lp.bottomMargin = dp(8);
        button.setLayoutParams(lp);
        return button;
    }

    private void chooseExe() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(intent, PICK_EXE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "File picker tidak tersedia.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_EXE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {
                    // Some document providers do not grant persistable permissions.
                }
                selectedUri = uri.toString();
                prefs.edit().putString(KEY_EXE, selectedUri).apply();
                refreshStatus();
            }
        }
    }

    private void launchGame() {
        if (selectedUri == null || selectedUri.isEmpty()) {
            Toast.makeText(this, "Pilih file EXE terlebih dahulu.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent run = new Intent(ACTION_RUN);
        run.setPackage(RUNTIME_PACKAGE);
        run.putExtra("executable_uri", selectedUri);
        run.putExtra("game_title", "Naruto Shippuden Ultimate Ninja Storm Revolution");
        run.putExtra("profile", "helio_g100_960x540_30fps");
        run.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(run);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this,
                "Runtime companion belum tersedia atau belum mendukung kontrak StormBox.",
                Toast.LENGTH_LONG).show();
        }
    }

    private void refreshStatus() {
        if (exeLabel != null) {
            exeLabel.setText(selectedUri == null
                ? "Game: not selected"
                : "Game URI: " + Uri.parse(selectedUri).getLastPathSegment());
        }
        if (runtimeLabel != null) {
            Intent probe = new Intent(ACTION_RUN);
            probe.setPackage(RUNTIME_PACKAGE);
            boolean available = getPackageManager().resolveActivity(probe, 0) != null;
            runtimeLabel.setText(available
                ? "Runtime: companion detected (compatibility still needs testing)"
                : "Runtime: not detected — install a compatible runtime separately");
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
