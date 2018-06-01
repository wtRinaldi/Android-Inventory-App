/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.items;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.items.data.ItemContract;
import com.example.android.items.data.ItemContract.ItemEntry;

import static com.example.android.items.data.ItemContract.ItemEntry.COLUMN_ITEM_NAME;
import static com.example.android.items.data.ItemContract.ItemEntry.COLUMN_ITEM_PICTURE;
import static com.example.android.items.data.ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY;
import static com.example.android.items.data.ItemProvider.LOG_TAG;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;
    private Uri mCurrentItemUri;
    private EditText mNameEditText;
    private EditText mItemEditText;
    private EditText mValueEditText;
    private EditText mQuantityEditText;
    private Spinner mSizeSpinner;
    private int mSize = ItemEntry.ITEM_UNKNOWN;
    private boolean mItemHasChanged = false;
    private static final int IMAGE_REQUEST = 0;
    private TextView mPictureText;
    private Uri mUri;
    private TextView mTextView;
    private ImageView mPictureImageView;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        mTextView = (TextView) findViewById(R.id.image_uri);

        Button pictureButton = (Button) findViewById(R.id.add_picture);
        pictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });

        if (mCurrentItemUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_item));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_item));
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mItemEditText = (EditText) findViewById(R.id.edit_item_type);
        mValueEditText = (EditText) findViewById(R.id.edit_item_value);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mPictureText = (TextView) findViewById(R.id.image_uri);
        mPictureImageView = (ImageView) findViewById(R.id.display_picture);

        mSizeSpinner = (Spinner) findViewById(R.id.spinner_gender);
        mNameEditText.setOnTouchListener(mTouchListener);
        mItemEditText.setOnTouchListener(mTouchListener);
        mValueEditText.setOnTouchListener(mTouchListener);
        mSizeSpinner.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select an image for Item"), IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            mUri = resultData.getData();
            Log.i(LOG_TAG, "Uri: " + mUri.toString());
            mTextView.setText(mUri.toString());
        }
    }

    private void setupSpinner() {

        ArrayAdapter sizeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_size_options, android.R.layout.simple_spinner_item);

        sizeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mSizeSpinner.setAdapter(sizeSpinnerAdapter);

        mSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.item_large))) {
                        mSize = ItemEntry.ITEM_LARGE;
                    } else if (selection.equals(getString(R.string.item_small))) {
                        mSize = ItemEntry.ITEM_SMALL;
                    } else {
                        mSize = ItemEntry.ITEM_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSize = ItemEntry.ITEM_UNKNOWN;
            }
        });
    }

    private void saveItem() {

        String nameString = mNameEditText.getText().toString().trim();
        String typeString = mItemEditText.getText().toString().trim();
        String valueString = mValueEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String pictureString = mPictureText.getText().toString().trim();

        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(typeString) ||
                TextUtils.isEmpty(valueString) || TextUtils.isEmpty(quantityString) ||
                TextUtils.isEmpty(pictureString)) {
                    Toast.makeText(this, getString(R.string.invalid_entry),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, nameString);
        values.put(ItemEntry.COLUMN_ITEM_TYPE, typeString);
        values.put(ItemEntry.COLUMN_ITEM_SIZE, mSize);
        int value = 0;
        if (!TextUtils.isEmpty(valueString)) {
            value = Integer.parseInt(valueString);
        }
        values.put(ItemEntry.COLUMN_ITEM_VALUE, value);

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(COLUMN_ITEM_QUANTITY, quantity);
        values.put(COLUMN_ITEM_PICTURE, pictureString);

        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                    saveItem();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                ItemEntry._ID,
                COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_TYPE,
                ItemEntry.COLUMN_ITEM_SIZE,
                ItemEntry.COLUMN_ITEM_VALUE,
                COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_PICTURE};

        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_NAME);
            int typeColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_TYPE);
            int sizeColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SIZE);
            int valueColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_VALUE);
            int quantityColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_QUANTITY);
            int pictureColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_PICTURE);


            String name = cursor.getString(nameColumnIndex);
            String type = cursor.getString(typeColumnIndex);
            int size = cursor.getInt(sizeColumnIndex);
            int value = cursor.getInt(valueColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String picture = cursor.getString(pictureColumnIndex);


            mNameEditText.setText(name);
            mItemEditText.setText(type);
            switch (size) {
                case ItemContract.ItemEntry.ITEM_LARGE:
                    mSizeSpinner.setSelection(1);
                    break;
                case ItemContract.ItemEntry.ITEM_SMALL:
                    mSizeSpinner.setSelection(2);
                    break;
                default:
                    mSizeSpinner.setSelection(0);
                    break;
            }
            mValueEditText.setText(Integer.toString(value));
            mQuantityEditText.setText(Integer.toString(quantity));
            //mPictureText.setText(picture);
            mPictureImageView.setImageURI(Uri.parse(picture));
        }

        Button orderButton = (Button) findViewById(R.id.order);
        Button plus = (Button) findViewById(R.id.plus);
        Button minus = (Button) findViewById(R.id.minus);

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemIdColumnIndex = cursor.getColumnIndex(ItemEntry._ID);
                final long itemId = cursor.getLong(itemIdColumnIndex);
                Uri mCurrentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, itemId);
                int nameColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_NAME);
                String itemName = cursor.getString(nameColumnIndex);
                String subjectLine = "Need to order: " + itemName;

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));

                intent.putExtra(Intent.EXTRA_SUBJECT, subjectLine);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            };
        });

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemIdColumnIndex = cursor.getColumnIndex(ItemEntry._ID);
                final long itemId = cursor.getLong(itemIdColumnIndex);
                Uri mCurrentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, itemId);
                int quantityColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_QUANTITY);
                String itemQuantity = cursor.getString(quantityColumnIndex);
                int updateQuantity = Integer.parseInt(itemQuantity);
                updateQuantity++;
                ContentValues updateValues = new ContentValues();
                updateValues.put(ItemEntry.COLUMN_ITEM_QUANTITY, updateQuantity);
                int rowsUpdate = getContentResolver().update(mCurrentItemUri, updateValues, null, null);
            };
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemIdColumnIndex = cursor.getColumnIndex(ItemEntry._ID);
                final long itemId = cursor.getLong(itemIdColumnIndex);
                Uri mCurrentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, itemId);
                int quantityColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_QUANTITY);
                String itemQuantity = cursor.getString(quantityColumnIndex);
                int updateQuantity = Integer.parseInt(itemQuantity);

                if (updateQuantity > 0) {
                    updateQuantity--;
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(ItemEntry.COLUMN_ITEM_QUANTITY, updateQuantity);
                    int rowsUpdate = getContentResolver().update(mCurrentItemUri, updateValues, null, null);
                } else {
                    Toast.makeText(EditorActivity.this, "SOLD OUT!", Toast.LENGTH_SHORT).show();
                };
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mItemEditText.setText("");
        mValueEditText.setText("");
        mQuantityEditText.setText("");
        mSizeSpinner.setSelection(0);
        mPictureText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}