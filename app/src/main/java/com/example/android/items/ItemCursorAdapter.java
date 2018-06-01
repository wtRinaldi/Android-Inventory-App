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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.items.data.ItemContract.ItemEntry;

import static com.example.android.items.R.id.quantity;
import static com.example.android.items.data.ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY;

public class ItemCursorAdapter extends CursorAdapter {


    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView valueTextView = (TextView) view.findViewById(R.id.value);
        final TextView quantityTextView = (TextView) view.findViewById(quantity);
        Button sellButton = (Button) view.findViewById(R.id.sale);
        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        int valueColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_VALUE);
        int quantityColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_QUANTITY);
        String itemName = cursor.getString(nameColumnIndex);
        String itemValue = cursor.getString(valueColumnIndex);
        final String itemQuantity = cursor.getString(quantityColumnIndex);

        if (TextUtils.isEmpty(itemValue)) {
            itemValue = context.getString(R.string.value_zero);
        }

        final int position = cursor.getPosition();
        sellButton.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                cursor.moveToPosition(position);
                int itemIdColumnIndex = cursor.getColumnIndex(ItemEntry._ID);
                final long itemId = cursor.getLong(itemIdColumnIndex);
                Uri mCurrentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, itemId);
                int quantityColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_QUANTITY);
                String itemQuantity = cursor.getString(quantityColumnIndex);
                int updateQuantity = Integer.parseInt(itemQuantity);

                if (updateQuantity > 0) {
                    updateQuantity--;
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(COLUMN_ITEM_QUANTITY, updateQuantity);
                    context.getContentResolver().update(mCurrentItemUri, updateValues, null, null);
                } else {
                    Toast.makeText(context, "SOLD OUT!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        nameTextView.setText(itemName);
        valueTextView.setText(itemValue);
        quantityTextView.setText(itemQuantity);
    }
}