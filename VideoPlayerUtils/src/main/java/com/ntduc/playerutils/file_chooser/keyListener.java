package com.ntduc.playerutils.file_chooser;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Objects;

class keyListener implements DialogInterface.OnKeyListener {
    private WeakReference<ChooserDialog> _c;

    keyListener(ChooserDialog c) {
        this._c = new WeakReference<>(c);
    }

    /**
     * Called when a key is dispatched to a dialog. This allows listeners to
     * get a chance to respond before the dialog.
     *
     * @param dialog  the dialog the key has been dispatched to
     * @param keyCode the code for the physical key that was pressed
     * @param event   the KeyEvent object containing full information about
     *                the event
     * @return {@code true} if the listener has consumed the event,
     * {@code false} otherwise
     */
    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return false;

        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BUTTON_B) {
            if (_c.get().get_newFolderView() != null && Objects.requireNonNull(_c.get().get_newFolderView()).getVisibility() == VISIBLE) {
                Objects.requireNonNull(_c.get().get_newFolderView()).setVisibility(GONE);
                return true;
            }
            Objects.requireNonNull(_c.get().get_onBackPressed()).onBackPressed(_c.get().get_alertDialog());
            return true;
        }

        if (!_c.get().get_enableDpad()) return true;

        if (!Objects.requireNonNull(_c.get().get_list()).hasFocus()) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (_c.get().get_neutralBtn() == null) {
                    return false;
                }
                if (Objects.requireNonNull(_c.get().get_neutralBtn()).hasFocus() || Objects.requireNonNull(_c.get().get_negativeBtn()).hasFocus()
                        || Objects.requireNonNull(_c.get().get_positiveBtn()).hasFocus()) {
                    if (_c.get().get_options() != null && Objects.requireNonNull(_c.get().get_options()).getVisibility() == VISIBLE) {
                        Objects.requireNonNull(_c.get().get_options()).requestFocus(
                                Objects.requireNonNull(_c.get().get_neutralBtn()).hasFocus() ? View.FOCUS_RIGHT : View.FOCUS_LEFT);
                        return true;
                    } else if (_c.get().get_newFolderView() != null
                            && Objects.requireNonNull(_c.get().get_newFolderView()).getVisibility() == VISIBLE) {
                        Objects.requireNonNull(_c.get().get_newFolderView()).requestFocus(View.FOCUS_LEFT);
                        return true;
                    } else {
                        Objects.requireNonNull(_c.get().get_list()).requestFocus();
                        _c.get().setLastSelected(true);
                        return true;
                    }
                }
                if (_c.get().get_options() != null && Objects.requireNonNull(_c.get().get_options()).hasFocus()) {
                    Objects.requireNonNull(_c.get().get_list()).requestFocus();
                    _c.get().setLastSelected(true);
                    return true;
                }
            } else {
                return false;
            }
        }

        if (Objects.requireNonNull(_c.get().get_list()).hasFocus()) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    Objects.requireNonNull(_c.get().get_onBackPressed()).onBackPressed(_c.get().get_alertDialog());
                    _c.get().setLastSelected(false);
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    Objects.requireNonNull(_c.get().get_list()).performItemClick(_c.get().get_list(), Objects.requireNonNull(_c.get().get_list()).getSelectedItemPosition(),
                        Objects.requireNonNull(_c.get().get_list()).getSelectedItemId());
                    _c.get().setLastSelected(false);
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (_c.get().getLastSelected()) {
                        _c.get().setLastSelected(false);
                        if (_c.get().get_options() != null && Objects.requireNonNull(_c.get().get_options()).getVisibility() == VISIBLE) {
                            Objects.requireNonNull(_c.get().get_options()).requestFocus();
                        } else {
                            if (Objects.requireNonNull(_c.get().get_neutralBtn()).getVisibility() == VISIBLE) {
                                Objects.requireNonNull(_c.get().get_neutralBtn()).requestFocus();
                            } else {
                                Objects.requireNonNull(_c.get().get_negativeBtn()).requestFocus();
                            }
                        }
                        return true;
                    }
                    break;
                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    protected void finalize() throws Throwable {
        this._c.clear();
        this._c = null;
        super.finalize();
    }
}
